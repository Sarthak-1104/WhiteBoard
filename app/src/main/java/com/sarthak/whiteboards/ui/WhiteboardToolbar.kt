package com.sarthak.whiteboards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sarthak.whiteboards.R
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel

@Composable
fun WhiteboardToolbar(viewModel: WhiteboardViewModel, showFileDrawerState: (Boolean) -> Unit) {

    val scrollState = rememberScrollState()
    var selectedTool by remember { mutableStateOf(ToolMode.NONE) }
    var selectedShape by remember { mutableStateOf(ShapeType.LINE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF0F0F0))
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //TOOLS
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 5.dp)) {
            Text("Tools", style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(10.dp))

            ToolbarButton(R.drawable.free_hand_ic,
                selectedTool == ToolMode.NONE) {
                selectedTool = ToolMode.NONE
                viewModel.toolMode = ToolMode.NONE
            }

            ToolbarButton(R.drawable.brush_ic,
                selectedTool == ToolMode.DRAW) {
                selectedTool = ToolMode.DRAW
                viewModel.toolMode = ToolMode.DRAW
            }

            ToolbarButton(R.drawable.eraser_ic,
                selectedTool == ToolMode.ERASE) {
                selectedTool = ToolMode.ERASE
                viewModel.toolMode = ToolMode.ERASE
            }

            ToolbarButton(R.drawable.shapes_ic,
                selectedTool == ToolMode.SHAPE) {
                selectedTool = ToolMode.SHAPE
                viewModel.toolMode = ToolMode.SHAPE
            }

            ToolbarButton(R.drawable.text_ic,
                selectedTool == ToolMode.TEXT) {
                selectedTool = ToolMode.TEXT
                viewModel.toolMode = ToolMode.TEXT
            }

        }

        //STROKE
        if (viewModel.toolMode == ToolMode.DRAW || viewModel.toolMode == ToolMode.ERASE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 5.dp)) {
                Text("Stroke", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                Slider(
                    value = when(viewModel.toolMode){
                        ToolMode.DRAW -> viewModel.currentStrokeWidth
                        ToolMode.ERASE -> viewModel.currentEraserStrokeWidth
                        else -> {viewModel.currentStrokeWidth}
                    },
                    onValueChange = { when(viewModel.toolMode){
                        ToolMode.DRAW -> viewModel.currentStrokeWidth = it
                        ToolMode.ERASE -> viewModel.currentEraserStrokeWidth = it
                        else -> {viewModel.currentStrokeWidth = it}
                    }  },
                    valueRange = 3f..30f,
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .height(40.dp)
                )
            }
        }

        //COLORS Palettes
        if (viewModel.toolMode != ToolMode.ERASE && viewModel.toolMode != ToolMode.NONE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 5.dp)) {
                Text("Colors", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))

                val colors = listOf(
                    Color.Black, Color.Red, Color.Blue,
                    Color.Green, Color.Yellow, Color.Magenta
                )
                colors.forEach { color ->
                    ColorCircle(color, viewModel.currentColor == toHex(color)) {
                        viewModel.currentColor = toHex(color)
                    }
                }
            }
        }

        //SHAPES
        if (viewModel.toolMode == ToolMode.SHAPE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 5.dp)) {
                Text("Shapes", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))

                ToolbarButton(R.drawable.line_ic,
                    selectedShape == ShapeType.LINE) {
                    selectedShape = ShapeType.LINE
                    viewModel.selectedShapeType = ShapeType.LINE
                }
                ToolbarButton(R.drawable.circle_ic,
                    selectedShape == ShapeType.CIRCLE) {
                    selectedShape = ShapeType.CIRCLE
                    viewModel.selectedShapeType = ShapeType.CIRCLE
                }
                ToolbarButton(R.drawable.square_ic,
                    selectedShape == ShapeType.RECTANGLE) {
                    selectedShape = ShapeType.RECTANGLE
                    viewModel.selectedShapeType = ShapeType.RECTANGLE
                }
                ToolbarButton(R.drawable.pentagon_ic,
                    selectedShape == ShapeType.POLYGON) {
                    selectedShape = ShapeType.POLYGON
                    viewModel.selectedShapeType = ShapeType.POLYGON
                }
            }
        }

        // CLEAR BUTTON
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 5.dp)) {
            Text("Clear", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(10.dp))
            ToolbarButton(R.drawable.delete_ic) {
                viewModel.clearBoard()
            }
        }

        //UNDO/REDO BUTTON
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 5.dp)) {
            Text("Undo/Redo", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(10.dp))
            ToolbarButton(R.drawable.undo_ic) {
                viewModel.undo()
            }
            ToolbarButton(R.drawable.redo_ic) {
                viewModel.redo()
            }
        }

        //OPEN/SAVE FILE
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 5.dp)) {
            Text("Open", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(10.dp))
            ToolbarButton(R.drawable.file_open_ic) {
                showFileDrawerState(true)
            }
            ToolbarButton(R.drawable.save_file_ic) {
                viewModel.saveCurrentBoard()
            }
        }

    }

}

