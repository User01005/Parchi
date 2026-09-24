package com.example.model

import java.util.UUID

/**
 * Represents an individual grocery item on the bill.
 *
 * @param id Unique identifier
 * @param serialNumber S.No. (1, 2, 3...)
 * @param itemName Name of the grocery item (e.g. Atta, Sugar, Moong Dal)
 * @param weightOrQuantity Weight or quantity (e.g. "10 kg", "500 gm", "2 packets")
 * @param price Price in currency (optional; null if shopkeeper didn't say the price)
 * @param isVerified Whether shopkeeper has verified this line item
 */
data class BillItem(
    val id: String = UUID.randomUUID().toString(),
    val serialNumber: Int = 1,
    val itemName: String = "",
    val weightOrQuantity: String = "",
    val price: Double? = null,
    val isVerified: Boolean = false
) {
    val formattedPrice: String
        get() = price?.let { "₹${if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)}" } ?: ""
}
