package com.example.data

import com.example.model.BillEntity
import com.example.model.BillItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillRepository(private val billDao: BillDao) {

    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val distinctDates: Flow<List<String>> = billDao.getDistinctDates()

    fun getBillsByDate(dateKey: String): Flow<List<BillEntity>> {
        return billDao.getBillsByDate(dateKey)
    }

    suspend fun saveBill(
        billNumber: String,
        formattedDateTime: String,
        customerName: String,
        customerPhone: String,
        customerHouseNo: String,
        items: List<BillItem>,
        totalAmount: Double,
        isVerified: Boolean
    ): Long {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("serialNumber", item.serialNumber)
                put("itemName", item.itemName)
                put("weightOrQuantity", item.weightOrQuantity)
                if (item.price != null) {
                    put("price", item.price)
                } else {
                    put("price", JSONObject.NULL)
                }
                put("isVerified", item.isVerified)
            }
            jsonArray.put(obj)
        }

        val now = Date()
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val dateDisplay = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)

        val entity = BillEntity(
            billNumber = billNumber,
            formattedDateTime = formattedDateTime,
            dateKey = dateKey,
            dateDisplay = dateDisplay,
            timestamp = System.currentTimeMillis(),
            customerName = customerName,
            customerPhone = customerPhone,
            customerHouseNo = customerHouseNo,
            itemsJson = jsonArray.toString(),
            totalAmount = totalAmount,
            itemCount = items.size,
            isVerified = isVerified
        )

        return billDao.insertBill(entity)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun deleteBillById(billId: Long) {
        billDao.deleteBillById(billId)
    }

    fun parseItemsJson(jsonStr: String): List<BillItem> {
        val result = mutableListOf<BillItem>()
        if (jsonStr.isBlank()) return result
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val price = if (obj.isNull("price")) null else obj.optDouble("price")
                result.add(
                    BillItem(
                        id = obj.optString("id"),
                        serialNumber = obj.optInt("serialNumber", i + 1),
                        itemName = obj.optString("itemName"),
                        weightOrQuantity = obj.optString("weightOrQuantity"),
                        price = price,
                        isVerified = obj.optBoolean("isVerified", false)
                    )
                )
            }
        } catch (_: Exception) {
            // Return whatever parsed so far
        }
        return result
    }
}
