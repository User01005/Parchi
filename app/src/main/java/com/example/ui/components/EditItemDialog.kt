package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BillItem
import com.example.parser.VoiceBillParser
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.MicRecordRed
import com.example.ui.theme.PriceGreen

@Composable
fun EditItemDialog(
    item: BillItem,
    onDismiss: () -> Unit,
    onSave: (BillItem) -> Unit,
    onVoiceClick: () -> Unit = {}
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var weightOrQuantity by remember { mutableStateOf(item.weightOrQuantity) }
    var priceText by remember {
        mutableStateOf(item.price?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("edit_item_dialog"),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Item #${item.serialNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "Edit by text or voice",
                            fontSize = 12.sp,
                            color = InkMuted
                        )
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

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name field
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name (e.g. Atta, Sugar)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_item_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5E3C),
                        focusedLabelColor = Color(0xFF8B5E3C)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Weight / Quantity field
                OutlinedTextField(
                    value = weightOrQuantity,
                    onValueChange = { weightOrQuantity = it },
                    label = { Text("Weight / Quantity (e.g. 10 kg, 500 gm, 2 pkts)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_weight_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5E3C),
                        focusedLabelColor = Color(0xFF8B5E3C)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price field (optional)
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price ₹ (Optional - leave empty if not known)") },
                    placeholder = { Text("e.g. 356 or leave blank") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PriceGreen,
                        focusedLabelColor = PriceGreen
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TactileButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        backgroundColor = Color(0xFFE2DED4),
                        textColor = InkBlack,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )

                    TactileButton(
                        text = "Save Item",
                        onClick = {
                            val priceDouble = priceText.trim().toDoubleOrNull()
                            val updated = item.copy(
                                itemName = itemName.trim().ifEmpty { item.itemName },
                                weightOrQuantity = weightOrQuantity.trim().ifEmpty { item.weightOrQuantity },
                                price = priceDouble
                            )
                            onSave(updated)
                        },
                        icon = Icons.Default.Check,
                        backgroundColor = Color(0xFF1E3A2F),
                        textColor = Color.White,
                        modifier = Modifier.weight(1.3f),
                        height = 48.dp,
                        testTag = "save_item_button"
                    )
                }
            }
        }
    }
}
