package com.sarthak.whiteboards.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sarthak.whiteboards.ui.SavedFileDrawer
import com.sarthak.whiteboards.ui.WhiteboardCanvas
import com.sarthak.whiteboards.ui.WhiteboardToolbar
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel

@Composable
fun WhiteboardScreen(viewModel: WhiteboardViewModel) {
    var showFileDrawer by remember { mutableStateOf(false) }
    val savedFiles by viewModel.savedFiles.collectAsState()

    LaunchedEffect(showFileDrawer) {
        if (showFileDrawer) {
            viewModel.refreshSavedFiles()
        }
    }

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

        if (!showFileDrawer) {
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxHeight()
            ) {
                WhiteboardToolbar(viewModel) { newValue -> showFileDrawer = newValue }
            }
        }

        if (showFileDrawer) {
            SavedFileDrawer(
                whiteboardFiles = savedFiles,
                isVisible = showFileDrawer,
                onFileClick = { fileName ->
                    viewModel.loadFile(fileName)
                },
                onDismiss = {
                    showFileDrawer = false
                }
            )
        }
    }
}
