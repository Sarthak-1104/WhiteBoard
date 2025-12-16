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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarthak.whiteboards.models.Point
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.viewmodels.WhiteboardViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WhiteboardCanvas(
    viewModel: WhiteboardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var shapePreviewEnd by remember { mutableStateOf<Point?>(null) }
    var showTextInput by remember { mutableStateOf(false) }
    var textInputPosition by remember { mutableStateOf<Point?>(null) }

    val graphicsLayer = rememberGraphicsLayer()
    LaunchedEffect(graphicsLayer) { viewModel.graphicsLayer = graphicsLayer }

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
                            ToolMode.ERASE -> viewModel.startEraser(point)
                            ToolMode.SHAPE -> viewModel.startShape(point)
                            ToolMode.TEXT -> {}
                            ToolMode.NONE -> {}
                            else -> {}
                        }
                    },

                    onDrag = { change, _ ->
                        val point = Point(change.position.x, change.position.y)
                        when (viewModel.toolMode) {

                            ToolMode.DRAW -> viewModel.continueStroke(point)
                            ToolMode.ERASE -> viewModel.updateEraser(point)
                            ToolMode.SHAPE -> shapePreviewEnd = point
                            ToolMode.TEXT -> {}
                            ToolMode.NONE -> {}
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
                            ToolMode.ERASE -> {viewModel.endEraser()}
                            else -> {}
                        }
                    }
                )
            }
            .pointerInput(Unit) {

                detectTapGestures { offset ->
                    when(viewModel.toolMode) {
                        ToolMode.TEXT -> {
                            textInputPosition = Point(offset.x, offset.y)
                            showTextInput = true
                        }
                        else -> {}
                    }
                }
            }
            .graphicsLayer {
                scaleX = viewModel.scale
                scaleY = viewModel.scale
                translationX = viewModel.offset.x
                translationY = viewModel.offset.y
                viewModel.graphicsLayer = graphicsLayer
            }
            .drawWithContent {
                graphicsLayer?.record {
                    drawRect(Color.White, size = size)
                    this@drawWithContent.drawContent()
                }
                drawContent()
            }
    ) {


        drawIntoCanvas { canvas ->
            val checkPoint = canvas.nativeCanvas.saveLayer(0f, 0f, size.width, size.height, null)


        viewModel.currentEraserPosition.value?.let { pos ->

            drawCircle(
                color = Color.White,
                radius = (viewModel.currentEraserStrokeWidth/2).dp.toPx(),
                center = Offset(pos.x, pos.y)
            )
            drawCircle(
                color = Color.Black,
                radius = (viewModel.currentEraserStrokeWidth/2).dp.toPx(),
                center = Offset(pos.x, pos.y),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        state.shapes.forEach { shape ->
            val color = Color(android.graphics.Color.parseColor(shape.color))

            when (shape.type) {
                ShapeType.RECTANGLE -> drawRect(
                    color = color,
                    topLeft = Offset(shape.topLeft.x, shape.topLeft.y),
                    size = androidx.compose.ui.geometry.Size(
                        width = shape.bottomRight.x - shape.topLeft.x,
                        height = shape.bottomRight.y - shape.topLeft.y
                    ),
                    style = Stroke(width = 5f)
                )

                ShapeType.CIRCLE -> drawOval(
                    color = color,
                    topLeft = Offset(shape.topLeft.x, shape.topLeft.y),
                    size = androidx.compose.ui.geometry.Size(
                        width = shape.bottomRight.x - shape.topLeft.x,
                        height = shape.bottomRight.y - shape.topLeft.y
                    ),
                    style = Stroke(width = 5f)
                )

                ShapeType.LINE -> drawLine(
                    color = color,
                    start = Offset(shape.topLeft.x, shape.topLeft.y),
                    end = Offset(shape.bottomRight.x, shape.bottomRight.y),
                    strokeWidth = 5f
                )

                ShapeType.POLYGON -> {
                    val left = shape.topLeft.x
                    val top = shape.topLeft.y
                    val width = shape.bottomRight.x - shape.topLeft.x
                    val height = shape.bottomRight.y - shape.topLeft.y
                    val centerX = left + width / 2
                    val centerY = top + height / 2

                    val pentagonPath = Path().apply {
                        val radiusX = width / 2 * 0.85f
                        val radiusY = height / 2 * 0.85f

                        for (i in 0..4) {
                            val angle = (i * 72f - 90f) * Math.PI.toFloat() / 180f
                            val x = centerX + cos(angle) * radiusX
                            val y = centerY + sin(angle) * radiusY
                            if (i == 0) moveTo(x, y)
                            else lineTo(x, y)
                        }
                        close()
                    }

                    drawPath(
                        path = pentagonPath,
                        color = color,
                        style = Stroke(width = 5f)
                    )
                }
            }
        }


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

            state.strokes.forEach { stroke ->
                val path = Path()
                if (stroke.points.isNotEmpty()) {
                    path.moveTo(stroke.points.first().x, stroke.points.first().y)
                    for (p in stroke.points.drop(1)) {
                        path.lineTo(p.x, p.y)
                    }
                }
                if (stroke.color == "ERASER") {
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = stroke.width.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        blendMode = BlendMode.Clear
                    )
                } else {
                    drawPath(
                        path = path,
                        color = Color(android.graphics.Color.parseColor(stroke.color)),
                        style = Stroke(
                            width = stroke.width.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
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

                        ShapeType.POLYGON -> drawPentagonPreview(
                            color = color,
                            start = Offset(start.x, start.y),
                            end = Offset(end.x, end.y),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            canvas.nativeCanvas.restoreToCount(checkPoint)
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

fun DrawScope.drawPentagonPreview(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    val left = minOf(start.x, end.x)
    val top = minOf(start.y, end.y)
    val width = abs(end.x - start.x)
    val height = abs(end.y - start.y)
    val centerX = left + width / 2
    val centerY = top + height / 2

    val pentagonPath = Path().apply {
        val radiusX = width / 2 * 0.75f
        val radiusY = height / 2 * 0.75f

        for (i in 0..4) {
            val angle = (i * 72f - 90f) * Math.PI.toFloat() / 180f
            val x = centerX + cos(angle) * radiusX
            val y = centerY + sin(angle) * radiusY
            if (i == 0) moveTo(x, y)
            else lineTo(x, y)
        }
        close()
    }

    drawPath(pentagonPath, color, style = Stroke(width = strokeWidth))
}


