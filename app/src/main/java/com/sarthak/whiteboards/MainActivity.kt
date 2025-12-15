package com.sarthak.whiteboards

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel
import com.sarthak.whiteboards.views.WhiteboardScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val whiteboardViewModel: WhiteboardViewModel by viewModels()
        enableEdgeToEdge()
        setContent {
            WhiteboardScreen(whiteboardViewModel)
        }
    }
}