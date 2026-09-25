package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.ui.theme.PastelAmber
import com.example.ui.theme.PastelAmberAccent
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSkyAccent
import java.util.Locale

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
            .background(Color(0xFFF7F8FB))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Store Name on Left, Search Icon on Right (POS READY REMOVED)
            HomeTopBar(
                storeName = uiState.storeName,
                isSearchExpanded = uiState.isSearchExpanded,
                onToggleSearch = { viewModel.toggleSearch() }
            )

            // Expandable Search Bar (When Search Icon is Toggled)
            AnimatedVisibility(
                visible = uiState.isSearchExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                            .testTag("search_bills_input"),
                        placeholder = {
                            Text(
                                text = "Search by bill #, customer, item or date...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PastelSkyAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    viewModel.updateSearchQuery("")
                                } else {
                                    viewModel.toggleSearch()
                                    focusManager.clearFocus()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PastelSkyAccent,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }
            }

            // Main Content Area based on Selected Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.selectedHomeTab) {
                    HomeNavTab.Receipts -> {
                        ReceiptsTabContent(
                            bills = filteredBills,
                            searchQuery = uiState.searchQuery,
                            onBillClick = { bill -> viewModel.openExistingBill(bill) },
                            onDeleteBill = { bill -> viewModel.deleteSavedBill(bill) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    HomeNavTab.DailySales -> {
                        DailySalesTabContent(
                            bills = allBills,
                            onBillClick = { bill -> viewModel.openExistingBill(bill) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    HomeNavTab.Profile -> {
                        StoreProfileTabContent(
                            storeName = uiState.storeName,
                            storeCategory = uiState.storeCategory,
                            storePhone = uiState.storePhone,
                            onSave = { name, cat, phone ->
                                viewModel.updateStoreProfile(name, cat, phone)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // THE HIGHLIGHT: "Start Another Receipt" Action Bar Just Above The Navigation Bar
            StartReceiptActionBar(
                onStartReceipt = { viewModel.startNewReceipt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Modern Squircle Navigation Bar (Pastel Tints, Squircle Pill Indicators)
            HomeSquircleNavigationBar(
                selectedTab = uiState.selectedHomeTab,
                onSelectTab = { tab -> viewModel.selectHomeTab(tab) }
            )
        }
    }
}

/**
 * Clean Top Bar: Business Name on Left, Search Icon on Right (POS READY Removed)
 */
@Composable
private fun HomeTopBar(
    storeName: String,
    isSearchExpanded: Boolean,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = storeName.uppercase(Locale.getDefault()),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Kirana Voice Billing POS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B)
            )
        }

        // Search Icon Button in Squircle Container
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Color(0x14000000))
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSearchExpanded) PastelSky else Color.White)
                .border(
                    1.dp,
                    if (isSearchExpanded) PastelSkyAccent else Color(0xFFE2E8F0),
                    RoundedCornerShape(14.dp)
                )
                .clickable { onToggleSearch() }
                .testTag("home_search_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Bills",
                tint = if (isSearchExpanded) PastelSkyAccent else Color(0xFF334155),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Dedicated Action Bar positioned right above the navigation bar:
 * "Start Another Receipt" / "Create Another Bill"
 */
@Composable
private fun StartReceiptActionBar(
    onStartReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1F000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.2.dp, PastelRoseAccent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable { onStartReceipt() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("start_another_receipt_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "START ANOTHER RECEIPT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PastelRose)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VOICE POS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = PastelRoseAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to speak items or create bill",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Squircle Mic Action Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PastelRose)
                    .border(1.dp, PastelRoseAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start Voice Receipt",
                    tint = PastelRoseAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Modern Squircle Navigation Bar (3 Tabs: Receipts, Daily Sales, Store Profile)
 */
@Composable
private fun HomeSquircleNavigationBar(
    selectedTab: HomeNavTab,
    onSelectTab: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Receipts
            SquircleNavTabItem(
                icon = Icons.Default.ReceiptLong,
                label = "Receipts",
                isSelected = selectedTab == HomeNavTab.Receipts,
                activeBgColor = PastelLavender,
                activeAccentColor = PastelLavenderAccent,
                onClick = { onSelectTab(HomeNavTab.Receipts) },
                testTag = "tab_receipts",
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Daily Sales
            SquircleNavTabItem(
                icon = Icons.Default.TrendingUp,
                label = "Daily Sales",
                isSelected = selectedTab == HomeNavTab.DailySales,
                activeBgColor = PastelAmber,
                activeAccentColor = PastelAmberAccent,
                onClick = { onSelectTab(HomeNavTab.DailySales) },
                testTag = "tab_sales",
                modifier = Modifier.weight(1f)
            )

            // Tab 3: Store Profile / Settings
            SquircleNavTabItem(
                icon = Icons.Default.Storefront,
                label = "My Store",
                isSelected = selectedTab == HomeNavTab.Profile,
                activeBgColor = PastelMint,
                activeAccentColor = PastelMintAccent,
                onClick = { onSelectTab(HomeNavTab.Profile) },
                testTag = "tab_profile",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SquircleNavTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    activeBgColor: Color,
    activeAccentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) activeBgColor else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeAccentColor else Color(0xFF64748B),
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) activeAccentColor else Color(0xFF64748B)
            )
        }
    }
}

/**
 * Main Receipts Tab: Grouped Past Receipts List
 */
@Composable
private fun ReceiptsTabContent(
    bills: List<BillEntity>,
    searchQuery: String,
    onBillClick: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bills.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PastelLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No Receipts",
                        tint = PastelLavenderAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (searchQuery.isNotEmpty()) "No matching receipts" else "No receipts yet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (searchQuery.isNotEmpty()) "Try searching for a different item or customer name"
                    else "Tap 'Start Another Receipt' below to dictate your first bill!",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        val groupedBills = remember(bills) {
            bills.groupBy { it.dateDisplay.ifEmpty { "Recent Bills" } }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupedBills.forEach { (dateHeader, dateBills) ->
                item(key = "header_$dateHeader") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PastelMint)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dateHeader.uppercase(Locale.getDefault()),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PastelMintAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${dateBills.size} bills • ₹${String.format(Locale.US, "%.0f", dateBills.sumOf { it.totalAmount })}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                items(dateBills, key = { it.id }) { bill ->
                    PastReceiptSquircleCard(
                        bill = bill,
                        onClick = { onBillClick(bill) },
                        onDelete = { onDeleteBill(bill) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Squircle Card for each saved Receipt
 */
@Composable
private fun PastReceiptSquircleCard(
    bill: BillEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x12000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            .clickable { onClick() }
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bill.formattedDateTime.substringAfter("•").trim(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                if (bill.customerName.isNotBlank() || bill.customerPhone.isNotBlank()) {
                    Text(
                        text = listOf(bill.customerName, bill.customerPhone).filter { it.isNotBlank() }.joinToString(" • "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PastelLavenderAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${bill.itemCount} items",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${if (bill.totalAmount % 1.0 == 0.0) bill.totalAmount.toInt() else String.format(Locale.US, "%.2f", bill.totalAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Daily Sales Analytics Content
 */
@Composable
private fun DailySalesTabContent(
    bills: List<BillEntity>,
    onBillClick: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRevenue = remember(bills) { bills.sumOf { it.totalAmount } }
    val totalBills = bills.size
    val totalItems = remember(bills) { bills.sumOf { it.itemCount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // Daily Summary Card in Warm Pastel Mint Squircle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x14000000))
                .clip(RoundedCornerShape(22.dp))
                .background(PastelMint)
                .border(1.2.dp, PastelMintBorder, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL SALES REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PastelMintAccent
                    )
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Sales",
                        tint = PastelMintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹ ${if (totalRevenue % 1.0 == 0.0) totalRevenue.toInt() else String.format(Locale.US, "%.2f", totalRevenue)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "BILLS ISSUED", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF64748B))
                            Text(text = "$totalBills", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "TOTAL ITEMS", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF64748B))
                            Text(text = "$totalItems", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALL TRANSACTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF475569),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
                PastReceiptSquircleCard(
                    bill = bill,
                    onClick = { onBillClick(bill) },
                    onDelete = {}
                )
            }
        }
    }
}

/**
 * Tab 3: Store Profile & Settings Content
 */
@Composable
private fun StoreProfileTabContent(
    storeName: String,
    storeCategory: String,
    storePhone: String,
    onSave: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editName by remember(storeName) { mutableStateOf(storeName) }
    var editCategory by remember(storeCategory) { mutableStateOf(storeCategory) }
    var editPhone by remember(storePhone) { mutableStateOf(storePhone) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x12000000))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "STORE INFORMATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PastelLavenderAccent
                )

                Column {
                    Text(
                        text = "STORE / BUSINESS NAME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "STORE CATEGORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "STORE PHONE / WHATSAPP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSave(editName, editCategory, editPhone)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "SAVE STORE PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
