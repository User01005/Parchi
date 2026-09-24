package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 3D Thermal Printer Dispenser based on the reference design.
 * Features the realistic printer opening slot from which the crisp paper receipt feeds out.
 */
@Composable
fun ThermalPrinterDispenser(
    modifier: Modifier = Modifier,
    paperContent: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Printer Slot Machine Bar at the top
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(28.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x33000000))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF141416),
                            Color(0xFF1F2024),
                            Color(0xFF0F0F11)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF2E3036), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Inner hollow dark dispenser slit
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF050505))
                    .border(0.5.dp, Color(0xFF3F3F46), RoundedCornerShape(4.dp))
            )
        }

        // Receipt Paper coming out of the slot (overlapping by -8.dp to appear feeding from the slot)
        Box(
            modifier = Modifier
                .offset(y = (-8).dp)
                .fillMaxWidth(0.88f)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    ambientColor = Color(0x26000000),
                    spotColor = Color(0x33000000)
                )
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 18.dp, end = 18.dp, bottom = 12.dp)
            ) {
                paperContent()

                Spacer(modifier = Modifier.height(14.dp))

                // Serrated thermal paper tear edge at the bottom
                ThermalPaperTearEdge(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
            }
        }
    }
}

/**
 * Zig-zag perforated serrated cut at the bottom of thermal receipts.
 */
@Composable
fun ThermalPaperTearEdge(
    modifier: Modifier = Modifier,
    teethCount: Int = 26,
    color: Color = Color(0xFFF1F0EC)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val toothWidth = width / teethCount

        val path = Path().apply {
            moveTo(0f, 0f)
            for (i in 0 until teethCount) {
                val startX = i * toothWidth
                val midX = startX + (toothWidth / 2f)
                val endX = (i + 1) * toothWidth

                lineTo(midX, height)
                lineTo(endX, 0f)
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}
