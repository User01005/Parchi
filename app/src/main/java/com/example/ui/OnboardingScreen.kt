package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PastelAmber
import com.example.ui.theme.PastelAmberAccent
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSkyAccent

@Composable
fun OnboardingScreen(
    onComplete: (businessName: String, category: String, phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    var businessName by remember { mutableStateOf("Manmohan Di Hatti") }
    var selectedCategory by remember { mutableStateOf("Kirana & Grocery") }
    var storePhone by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "Kirana & Grocery",
        "General Store",
        "Dairy & Sweets",
        "Fruits & Vegetables"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FB))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Brand Badge (Squircle)
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelMint)
                        .border(1.2.dp, PastelMintAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelMintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Parchi",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARCHI • पर्ची POS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (step == 0) {
                    // STEP 1: Introduction to PARCHI
                    Text(
                        text = "Voice Billing for Kirana & Retail",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bol kar pakki parchi banayein in seconds. Fast, accurate, and completely offline.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Feature Cards in Pastel Squircles
                    FeatureCard(
                        icon = Icons.Default.Mic,
                        title = "Continuous Voice Input",
                        desc = "Speak freely in Hindi, Hinglish, or English without pausing.",
                        bgColor = PastelRose,
                        accentColor = PastelRoseAccent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureCard(
                        icon = Icons.Default.Receipt,
                        title = "Smart Item & Price Extraction",
                        desc = "Auto-detects quantities (10 kg, 500 g, 2 pcs) and prices automatically.",
                        bgColor = PastelLavender,
                        accentColor = PastelLavenderAccent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureCard(
                        icon = Icons.Default.TrendingUp,
                        title = "Thermal Receipts & Daily Sales",
                        desc = "Print & WhatsApp shareable receipts. Track today's total store revenue.",
                        bgColor = PastelAmber,
                        accentColor = PastelAmberAccent
                    )
                } else {
                    // STEP 2: Business Profile Setup
                    Text(
                        text = "Setup Your Business",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This name will appear on all your receipts and at the top of your billing counter.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Business Name Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "BUSINESS / STORE NAME *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            placeholder = { Text("e.g. Manmohan Di Hatti or Gupta Kirana") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                                .testTag("onboarding_business_name_input"),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Store",
                                    tint = PastelLavenderAccent
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PastelLavenderAccent,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Store Category Selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STORE CATEGORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.chunked(2).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowList.forEach { category ->
                                        val isSelected = selectedCategory == category
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isSelected) PastelSky else Color.White)
                                                .border(
                                                    1.2.dp,
                                                    if (isSelected) PastelSkyAccent else Color(0xFFE2E8F0),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { selectedCategory = category }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = category,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) PastelSkyAccent else Color(0xFF334155),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Store Phone / WhatsApp (Optional)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STORE PHONE / WHATSAPP (OPTIONAL)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = storePhone,
                            onValueChange = { storePhone = it },
                            placeholder = { Text("e.g. 9876543210") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x14000000))
                                .testTag("onboarding_store_phone_input"),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PastelMintAccent,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
            }

            // Bottom CTA Button & Step indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step Dot Indicators (Squircle dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = if (step == 0) 24.dp else 10.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (step == 0) Color(0xFF0F172A) else Color(0xFFCBD5E1))
                    )
                    Box(
                        modifier = Modifier
                            .size(width = if (step == 1) 24.dp else 10.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (step == 1) Color(0xFF0F172A) else Color(0xFFCBD5E1))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (step == 0) {
                            step = 1
                        } else {
                            onComplete(businessName, selectedCategory, storePhone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x24000000))
                        .testTag("onboarding_continue_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (step == 0) "GET STARTED" else "START BILLING WITH PARCHI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step == 0) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                            contentDescription = "Proceed",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    bgColor: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0F000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }
    }
}
