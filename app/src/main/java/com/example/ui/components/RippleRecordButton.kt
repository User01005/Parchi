package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MicPulsingRed
import com.example.ui.theme.MicRecordRed

/**
 * Visual indicator for voice input featuring a prominent 'record' button with a
 * multi-ring ripple effect animation that signals when the app is actively listening for dictation.
 */
@Composable
fun RippleRecordButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    liveTranscript: String = "",
    isProcessing: Boolean = false,
    testTag: String = "ripple_record_button"
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "btn_press_scale"
    )

    // Infinite transitions for radiating ripple effect when listening
    val infiniteTransition = rememberInfiniteTransition(label = "voice_ripples")

    // First ripple wave (starts immediately)
    val ripple1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1_scale"
    )
    val ripple1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1_alpha"
    )

    // Second ripple wave (delayed phase)
    val ripple2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2_scale"
    )
    val ripple2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2_alpha"
    )

    // Third outer ripple wave (wider aura)
    val ripple3Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 3.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, delayMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_3_scale"
    )
    val ripple3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, delayMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_3_alpha"
    )

    // Center button pulsating scale when listening
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center_pulse"
    )

    val buttonBgColor by animateColorAsState(
        targetValue = if (isListening) MicPulsingRed else MicRecordRed,
        label = "btn_bg_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active listening status chip with pulsing dot
        if (isListening) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF1F2))
                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MicRecordRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveTranscript.isNotBlank()) "Hearing: \"$liveTranscript\"" else "Recording speech... Tap to stop & create bill",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MicPulsingRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Ripple Container
        Box(
            modifier = Modifier
                .size(90.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            // Expanding Ripple Rings (Visible only when actively listening)
            if (isListening) {
                // Wave 3 (widest ring)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ripple3Scale)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MicRecordRed.copy(alpha = ripple3Alpha))
                        .border(1.5.dp, Color(0xFFF43F5E).copy(alpha = ripple3Alpha), RoundedCornerShape(26.dp))
                )

                // Wave 2 (middle ring)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ripple2Scale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MicRecordRed.copy(alpha = ripple2Alpha))
                        .border(1.8.dp, Color(0xFFFB7185).copy(alpha = ripple2Alpha), RoundedCornerShape(24.dp))
                )

                // Wave 1 (inner core ring)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ripple1Scale)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MicRecordRed.copy(alpha = ripple1Alpha))
                        .border(2.dp, Color.White.copy(alpha = ripple1Alpha), RoundedCornerShape(22.dp))
                )
            }

            // Tactile 3D shadow under center button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .offset { IntOffset(0, 4.dp.roundToPx()) }
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            // Main Core Record Button (Squircle)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (isListening) pulseScale * buttonScale else buttonScale)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                buttonBgColor,
                                buttonBgColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        width = if (isListening) 3.dp else 2.dp,
                        color = if (isListening) Color(0xFFFFF1F2) else Color(0xFFFF8599),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Descriptive label below button
        Text(
            text = when {
                isProcessing -> "CREATING RECEIPT..."
                isListening -> "RECORDING AUDIO... TAP TO FINISH"
                else -> "TAP TO SPEAK (HINDI / ENG)"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = if (isListening) MicPulsingRed else Color(0xFF1E293B)
        )
    }
}
