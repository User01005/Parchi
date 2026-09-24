package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BillRepository
import com.example.model.BillEntity
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkFaint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PriceGreen
import com.example.ui.theme.PrintPrimaryBlue
import com.example.ui.theme.SharePrimaryGreen
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WoodenFrameDark
import com.example.util.ReceiptPrintHelper
import com.example.util.ReceiptShareHelper

@Composable
fun HistoricalBillsDialog(
    bills: List<BillEntity>,
    repository: BillRepository,
    onDismiss: () -> Unit,
    onLoadBill: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedDateFilter by remember { mutableStateOf<String?>(null) } // null = All Dates

    // Group bills by dateKey or dateDisplay
    val distinctDates = remember(bills) {
        bills.map { it.dateDisplay.ifBlank { it.formattedDateTime.split(",").firstOrNull()?.trim() ?: "Recent" } }
            .distinct()
    }

    val filteredBills = remember(bills, selectedDateFilter) {
        if (selectedDateFilter == null) {
            bills
        } else {
            bills.filter {
                val display = it.dateDisplay.ifBlank { it.formattedDateTime.split(",").firstOrNull()?.trim() ?: "Recent" }
                display == selectedDateFilter
            }
        }
    }

    val totalRevenue = remember(filteredBills) {
        filteredBills.sumOf { it.totalAmount }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(6.dp)
                .testTag("historical_bills_dialog"),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WoodenFrameDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Previous Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Text(
                                text = "${filteredBills.size} bills • Total: ₹${if (totalRevenue % 1.0 == 0.0) totalRevenue.toInt() else String.format("%.2f", totalRevenue)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PriceGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Filter Chips (Shopkeeper can view transactions by date)
                if (distinctDates.isNotEmpty()) {
                    Text(
                        text = "VIEW BY DATE:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = InkMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All Dates" Chip
                        item {
                            val isSelected = selectedDateFilter == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) WoodenFrameDark else Color(0xFFEFECE6))
                                    .border(1.dp, if (isSelected) WoodenFrameDark else Color(0xFFDED8CD), RoundedCornerShape(16.dp))
                                    .clickable { selectedDateFilter = null }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "All Dates (${bills.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else InkBlack
                                )
                            }
                        }

                        // Individual Date Chips
                        items(distinctDates) { dateStr ->
                            val isSelected = selectedDateFilter == dateStr
                            val countForDate = bills.count {
                                val d = it.dateDisplay.ifBlank { it.formattedDateTime.split(",").firstOrNull()?.trim() ?: "Recent" }
                                d == dateStr
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) WoodenFrameDark else Color(0xFFEFECE6))
                                    .border(1.dp, if (isSelected) WoodenFrameDark else Color(0xFFDED8CD), RoundedCornerShape(16.dp))
                                    .clickable { selectedDateFilter = dateStr }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Date",
                                        tint = if (isSelected) Color.White else InkMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$dateStr ($countForDate)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else InkBlack
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of Bills Grouped By Date
                if (filteredBills.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "No Bills",
                                tint = InkFaint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions found for this date.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkMuted
                            )
                            Text(
                                text = "Speak and save bills to build daily transaction logs.",
                                fontSize = 12.sp,
                                color = InkFaint
                            )
                        }
                    }
                } else {
                    // Group filtered bills by their display date
                    val groupedMap = filteredBills.groupBy {
                        it.dateDisplay.ifBlank { it.formattedDateTime.split(",").firstOrNull()?.trim() ?: "Recent" }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        groupedMap.forEach { (dateGroup, groupBills) ->
                            val dayTotal = groupBills.sumOf { it.totalAmount }

                            // Date Group Header
                            item(key = "header_$dateGroup") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3EFE8))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Date Group",
                                            tint = WoodenFrameDark,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dateGroup,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WoodenFrameDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${groupBills.size} bills)",
                                            fontSize = 11.sp,
                                            color = InkMuted
                                        )
                                    }

                                    Text(
                                        text = "Day Sales: ₹${if (dayTotal % 1.0 == 0.0) dayTotal.toInt() else String.format("%.2f", dayTotal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceGreen
                                    )
                                }
                            }

                            // Items for this Date Group
                            items(groupBills, key = { it.id }) { bill ->
                                HistoricalBillCard(
                                    bill = bill,
                                    repository = repository,
                                    context = context,
                                    onLoadBill = onLoadBill,
                                    onDeleteBill = onDeleteBill
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TactileButton(
                    text = "Close",
                    onClick = onDismiss,
                    backgroundColor = Color(0xFFE2DED4),
                    textColor = InkBlack,
                    modifier = Modifier.fillMaxWidth(),
                    height = 46.dp
                )
            }
        }
    }
}

@Composable
private fun HistoricalBillCard(
    bill: BillEntity,
    repository: BillRepository,
    context: Context,
    onLoadBill: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit
) {
    val items = remember(bill.itemsJson) {
        repository.parseItemsJson(bill.itemsJson)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFAF8F5))
            .border(1.2.dp, Color(0xFFE5E0D5), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            // Bill Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${bill.billNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${bill.itemCount} items",
                        fontSize = 11.sp,
                        color = InkMuted
                    )
                }

                Text(
                    text = "₹${if (bill.totalAmount % 1.0 == 0.0) bill.totalAmount.toInt() else String.format("%.2f", bill.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = PriceGreen,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time and Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (bill.customerName.isNotBlank()) "Customer: ${bill.customerName}" else "Walk-in Customer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkBlack
                )

                Text(
                    text = bill.formattedDateTime,
                    fontSize = 11.sp,
                    color = InkFaint
                )
            }

            // Preview items string
            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val itemsSummary = items.take(4).joinToString(", ") { "${it.weightOrQuantity} ${it.itemName}" } +
                        if (items.size > 4) " +${items.size - 4} more" else ""
                Text(
                    text = itemsSummary,
                    fontSize = 11.sp,
                    color = InkMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons: Load into current bill, Print, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Load button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFECE6))
                        .clickable { onLoadBill(bill) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Load",
                            tint = InkBlack,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Load",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                    }
                }

                // Quick Print
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDBEAFE))
                        .clickable {
                            ReceiptPrintHelper.printReceipt(
                                context = context,
                                storeName = "Kirana Store",
                                billNumber = bill.billNumber,
                                dateTime = bill.formattedDateTime,
                                customerName = bill.customerName,
                                customerPhone = bill.customerPhone,
                                customerHouseNo = bill.customerHouseNo,
                                items = items,
                                totalAmount = bill.totalAmount
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = PrintPrimaryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Print",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrintPrimaryBlue
                        )
                    }
                }

                // Quick Share
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7))
                        .clickable {
                            ReceiptShareHelper.shareReceipt(
                                context = context,
                                storeName = "Kirana Store",
                                billNumber = bill.billNumber,
                                dateTime = bill.formattedDateTime,
                                customerName = bill.customerName,
                                customerPhone = bill.customerPhone,
                                customerHouseNo = bill.customerHouseNo,
                                items = items,
                                totalAmount = bill.totalAmount
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = SharePrimaryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Share",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SharePrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Delete Icon
                IconButton(
                    onClick = { onDeleteBill(bill) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
