package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ReceiptPaperWhite

/**
 * Custom frame styled as a warm "wooden phone" enclosure that comfortably holds
 * the crisp white grocery bill receipt.
 */
@Composable
fun ReceiptPaperCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Outer wooden phone chassis
    val woodGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF9E6B47),
            Color(0xFF865432),
            Color(0xFF6F4325)
        )
    )

    val phoneFrameShape = RoundedCornerShape(32.dp)
    val paperShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = phoneFrameShape, ambientColor = Color(0x33442A16), spotColor = Color(0x44442A16))
            .clip(phoneFrameShape)
            .background(woodGradient)
            .border(2.dp, Color(0xFFB5835C), phoneFrameShape)
            .padding(10.dp) // Wooden border bezel
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wooden phone top speaker / camera capsule bar
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .width(54.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF482B17).copy(alpha = 0.75f))
            )

            // Crisp white paper receipt mounted in the wooden phone
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = paperShape,
                colors = CardDefaults.cardColors(
                    containerColor = ReceiptPaperWhite
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
