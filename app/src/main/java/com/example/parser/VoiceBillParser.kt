package com.example.parser

import com.example.model.BillItem
import java.util.Locale

data class ParsedCustomerInfo(
    val name: String? = null,
    val phone: String? = null,
    val houseNo: String? = null
)

data class ParseResult(
    val items: List<BillItem> = emptyList(),
    val customerInfo: ParsedCustomerInfo = ParsedCustomerInfo()
)

object VoiceBillParser {

    // Regex patterns for quantity / weight units (English + Hindi transliterated + Devanagari)
    private val UNIT_REGEX =
        """(?i)(?:^|\b)(\d+(?:\.\d+)?)\s*(kg|kgs|kilo|kilos|kilogram|kilograms|gm|gms|gram|grams|g|litre|litres|liter|liters|lt|l|ml|packet|packets|pkt|pkts|pouch|pouches|pudhiya|piece|pieces|pc|pcs|nag|dane|dozen|dozens|darjan|bottle|bottles|botal|box|boxes|dabba|dibba|tin|tins|can|cans|bori|katta|bag|bags|किलो|किग्रा|ग्राम|ग्रा|लीटर|ली|मिली|पैकेट|पुड़िया|पीस|नग|दाने|दर्जन|बोतल|डिब्बा|डब्बा|टिन|कैन|बोरी|कट्टा|थैली)(?:(?=[^\w\u0900-\u097F])|$)""".toRegex()

    // Pattern for "50 rupaye ka dahi" or "10 rs wala biscuit" or "50 रुपये का दही"
    private val HINDI_PRICE_LEAD_REGEX =
        """(?i)^(\d+(?:\.\d+)?)\s*(?:rupaye|rupee|rupees|rs|inr|₹|रुपये|रुपए|रुपिया|रु)\s*(?:ka|ki|ke|wala|wali|wale|का|की|के|वाला|वाली|वाले)\s+(.+)$""".toRegex()

    // Standalone count at start, e.g. "2 brush" or "3 soaps" or "2 bread"
    private val LEADING_COUNT_REGEX = """(?i)^(\d+)\s+([a-zA-Z\u0900-\u097F\s\-]+)""".toRegex()

    // Standalone count at end, e.g. "brush 2" or "brush 2 pcs"
    private val TRAILING_COUNT_REGEX = """(?i)^([a-zA-Z\u0900-\u097F\s\-]+?)\s+(\d+)\s*(?:piece|pieces|pc|pcs|nag|dane|पीस|नग)?$""".toRegex()

    // Price patterns: explicit price preceded or followed by currency/rate keywords
    private val EXPLICIT_PRICE_REGEX =
        """(?i)(?:(?:rs\.?|rupees|rupaye|inr|₹|रुपये|रुपए|रुपिया|रु|price|rate|bhav|भाव|दाम|दर)\s*(\d+(?:\.\d+)?)|(\d+(?:\.\d+)?)\s*(?:rs\.?|rupees|rupaye|inr|₹|रुपये|रुपए|रुपिया|रु|ka|ki|का|की|\/-))\s*$""".toRegex()

    // Trailing bare price, e.g. "10 kg atta 356"
    private val TRAILING_BARE_PRICE_REGEX = """(?i)\s+(\d+(?:\.\d+)?)\s*$""".toRegex()

    // Thinking and hesitation phrases to filter out so user's train of thought isn't captured as items
    private val THINKING_FILLER_WORDS = listOf(
        "okay", "ok", "alright", "all right",
        "hmmm", "hmm", "ummm", "umm", "um", "uhh", "uh",
        "wait", "wait a second", "wait a minute", "let me see", "let's see", "let me think", "what else",
        "second", "seconds", "minute", "minutes",
        "achha", "acha", "theek hai", "thik hai", "haan", "haa", "ha",
        "matlab", "matlab ki", "ek second", "1 second", "ek minute", "1 minute", "ruko", "rukiye", "socho", "dekhne do", "dekho",
        "aur kya tha", "kya tha", "aur kya", "aur kya chahiye", "kya bole",
        "bhaiya", "bhai", "sir", "madam", "sunno", "suno", "listen",
        "ek kaam karo", "ek kaam kijiye", "aur batao",
        "bas itna hi", "itna hi", "bas", "ho gaya", "done", "that's it", "that is all", "khatam"
    )

    /**
     * Parses raw spoken transcript into grocery bill items and optional customer info.
     * Accurately removes hesitation/filler words, resolves repetitive quantity stumbles ("two piece, two piece, two brush"),
     * and preserves English/Hindi item names with correct units.
     */
    fun parseTranscript(transcript: String, startingSerial: Int = 1): ParseResult {
        if (transcript.isBlank()) return ParseResult()

        val normalized = normalizeSpeechText(transcript.trim())
        val (customerInfo, cleanItemSpeech) = extractAndRemoveCustomerInfo(normalized)

        if (cleanItemSpeech.isBlank()) {
            return ParseResult(items = emptyList(), customerInfo = customerInfo)
        }

        // Clean out end-of-speech terminal fillers like "bas itna hi", "that's it", "done"
        var speech = cleanItemSpeech
            .replace(Regex("""(?i)\b(?:bas\s+itna\s+hi|itna\s+hi|bas|ho\s+gaya|done|that's\s+it|that\s+is\s+all|khatam|bas\s+yehi)\s*$"""), "")
            .trim()

        // Handle repetitive stutter / quantity hesitations before an item, e.g.:
        // "2 piece 2 piece 2 brush" -> "2 brush"
        // "2 piece, 2 piece, 2 brush" -> "2 brush"
        speech = speech.replace(
            Regex("""(?i)\b(\d+)\s*(?:piece|pieces|pc|pcs)?(?:\s*,\s*|\s+)+(?:\1\s*(?:piece|pieces|pc|pcs)?(?:\s*,\s*|\s+)+)*(?=\d+\s*[a-zA-Z\u0900-\u097F])"""),
            ""
        ).trim()

        // Split text by conjunctions & natural speech pauses:
        // "and", "aur", "और", "plus", "then", "phir", "फिर", commas, newlines, or thinking boundaries ("okay", "wait", "achha")
        val splitRegex = Regex("""(?i),\s*|\n+|\s+and\s+|\s+aur\s+|\s+और\s+|\s+तथा\s+|\s+एवं\s+|\s+plus\s+|\s+then\s+|\s+phir\s+|\s+फिर\s+|\s+saath\s+mein\s+|\s+sath\s+me\s+|\s+(?:okay|ok|wait\s+a\s+second|wait\s+1\s+second|wait\s+ek\s+second|wait|1\s+second|ek\s+second|1\s+minute|ek\s+minute|achha|acha|ruko|theek\s+hai)\s+""")
        val primaryClauses = speech.split(splitRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Also handle clauses where multiple items are spoken back-to-back without explicit conjunctions
        val itemClauses = mutableListOf<String>()
        val multiItemBoundaryRegex = Regex("""(?<=(?:kg|kgs|kilo|kilos|gram|grams|gm|gms|g|litre|litres|liter|liters|l|ml|packet|packets|pkt|pkts|pouch|pouches|piece|pieces|pc|pcs|botal|bottle|dabba|dibba|bori|katta|bags|किलो|किग्रा|ग्राम|लीटर|पैकेट|पीस|बोतल|डिब्बा|बोरी|कट्टा)\b|\b\d+(?:\.\d+)?\b)\s+(?=(?:[a-zA-Z\u0900-\u097F]+\s+\d+|\d+\s*(?:kg|kgs|kilo|kilos|gram|gm|g|litre|liter|packet|piece|pcs|किलो|ग्राम|लीटर|पैकेट|पीस)))""")

        for (primary in primaryClauses) {
            val singleDirect = parseSingleItemClause(primary, 0)
            if (singleDirect != null && singleDirect.weightOrQuantity != "1 item") {
                // If the clause directly parses as an item with its unit or count, keep it intact
                itemClauses.add(primary)
            } else if (UNIT_REGEX.findAll(primary).count() > 1) {
                // Multiple items fused without conjunctions
                val subClauses = primary.split(multiItemBoundaryRegex).map { it.trim() }.filter { it.isNotBlank() }
                itemClauses.addAll(subClauses)
            } else {
                itemClauses.add(primary)
            }
        }

        val parsedItems = mutableListOf<BillItem>()
        var currentSerial = startingSerial

        for (clause in itemClauses) {
            val item = parseSingleItemClause(clause, currentSerial)
            if (item != null) {
                // Check if this item is a duplicate repetition right after (self-correction or echo)
                val lastItem = parsedItems.lastOrNull()
                if (lastItem != null && lastItem.itemName.equals(item.itemName, ignoreCase = true) && lastItem.weightOrQuantity == item.weightOrQuantity) {
                    // Skip redundant immediate duplicate echo
                    continue
                }
                parsedItems.add(item)
                currentSerial++
            }
        }

        return ParseResult(items = parsedItems, customerInfo = customerInfo)
    }

    /**
     * Normalizes both English and Hindi number words, Devanagari numerals, and fractions.
     * Ensures phrases like "two brush" -> "2 brush", "Aata 10 kg", "Cheeni 10 kg" are normalized.
     */
    private fun normalizeSpeechText(text: String): String {
        var s = text

        // Convert Devanagari numerals (०, १, २, ३, ४, ५, ६, ७, ८, ९) to ASCII digits (0-9)
        s = buildString(s.length) {
            for (ch in s) {
                if (ch in '\u0966'..'\u096F') {
                    append((ch - '\u0966' + '0'.code).toChar())
                } else {
                    append(ch)
                }
            }
        }

        // Fractional quantities (English + Hindi)
        s = s.replace(Regex("""(?i)\b(?:half|aadha|adha|आधा)\s*(?:kg|kilo|किलो|किग्रा)\b"""), "500 g")
        s = s.replace(Regex("""(?i)\b(?:half|aadha|adha|आधा)\s*(?:litre|liter|l|लीटर)\b"""), "500 ml")
        s = s.replace(Regex("""(?i)\b(?:quarter|paav|pao|paw|पाव|एक\s+पाव)\s*(?:kg|kilo|किलो)?\b"""), "250 g")
        s = s.replace(Regex("""(?i)\b(?:one\s+and\s+a\s+half|dedh|derh|डेढ़)\s*(?:kg|kilo|किलो)\b"""), "1.5 kg")
        s = s.replace(Regex("""(?i)\b(?:one\s+and\s+a\s+half|dedh|derh|डेढ़)\s*(?:litre|liter|l|लीटर)\b"""), "1.5 L")
        s = s.replace(Regex("""(?i)\b(?:two\s+and\s+a\s+half|dhai|ढाई)\s*(?:kg|kilo|किलो)\b"""), "2.5 kg")
        s = s.replace(Regex("""(?i)\b(?:dhai\s+sau|dhaisau|ढाई\s*सौ)\s*(?:gram|gm|g|ग्राम)?\b"""), "250 g")
        s = s.replace(Regex("""(?i)\b(?:saadhe\s+teen|sadhe\s+teen|साढ़े\s*तीन)\s*(?:kg|kg|किलो)\b"""), "3.5 kg")

        // Compound numbers (e.g. "twenty five", "twenty-five")
        s = s.replace(Regex("""(?i)\btwenty[\s\-]five\b"""), "25")
        s = s.replace(Regex("""(?i)\btwenty[\s\-]two\b"""), "22")
        s = s.replace(Regex("""(?i)\bthirty[\s\-]five\b"""), "35")

        // English and Hindi numbers dictionary
        val numberWords = listOf(
            // English number words
            "zero" to "0",
            "one" to "1",
            "two" to "2",
            "three" to "3",
            "four" to "4",
            "five" to "5",
            "six" to "6",
            "seven" to "7",
            "eight" to "8",
            "nine" to "9",
            "ten" to "10",
            "eleven" to "11",
            "twelve" to "12",
            "thirteen" to "13",
            "fourteen" to "14",
            "fifteen" to "15",
            "sixteen" to "16",
            "seventeen" to "17",
            "eighteen" to "18",
            "nineteen" to "19",
            "twenty" to "20",
            "thirty" to "30",
            "forty" to "40",
            "fifty" to "50",
            "sixty" to "60",
            "seventy" to "70",
            "eighty" to "80",
            "ninety" to "90",
            "hundred" to "100",

            // Hindi transliterated & Devanagari numbers
            "ek" to "1", "एक" to "1",
            "do" to "2", "दो" to "2",
            "teen" to "3", "तीन" to "3",
            "chaar" to "4", "char" to "4", "चार" to "4",
            "paanch" to "5", "panch" to "5", "पाँच" to "5", "पांच" to "5",
            "chhe" to "6", "cheh" to "6", "che" to "6", "छह" to "6", "छः" to "6",
            "saat" to "7", "सात" to "7",
            "aath" to "8", "आठ" to "8",
            "nau" to "9", "नौ" to "9",
            "das" to "10", "दस" to "10",
            "gyarah" to "11", "ग्यारह" to "11",
            "barah" to "12", "बारह" to "12",
            "terah" to "13", "तेरह" to "13",
            "chaudah" to "14", "चौदह" to "14",
            "pandrah" to "15", "पंद्रह" to "15",
            "solah" to "16", "सोलह" to "16",
            "satrah" to "17", "सत्रह" to "17",
            "atharah" to "18", "अठारह" to "18",
            "unnis" to "19", "उन्नीस" to "19",
            "bees" to "20", "बीस" to "20",
            "pachees" to "25", "पच्चीस" to "25",
            "tees" to "30", "तीस" to "30",
            "chaalis" to "40", "चालीस" to "40",
            "pachaas" to "50", "पचास" to "50",
            "sau" to "100", "सौ" to "100"
        )

        for ((word, digit) in numberWords) {
            // Replace word when followed by a unit, currency, or noun word
            s = s.replace(
                Regex("""(?i)\b$word\b(?=\s+(?:kg|kgs|kilo|gram|gm|g|packet|pkt|pkts|pouch|litre|liter|l|ml|piece|pieces|pc|pcs|botal|bottle|dabba|box|bori|rupaye|rs|rupees|₹|किलो|किग्रा|ग्राम|लीटर|पैकेट|पीस|बोतल|डिब्बा|बोरी|रुपये|रुपए|[a-zA-Z\u0900-\u097F]))"""),
                digit
            )
        }

        return s
    }

    /**
     * Extracts customer info (name, phone, house) and cleanly strips ONLY the matched customer phrases,
     * leaving the item speech completely intact.
     */
    private fun extractAndRemoveCustomerInfo(text: String): Pair<ParsedCustomerInfo, String> {
        var remaining = text
        var name: String? = null
        var phone: String? = null
        var houseNo: String? = null

        // 1. Phone / Mobile / Telephone (10-digit Indian mobile number)
        val phonePattern = """(?i)\b(?:mobile|phone|contact|number|ph|telephone|telephon|call)\s*(?:is|no|number|:)?\s*(?:\+?91[\-\s]?)?([6-9]\d{9})\b""".toRegex()
        val phoneMatch = phonePattern.find(remaining)
        if (phoneMatch != null) {
            phone = phoneMatch.groupValues[1]
            remaining = remaining.removeRange(phoneMatch.range).trim()
        } else {
            val barePhone = """\b([6-9]\d{9})\b""".toRegex().find(remaining)
            if (barePhone != null) {
                phone = barePhone.groupValues[1]
                remaining = remaining.removeRange(barePhone.range).trim()
            }
        }

        // 2. House / Flat / Address
        val housePattern = """(?i)\b(?:house|flat|room|door|makan)\s*(?:number|no|#)?\s*([a-zA-Z0-9\-\/]+)""".toRegex()
        val houseMatch = housePattern.find(remaining)
        if (houseMatch != null) {
            val candidateHouse = houseMatch.groupValues[1]
            if (candidateHouse.isNotBlank() && !candidateHouse.equals("is", ignoreCase = true) && !candidateHouse.equals("ka", ignoreCase = true)) {
                houseNo = candidateHouse
                remaining = remaining.removeRange(houseMatch.range).trim()
            }
        }

        // 3. Customer Name
        val namePattern = """(?i)\b(?:grahak\s+ka\s+naam|grahak\s+naam|grahak|customer\s+name|customer's\s+name|customer|naam\s+hai|naam)\s*[:\-]?\s*([a-zA-Z\u0900-\u0963\u0972-\u097F]+(?:\s+[a-zA-Z\u0900-\u0963\u0972-\u097F]+)?)""".toRegex()
        val nameMatch = namePattern.find(remaining)
        if (nameMatch != null) {
            val candidate = nameMatch.groupValues[1].trim()
            val reservedGroceryTerms = setOf(
                "atta", "sugar", "cheeni", "rice", "chawal", "dal", "daal", "oil", "tel", "sarson",
                "milk", "doodh", "tea", "chai", "soap", "sabun", "biscuit", "namak", "salt", "brush",
                "kg", "kilo", "gram", "gm", "packet", "pkt", "rupaye", "rs", "rupees", "bhav", "rate",
                "hai", "ka", "ki", "ke", "ko"
            )
            val words = candidate.split(Regex("""\s+""")).filter { it.isNotBlank() && it.all { ch -> ch.isLetter() } }
            val isReserved = words.any { reservedGroceryTerms.contains(it.lowercase(Locale.ROOT)) }
            if (!isReserved && words.isNotEmpty()) {
                name = words.joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
                remaining = remaining.removeRange(nameMatch.range).trim()
            }
        }

        remaining = remaining.replace(Regex("""^[,\s\-\–]+"""), "")
            .replace(Regex("""[,\s\-\–]+$"""), "")
            .trim()

        return Pair(ParsedCustomerInfo(name = name, phone = phone, houseNo = houseNo), remaining)
    }

    /**
     * Parses a single item clause, e.g.:
     * "two brush" -> Brush, 2 pcs
     * "Aata 10 kg" -> Aata, 10 kg
     * "Cheeni 10 kg" -> Cheeni, 10 kg
     * "10 kg of atta 356" -> Atta, 10 kg, ₹356
     * "50 rupaye ka dahi" -> Dahi, 1 item, ₹50
     * Discards orphan clauses with no item name (e.g. standalone "2 piece" or filler words).
     */
    fun parseSingleItemClause(clause: String, serialNumber: Int): BillItem? {
        var text = clause.trim()
        if (text.isBlank()) return null

        // Strip leading/trailing thinking and filler words
        text = stripThinkingFillers(text)
        if (text.isBlank()) return null

        // Check Pattern A: "50 rupaye ka dahi" or "10 rs wala biscuit"
        val hindiPriceLeadMatch = HINDI_PRICE_LEAD_REGEX.find(text)
        if (hindiPriceLeadMatch != null) {
            val priceVal = hindiPriceLeadMatch.groupValues[1].toDoubleOrNull()
            val itemNameRaw = cleanItemName(hindiPriceLeadMatch.groupValues[2])
            if (itemNameRaw.isNotBlank()) {
                return BillItem(
                    serialNumber = serialNumber,
                    itemName = itemNameRaw,
                    weightOrQuantity = "1 item",
                    price = priceVal,
                    isVerified = false
                )
            }
        }

        var price: Double? = null

        // 1. Check for explicit price ("280 rupees", "280 rupaye", "rs 356", "₹45")
        val explicitMatch = EXPLICIT_PRICE_REGEX.find(text)
        if (explicitMatch != null) {
            val numStr = explicitMatch.groupValues[1].ifEmpty { explicitMatch.groupValues[2] }
            price = numStr.toDoubleOrNull()
            text = text.removeRange(explicitMatch.range).trim()
        } else {
            // 2. Check for trailing bare price number, e.g. "10 kg atta 356"
            val trailingMatch = TRAILING_BARE_PRICE_REGEX.find(text)
            if (trailingMatch != null) {
                val candidatePrice = trailingMatch.groupValues[1]
                val remainingBeforePrice = text.substring(0, trailingMatch.range.first).trim()
                if (remainingBeforePrice.any { it.isLetter() }) {
                    price = candidatePrice.toDoubleOrNull()
                    text = remainingBeforePrice
                }
            }
        }

        var weightOrQuantity = ""
        var itemName = ""

        // Check for standard unit match (e.g. "10 kg", "500 g", "2 litre", "2 piece")
        val unitMatch = UNIT_REGEX.find(text)
        if (unitMatch != null) {
            val amount = unitMatch.groupValues[1]
            val rawUnit = unitMatch.groupValues[2].lowercase(Locale.ROOT)
            val standardizedUnit = standardizeUnit(rawUnit)
            weightOrQuantity = "$amount $standardizedUnit"

            // Item name is before and/or after unit: e.g. "Aata 10 kg" or "10 kg Aata" or "2 piece brush"
            val beforeUnit = text.substring(0, unitMatch.range.first).trim()
            val afterUnit = text.substring(unitMatch.range.last + 1).trim()
            val combined = ("$beforeUnit $afterUnit").trim()
            itemName = cleanItemName(combined)
        } else {
            // Check for leading count e.g. "2 brush", "3 soaps", "2 bread"
            val leadingCountMatch = LEADING_COUNT_REGEX.find(text)
            if (leadingCountMatch != null) {
                val count = leadingCountMatch.groupValues[1]
                val remainder = leadingCountMatch.groupValues[2].trim()
                val cleanRemainder = cleanItemName(remainder)
                if (cleanRemainder.isNotBlank()) {
                    weightOrQuantity = "$count pcs"
                    itemName = cleanRemainder
                }
            } else {
                // Check for trailing count e.g. "brush 2" or "brush 2 pcs"
                val trailingCountMatch = TRAILING_COUNT_REGEX.find(text)
                if (trailingCountMatch != null) {
                    val remainder = trailingCountMatch.groupValues[1].trim()
                    val count = trailingCountMatch.groupValues[2]
                    val cleanRemainder = cleanItemName(remainder)
                    if (cleanRemainder.isNotBlank()) {
                        weightOrQuantity = "$count pcs"
                        itemName = cleanRemainder
                    }
                }
            }

            // Fallback: standalone item name
            if (itemName.isBlank()) {
                val cleanFallback = cleanItemName(text)
                if (cleanFallback.isNotBlank()) {
                    itemName = cleanFallback
                    weightOrQuantity = "1 item"
                }
            }
        }

        // If no genuine item name could be resolved (e.g. user just said "2 piece" without naming an item), discard orphan!
        if (itemName.isBlank()) {
            return null
        }

        return BillItem(
            serialNumber = serialNumber,
            itemName = itemName,
            weightOrQuantity = weightOrQuantity,
            price = price,
            isVerified = false
        )
    }

    private fun stripThinkingFillers(input: String): String {
        var s = input.trim()
        // Strip filler words from start or end
        val fillerPattern = Regex("""(?i)^(?:okay|ok|wait\s+a\s+second|wait\s+1\s+second|wait\s+ek\s+second|wait\s+a\s+minute|wait|achha|acha|theek\s+hai|thik\s+hai|haan|haa|ha|hmmm?|ummm?|uhh?|ek\s+second|1\s+second|ek\s+minute|1\s+minute|ruko|socho|bhaiya|suno|listen|ek\s+kaam\s+karo)\s+""")
        while (fillerPattern.containsMatchIn(s)) {
            s = s.replace(fillerPattern, "").trim()
        }

        val trailingFillerPattern = Regex("""(?i)\s+(?:okay|ok|bas|ho\s+gaya|done|that's\s+it|khatam|bas\s+itna\s+hi|itna\s+hi)$""")
        while (trailingFillerPattern.containsMatchIn(s)) {
            s = s.replace(trailingFillerPattern, "").trim()
        }

        // If the entire clause is just a filler word, empty it out
        val lower = s.lowercase(Locale.ROOT)
        if (THINKING_FILLER_WORDS.contains(lower)) {
            return ""
        }

        return s
    }

    private fun standardizeUnit(unit: String): String {
        return when (unit) {
            "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms", "किलो", "किग्रा" -> "kg"
            "gm", "gms", "gram", "grams", "g", "ग्राम", "ग्रा" -> "g"
            "litre", "litres", "liter", "liters", "lt", "l", "लीटर", "ली" -> "L"
            "ml", "मिली" -> "ml"
            "packet", "packets", "pkt", "pkts", "pouch", "pouches", "pudhiya", "पैकेट", "पुड़िया", "थैली" -> "pkts"
            "piece", "pieces", "pc", "pcs", "nag", "dane", "पीस", "नग", "दाने" -> "pcs"
            "dozen", "dozens", "darjan", "दर्जन" -> "dozen"
            "bottle", "bottles", "botal", "बोतल" -> "bottles"
            "box", "boxes", "dabba", "dibba", "डिब्बा", "डब्बा" -> "boxes"
            "tin", "tins", "can", "cans", "टिन", "कैन" -> "tins"
            "bori", "katta", "bag", "bags", "बोरी", "कट्टा" -> "bags"
            else -> unit
        }
    }

    private fun cleanItemName(rawName: String): String {
        var clean = rawName
            .replace(Regex("""(?i)\b(of|ka|ki|ke|walay|wala|wali|wale|item|chahiye|de\s+do|dena|aur|and|piece|pieces|pc|pcs|second|seconds|minute|minutes|wait|ruko)\b"""), " ")
            .replace(Regex("""[^\w\s\-\u0900-\u097F]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (clean.isBlank()) return ""

        // Filter out filler words if any remained in the item name
        val words = clean.split(" ").filter { word ->
            val w = word.lowercase(Locale.ROOT)
            !THINKING_FILLER_WORDS.contains(w) && w != "piece" && w != "pieces" && w != "pc" && w != "pcs"
        }

        if (words.isEmpty()) return ""

        return words.joinToString(" ") { word ->
            // If English or Latin script, use titlecase
            if (word.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                word.lowercase(Locale.ROOT)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            } else {
                // Keep Devanagari as is
                word
            }
        }
    }
}
