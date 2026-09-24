package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BillItem
import com.example.ui.components.CustomerEditDialog
import com.example.ui.components.EditItemDialog
import com.example.ui.components.ManualEditReceiptSheet
import com.example.ui.components.MinimalThermalReceiptContent
import com.example.ui.components.ReceiptBottomBar
import com.example.ui.components.ReceiptEditChoiceDialog
import com.example.ui.components.ThermalPrinterDispenser
import com.example.util.ReceiptPrintHelper
import com.example.util.ReceiptShareHelper

@Composable
fun ReceiptScreen(
    viewModel: BillViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showManualAddDialog by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is needed to speak receipt items.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

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
            // Top Navigation Bar: Minimal back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { viewModel.navigateToHome() },
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(2.dp, CircleShape, spotColor = Color(0x14000000))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD4D4D8), CircleShape)
                        .testTag("receipt_top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to home",
                        tint = Color(0xFF18181B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Status pill or title
                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF18181B))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CREATING RECEIPT...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                } else if (uiState.isListening) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "● RECORDING...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE11D48)
                        )
                    }
                } else {
                    Text(
                        text = "RECEIPT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Spacer(modifier = Modifier.width(38.dp))
            }

            // Scrollable Content: 3D Thermal Printer Dispenser Slot + Emerging Receipt
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Realistic 3D Thermal Printer Slot from reference image
                ThermalPrinterDispenser(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MinimalThermalReceiptContent(
                        dateTime = uiState.formattedDateTime,
                        billNumber = uiState.billNumber,
                        customerName = uiState.customerName,
                        customerPhone = uiState.customerPhone,
                        customerHouseNo = uiState.customerHouseNo,
                        items = uiState.items,
                        totalAmount = uiState.totalAmount,
                        isProcessing = uiState.isProcessing,
                        onEditCustomerClick = { viewModel.openCustomerEdit() },
                        onToggleVerify = { viewModel.toggleItemVerification(it) },
                        onEditItem = { viewModel.openEditItem(it) },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Section: Single Record Button (before finished) or Pill Navbar (when finished)
            ReceiptBottomBar(
                isReceiptFinished = uiState.isReceiptFinished || uiState.items.isNotEmpty(),
                isListening = uiState.isListening,
                liveTranscript = uiState.liveTranscript,
                isProcessing = uiState.isProcessing,
                onRecordClick = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        viewModel.toggleVoiceRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onBackClick = { viewModel.navigateToHome() },
                onEditClick = { viewModel.openEditOptions() },
                onPrintClick = {
                    if (uiState.items.isEmpty()) {
                        Toast.makeText(context, "No items to print", Toast.LENGTH_SHORT).show()
                    } else {
                        ReceiptPrintHelper.printReceipt(
                            context = context,
                            storeName = uiState.storeName,
                            billNumber = uiState.billNumber,
                            dateTime = uiState.formattedDateTime,
                            customerName = uiState.customerName,
                            customerPhone = uiState.customerPhone,
                            customerHouseNo = uiState.customerHouseNo,
                            items = uiState.items,
                            totalAmount = uiState.totalAmount
                        )
                    }
                },
                onShareClick = {
                    if (uiState.items.isEmpty()) {
                        Toast.makeText(context, "No items to share", Toast.LENGTH_SHORT).show()
                    } else {
                        ReceiptShareHelper.shareReceipt(
                            context = context,
                            storeName = uiState.storeName,
                            billNumber = uiState.billNumber,
                            dateTime = uiState.formattedDateTime,
                            customerName = uiState.customerName,
                            customerPhone = uiState.customerPhone,
                            customerHouseNo = uiState.customerHouseNo,
                            items = uiState.items,
                            totalAmount = uiState.totalAmount
                        )
                    }
                }
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        // Customer Details Edit Dialog
        if (uiState.showCustomerEditDialog) {
            CustomerEditDialog(
                initialName = uiState.customerName,
                initialPhone = uiState.customerPhone,
                initialHouseNo = uiState.customerHouseNo,
                onDismiss = { viewModel.dismissCustomerEdit() },
                onSave = { name, phone, houseNo ->
                    viewModel.saveCustomerInfo(name, phone, houseNo)
                }
            )
        }

        // Edit Choice Dialog (Speak or Manual)
        if (uiState.showEditOptionsDialog) {
            ReceiptEditChoiceDialog(
                onDismiss = { viewModel.dismissEditOptions() },
                onSelectSpeak = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        viewModel.onSelectEditSpeak()
                    } else {
                        viewModel.dismissEditOptions()
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSelectManual = { viewModel.onSelectEditManual() }
            )
        }

        // Manual Edit Sheet
        if (uiState.showManualEditSheet) {
            ManualEditReceiptSheet(
                items = uiState.items,
                onDismiss = { viewModel.dismissManualEditSheet() },
                onEditItem = { viewModel.openEditItem(it) },
                onDeleteItem = { viewModel.deleteItem(it) },
                onAddNewItem = { showManualAddDialog = true }
            )
        }

        // Single Item Edit Dialog
        uiState.editingItem?.let { itemToEdit ->
            EditItemDialog(
                item = itemToEdit,
                onDismiss = { viewModel.dismissEditItem() },
                onSave = { updated -> viewModel.updateItem(updated) }
            )
        }

        // Manual Add Item Dialog
        if (showManualAddDialog) {
            val dummyItem = BillItem(
                serialNumber = uiState.items.size + 1,
                itemName = "",
                weightOrQuantity = "",
                price = null
            )
            EditItemDialog(
                item = dummyItem,
                onDismiss = { showManualAddDialog = false },
                onSave = { newItem ->
                    if (newItem.itemName.isNotBlank()) {
                        viewModel.addNewItemDirectly(
                            itemName = newItem.itemName,
                            weightOrQty = newItem.weightOrQuantity.ifEmpty { "1 item" },
                            price = newItem.price
                        )
                    }
                    showManualAddDialog = false
                }
            )
        }
    }
}
