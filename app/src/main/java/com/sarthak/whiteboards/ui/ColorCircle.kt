package com.sarthak.whiteboards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp


@Composable
fun ColorCircle(color: Color, isSelected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(bottom = 5.dp)
            .size(40.dp)
            .background(if (isSelected) Color.LightGray else Color.White, CircleShape)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = CircleShape
            )
            .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(
                bounded = true,
                radius = 22.dp
            )
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .padding(6.dp)
                .background(color, CircleShape)
        )
    }
}

fun toHex(color: Color): String {
    return String.format("#%06X", (color.toArgb() and 0xFFFFFF))
}

