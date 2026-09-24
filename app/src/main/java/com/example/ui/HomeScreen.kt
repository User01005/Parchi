package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BillEntity

@Composable
fun HomeScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredBills by viewModel.filteredBills.collectAsStateWithLifecycle()
    val allBills by viewModel.savedBills.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8E9ED))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main Content Area based on selected Tab
            when (uiState.selectedHomeTab) {
                HomeNavTab.Receipts -> {
                    ReceiptsTabContent(
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onCreateReceiptClick = { viewModel.startNewReceipt() },
                        bills = filteredBills,
                        totalSavedCount = allBills.size,
                        onBillClick = { bill -> viewModel.openExistingBill(bill) },
                        onDeleteBill = { bill -> viewModel.deleteSavedBill(bill) },
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
                HomeNavTab.DailySales -> {
                    DailySalesTabContent(
                        bills = allBills,
                        onBillClick = { bill -> viewModel.openExistingBill(bill) },
                        modifier = Modifier.weight(1f)
                    )
                }
                HomeNavTab.Create -> {
                    // Triggered directly, fallback to receipts tab
                }
            }

            // Improved 3-Item Capsule Navbar: Receipts, NEW, Sales
            HomeBottomNavigationBar(
                selectedTab = uiState.selectedHomeTab,
                onSelectTab = { tab -> viewModel.selectHomeTab(tab) }
            )
        }
    }
}

/**
 * Improved 3-Item Bottom Navigation Bar (Receipts, + NEW BILL, Sales)
 * Designed with a floating capsule dock for tactile, one-handed usability
 */
@Composable
private fun HomeBottomNavigationBar(
    selectedTab: HomeNavTab,
    onSelectTab: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(30.dp), spotColor = Color(0x24000000))
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFD4D4D8), RoundedCornerShape(30.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Receipts
            HomeNavTabButton(
                icon = Icons.Default.ReceiptLong,
                label = "Receipts",
                isSelected = selectedTab == HomeNavTab.Receipts,
                onClick = { onSelectTab(HomeNavTab.Receipts) },
                testTag = "tab_receipts",
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Create (Hero Accent Pill: + NEW BILL)
            Box(
                modifier = Modifier
                    .weight(1.15f)
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF18181B))
                    .clickable { onSelectTab(HomeNavTab.Create) }
                    .padding(horizontal = 12.dp)
                    .testTag("tab_create"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Bill",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "NEW BILL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }

            // Tab 3: Daily Sales
            HomeNavTabButton(
                icon = Icons.Default.TrendingUp,
                label = "Sales",
                isSelected = selectedTab == HomeNavTab.DailySales,
                onClick = { onSelectTab(HomeNavTab.DailySales) },
                testTag = "tab_sales",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeNavTabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Color(0xFFF4F4F5) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF18181B) else Color(0xFF71717A),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) Color(0xFF18181B) else Color(0xFF71717A)
            )
        }
    }
}

/**
 * Main Receipts Tab: Search Bar + Create Hero Action + Past Receipts List
 */
@Composable
private fun ReceiptsTabContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreateReceiptClick: () -> Unit,
    bills: List<BillEntity>,
    totalSavedCount: Int,
    onBillClick: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Store Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MANMOHAN DI HATTI",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = Color(0xFF18181B)
                )
                Text(
                    text = "Kirana Voice Billing POS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF71717A)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF18181B))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "POS READY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF4ADE80)
                )
            }
        }

        // 1. Bill Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                .testTag("search_bills_input"),
            placeholder = {
                Text(
                    text = "Search receipts by name, item or #bill...",
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF71717A),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF18181B),
                unfocusedBorderColor = Color(0xFFE4E4E7)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. "Create a Receipt" Elevated Hero Action Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x24000000))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF27272A),
                            Color(0xFF18181B),
                            Color(0xFF09090B)
                        )
                    )
                )
                .clickable { onCreateReceiptClick() }
                .padding(20.dp)
                .testTag("create_receipt_hero_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE RECEIPT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap to record voice items or create bill",
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F3F46)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Past Receipts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAST RECEIPTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = Color(0xFF71717A)
            )

            Text(
                text = "${bills.size} total",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFA1A1AA)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Empty state check
        if (bills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "No receipts",
                        tint = Color(0xFFD4D4D8),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching receipts" else "No receipts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF18181B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap 'Create Receipt' above to start your first bill.",
                        fontSize = 13.sp,
                        color = Color(0xFF71717A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Group by Date for clean organization
            val groupedByDate = remember(bills) {
                bills.groupBy {
                    it.dateDisplay.ifBlank { it.formattedDateTime.split("•").firstOrNull()?.trim() ?: "Recent" }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedByDate.forEach { (dateHeader, dateBills) ->
                    item(key = "header_$dateHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                tint = Color(0xFF71717A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateHeader,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    items(dateBills, key = { it.id }) { bill ->
                        val itemsList = remember(bill.itemsJson) {
                            viewModel.repository.parseItemsJson(bill.itemsJson)
                        }
                        val itemsSummary = itemsList.take(3).joinToString(", ") { "${it.weightOrQuantity} ${it.itemName}" } +
                                if (itemsList.size > 3) " +${itemsList.size - 3} more" else ""

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(16.dp))
                                .clickable { onBillClick(bill) }
                                .padding(14.dp)
                                .testTag("receipt_card_${bill.billNumber}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${bill.billNumber}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF18181B)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (bill.customerName.isNotBlank()) bill.customerName else "Walk-in",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF52525B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (itemsSummary.isNotBlank()) {
                                        Text(
                                            text = itemsSummary,
                                            fontSize = 11.sp,
                                            color = Color(0xFF71717A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = bill.formattedDateTime,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${if (bill.totalAmount % 1.0 == 0.0) bill.totalAmount.toInt() else String.format("%.2f", bill.totalAmount)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF18181B)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onDeleteBill(bill) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFA1A1AA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Daily Sales Tab
 */
@Composable
private fun DailySalesTabContent(
    bills: List<BillEntity>,
    onBillClick: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRevenue = remember(bills) { bills.sumOf { it.totalAmount } }
    val grouped = remember(bills) {
        bills.groupBy {
            it.dateDisplay.ifBlank { it.formattedDateTime.split("•").firstOrNull()?.trim() ?: "Recent" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "MANMOHAN DI HATTI • DAILY SALES",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = Color(0xFF18181B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Total Cumulative Sales Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF18181B))
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "TOTAL REVENUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "₹ ${if (totalRevenue % 1.0 == 0.0) totalRevenue.toInt() else String.format("%.2f", totalRevenue)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Text(
                    text = "Across ${bills.size} receipts",
                    fontSize = 12.sp,
                    color = Color(0xFF71717A)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "BY DATE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF71717A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            grouped.forEach { (date, group) ->
                val dayTotal = group.sumOf { it.totalAmount }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = date,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF18181B)
                            )
                            Text(
                                text = "${group.size} receipts",
                                fontSize = 11.sp,
                                color = Color(0xFF71717A)
                            )
                        }

                        Text(
                            text = "₹${if (dayTotal % 1.0 == 0.0) dayTotal.toInt() else String.format("%.2f", dayTotal)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF18181B)
                        )
                    }
                }
            }
        }
    }
}
