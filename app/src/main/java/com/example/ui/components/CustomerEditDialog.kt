package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted

@Composable
fun CustomerEditDialog(
    initialName: String,
    initialPhone: String,
    initialHouseNo: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, houseNo: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var houseNo by remember { mutableStateOf(initialHouseNo) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("customer_edit_dialog"),
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
                            text = "Customer Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "Fill details or leave empty for receipt space",
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

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name (Optional)") },
                    placeholder = { Text("e.g. Ramesh Sharma") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5E3C),
                        focusedLabelColor = Color(0xFF8B5E3C)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / Mobile Number (Optional)") },
                    placeholder = { Text("e.g. 9876543210") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5E3C),
                        focusedLabelColor = Color(0xFF8B5E3C)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // House Number / Address
                OutlinedTextField(
                    value = houseNo,
                    onValueChange = { houseNo = it },
                    label = { Text("House / Flat Number (Optional)") },
                    placeholder = { Text("e.g. Flat 302, B Block") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_house_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5E3C),
                        focusedLabelColor = Color(0xFF8B5E3C)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        text = "Update",
                        onClick = {
                            onSave(name.trim(), phone.trim(), houseNo.trim())
                        },
                        icon = Icons.Default.Check,
                        backgroundColor = Color(0xFF1E3A2F),
                        textColor = Color.White,
                        modifier = Modifier.weight(1.3f),
                        height = 48.dp,
                        testTag = "save_customer_button"
                    )
                }
            }
        }
    }
}
