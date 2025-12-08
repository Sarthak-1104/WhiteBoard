package com.sarthak.whiteboards.views

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sarthak.whiteboards.ui.WhiteboardCanvas
import com.sarthak.whiteboards.ui.WhiteboardToolbar
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel

@Composable
fun WhiteboardScreen(viewModel: WhiteboardViewModel) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
        ) {
            WhiteboardCanvas(viewModel)
        }

        Box(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight()
        ) {
            WhiteboardToolbar(viewModel)
        }
    }
}
