package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom controls for Receipt Screen.
 * When not finished: strictly displays ONLY the single Record button with ripple effect.
 * When finished: presents the sleek pill navigation options (Back, Edit, Print, Share).
 */
@Composable
fun ReceiptBottomBar(
    isReceiptFinished: Boolean,
    isListening: Boolean,
    liveTranscript: String,
    onRecordClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isReceiptFinished || isListening || isProcessing) {
            // STATE 1: Before / While Recording / While Processing
            // Single Record Button with ripple indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RippleRecordButton(
                    isListening = isListening,
                    liveTranscript = liveTranscript,
                    isProcessing = isProcessing,
                    onClick = onRecordClick,
                    modifier = Modifier.testTag("receipt_record_button")
                )
            }
        } else {
            // STATE 2: Receipt Completed
            // Sleek bottom pill navigation options matching reference design
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary mic pill if they wish to dictate more
                if (isListening) {
                    RippleRecordButton(
                        isListening = true,
                        liveTranscript = liveTranscript,
                        onClick = onRecordClick,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .testTag("receipt_record_active")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Pill Button (< BACK)
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.2.dp, Color(0xFFE4E4E7), RoundedCornerShape(24.dp))
                            .clickable { onBackClick() }
                            .padding(horizontal = 12.dp)
                            .testTag("pill_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF18181B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BACK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF18181B)
                            )
                        }
                    }

                    // Edit Pill Button (EDIT -> Speak or Manual)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.2.dp, Color(0xFFE4E4E7), RoundedCornerShape(24.dp))
                            .clickable { onEditClick() }
                            .padding(horizontal = 12.dp)
                            .testTag("pill_edit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF18181B),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EDIT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF18181B)
                            )
                        }
                    }

                    // Print Pill Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.2.dp, Color(0xFFE4E4E7), RoundedCornerShape(24.dp))
                            .clickable { onPrintClick() }
                            .padding(horizontal = 12.dp)
                            .testTag("pill_print_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print",
                                tint = Color(0xFF18181B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRINT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF18181B)
                            )
                        }
                    }

                    // Share Pill Button (Black solid pill from reference design)
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x28000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF18181B))
                            .clickable { onShareClick() }
                            .padding(horizontal = 14.dp)
                            .testTag("pill_share_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHARE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
