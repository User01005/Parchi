package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String,
    val formattedDateTime: String,
    val dateKey: String = "",       // e.g. "2026-09-21" for chronological sorting & date grouping
    val dateDisplay: String = "",   // e.g. "21 Sep 2026" for human-readable grouping header
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "",
    val customerPhone: String = "",
    val customerHouseNo: String = "",
    val itemsJson: String,
    val totalAmount: Double = 0.0,
    val itemCount: Int = 0,
    val isVerified: Boolean = false
)
