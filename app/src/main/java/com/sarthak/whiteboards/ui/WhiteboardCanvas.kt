package com.sarthak.whiteboards.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarthak.whiteboards.models.Point
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel

@Composable
fun WhiteboardCanvas(
    viewModel: WhiteboardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var shapePreviewEnd by remember { mutableStateOf<Point?>(null) }
    var showTextInput by remember { mutableStateOf(false) }
    var textInputPosition by remember { mutableStateOf<Point?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {

                detectDragGestures(
                    onDragStart = { offset ->
                        val point = Point(offset.x, offset.y)
                        when (viewModel.toolMode) {

                            ToolMode.DRAW -> viewModel.startStroke(point)
                            ToolMode.ERASE -> viewModel.eraseAt(point)
                            ToolMode.SHAPE -> viewModel.startShape(point)
                            ToolMode.TEXT -> {}
                            else -> {}
                        }
                    },

                    onDrag = { change, _ ->
                        val point = Point(change.position.x, change.position.y)
                        when (viewModel.toolMode) {

                            ToolMode.DRAW -> viewModel.continueStroke(point)
                            ToolMode.ERASE -> viewModel.eraseAt(point)
                            ToolMode.SHAPE -> shapePreviewEnd = point
                            ToolMode.TEXT -> {}
                            else -> {}
                        }
                    },

                    onDragEnd = {
                        when (viewModel.toolMode) {
                            ToolMode.DRAW -> viewModel.endStroke()
                            ToolMode.SHAPE -> {
                                shapePreviewEnd?.let { viewModel.endShape(it) }
                                shapePreviewEnd = null
                            }
                            ToolMode.TEXT -> {}
                            ToolMode.ERASE -> {}
                            else -> {}
                        }
                    }
                )
            }
            .pointerInput(Unit) {

                detectTapGestures { offset ->
                    if (viewModel.toolMode == ToolMode.TEXT) {
                        textInputPosition = Point(offset.x, offset.y)
                        showTextInput = true
                    }
                }
            }
    ) {

        state.strokes.forEach { stroke ->
            val path = Path()
            if (stroke.points.isNotEmpty()) {
                path.moveTo(stroke.points.first().x, stroke.points.first().y)
                for (p in stroke.points.drop(1)) {
                    path.lineTo(p.x, p.y)
                }
            }
            drawPath(
                path = path,
                color = Color(android.graphics.Color.parseColor(stroke.color)),
                style = Stroke(width = stroke.width.dp.toPx())
            )
        }


        state.shapes.forEach { shape ->
            val p1 = shape.points.first()
            val p2 = shape.points.last()
            val color = Color(android.graphics.Color.parseColor(shape.color))

            when (shape.type) {

                ShapeType.RECTANGLE -> drawRect(
                    color = color,
                    topLeft = Offset(p1.x, p1.y),
                    size = androidx.compose.ui.geometry.Size(
                        width = p2.x - p1.x,
                        height = p2.y - p1.y
                    ),
                    style = Stroke(width = 5f)
                )

                ShapeType.CIRCLE -> drawOval(
                    color = color,
                    topLeft = Offset(p1.x, p1.y),
                    size = androidx.compose.ui.geometry.Size(
                        width = p2.x - p1.x,
                        height = p2.y - p1.y
                    ),
                    style = Stroke(width = 5f)
                )

                ShapeType.LINE -> drawLine(
                    color = color,
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = 5f
                )

                ShapeType.POLYGON -> {
                    if (shape.points.size >= 3) {
                        val polyPath = Path().apply {
                            moveTo(shape.points[0].x, shape.points[0].y)
                            for (i in 1 until shape.points.size) {
                                lineTo(shape.points[i].x, shape.points[i].y)
                            }
                            close()
                        }
                        drawPath(polyPath, color, style = Stroke(width = 5f))
                    }
                }
            }
        }

        shapePreviewEnd?.let { end ->
            viewModel.getShapeStartPoint()?.let { start ->
                val color = Color.Gray.copy(alpha = 0.4f)

                when (viewModel.selectedShapeType) {
                    ShapeType.RECTANGLE -> drawRect(
                        color = color,
                        topLeft = Offset(start.x, start.y),
                        size = androidx.compose.ui.geometry.Size(
                            end.x - start.x,
                            end.y - start.y
                        ),
                        style = Stroke(width = 3f)
                    )

                    ShapeType.CIRCLE -> drawOval(
                        color = color,
                        topLeft = Offset(start.x, start.y),
                        size = androidx.compose.ui.geometry.Size(
                            end.x - start.x,
                            end.y - start.y
                        ),
                        style = Stroke(width = 3f)
                    )

                    ShapeType.LINE -> drawLine(
                        color = color,
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = 3f
                    )

                    else -> {}
                }
            }
        }

        drawIntoCanvas { canvas ->
            state.texts.forEach { txt ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(txt.color)
                    textSize = txt.size.sp.toPx()
                }
                canvas.nativeCanvas.drawText(
                    txt.text,
                    txt.position.x,
                    txt.position.y,
                    paint
                )
            }
        }
    }

    if (showTextInput && textInputPosition != null) {
        TextInputDialog(
            onDismiss = { showTextInput = false },
            onConfirm = { text ->
                viewModel.addText(text, textInputPosition!!)
                showTextInput = false
                textInputPosition = null
            }
        )
    }
}

@Composable
fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Text") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Enter text") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
