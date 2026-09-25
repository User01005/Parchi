package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.BillItem
import com.example.parser.ParseResult
import com.example.parser.ParsedCustomerInfo
import com.example.parser.VoiceBillParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

object GeminiBillParser {
    private const val TAG = "GeminiBillParser"
    // Using recommended gemini-3.5-flash model as mandated by gemini-api skill
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun parseWithGeminiOrFallback(transcript: String, startingSerial: Int = 1): ParseResult {
        if (transcript.isBlank()) return ParseResult()

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = withContext(Dispatchers.IO) {
                    callGeminiApi(transcript, apiKey, startingSerial)
                }
                if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                    Log.d(TAG, "Gemini AI parsed ${geminiResult.items.size} items successfully")
                    return geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call error or network unavailable, switching to smart local parser", e)
            }
        }

        // Fast, accurate local Kirana & phonetic parser fallback
        return VoiceBillParser.parseTranscript(transcript, startingSerial)
    }

    private fun callGeminiApi(transcript: String, apiKey: String, startingSerial: Int): ParseResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        val prompt = """
            You are an expert AI Voice Billing Assistant for Indian Kirana & Grocery stores (PARCHI POS).
            Your goal is to parse raw, continuous, noisy spoken speech into structured grocery bill items and optional customer info.

            CRITICAL INTELLIGENCE & PHONETIC CORRECTION RULES:
            1. Speech-To-Text engines frequently mishear Indian grocery terms when spoken in Hindi/Hinglish. You MUST phonetically reconstruct the intended Kirana item:
               - "aa", "aata", "ata", "aataa" -> "Atta"
               - "tama", "ragma", "raj ma", "razma" -> "Rajma"
               - "ida", "meda", "mayda", "mida" -> "Maida"
               - "sugar core", "shakar", "cheeni", "chini" -> "Sugar"
               - "sooji", "suji", "rawa", "rava" -> "Suji"
               - "baisan", "basan" -> "Besan"
               - "sarso", "sarson tel", "mustard" -> "Mustard Oil"
               - "refine", "refined" -> "Refined Oil"
               - "toor dal", "arhar dal", "tuvar dal", "chana dal", "moong dal", "urad dal" -> standardized Dal names
               - "core", "aur", "and", "plus", "sath me" -> conjunctions, DO NOT treat "core" as an item!
            2. Split multi-item dictations cleanly into individual items with their proper quantities.
            3. Extract weights / quantities accurately: e.g. "10 kg", "5 kg", "2 kg", "3 kg", "500 g", "250 g", "1 L", "500 ml", "2 pkts", "3 pcs", "1 bori", "1 bottle", "1 box".
            4. Extract price numbers if spoken (e.g. "10 kg atta 350 rupees" -> price: 350.0). If no price is mentioned, set price to null.
            5. Extract customer details if present (name, 10-digit mobile, house/flat number).

            Spoken Transcript:
            "$transcript"

            Return ONLY a valid JSON object following this exact schema:
            {
              "customerName": null,
              "customerPhone": null,
              "customerHouseNo": null,
              "items": [
                {
                  "itemName": "Atta",
                  "weightOrQuantity": "10 kg",
                  "price": null
                }
              ]
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            }
            put("generationConfig", generationConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error code: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val text = parts.getJSONObject(0).optString("text")
            if (text.isBlank()) return null

            val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedObj = JSONObject(cleanJson)

            val customerName = parsedObj.optString("customerName").takeIf { it.isNotBlank() && it != "null" }
            val customerPhone = parsedObj.optString("customerPhone").takeIf { it.isNotBlank() && it != "null" }
            val customerHouseNo = parsedObj.optString("customerHouseNo").takeIf { it.isNotBlank() && it != "null" }

            val itemsArray = parsedObj.optJSONArray("items") ?: JSONArray()
            val itemsList = mutableListOf<BillItem>()

            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val rawName = itemObj.optString("itemName").trim()
                if (rawName.isBlank()) continue

                val rawQty = itemObj.optString("weightOrQuantity").ifEmpty { "1 item" }
                val price = if (itemObj.isNull("price") || !itemObj.has("price")) null else itemObj.optDouble("price").takeIf { !it.isNaN() }

                itemsList.add(
                    BillItem(
                        serialNumber = startingSerial + i,
                        itemName = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        weightOrQuantity = rawQty,
                        price = price,
                        isVerified = false
                    )
                )
            }

            return ParseResult(
                items = itemsList,
                customerInfo = ParsedCustomerInfo(
                    name = customerName,
                    phone = customerPhone,
                    houseNo = customerHouseNo
                )
            )
        }
    }
}
