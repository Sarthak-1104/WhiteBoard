package com.sarthak.whiteboards.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sarthak.whiteboards.models.Point
import com.sarthak.whiteboards.models.ShapeModel
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.StrokeModel
import com.sarthak.whiteboards.models.TextModel
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.models.WhiteboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WhiteboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WhiteboardState())
    val uiState: StateFlow<WhiteboardState> = _uiState

    var toolMode: ToolMode = ToolMode.NONE
    var currentColor by mutableStateOf("#000000")
    var currentStrokeWidth by mutableFloatStateOf(6f)
    var selectedShapeType: ShapeType = ShapeType.LINE

    private var currentStrokePoints = mutableListOf<Point>()
    private var shapeStartPoint: Point? = null


    fun startStroke(point: Point) {
        currentStrokePoints = mutableListOf(point)

        _uiState.update {
            it.copy(strokes = it.strokes + StrokeModel(
                points = listOf(point),
                color = currentColor,
                width = currentStrokeWidth
            ))
        }
    }

    fun continueStroke(point: Point) {
        currentStrokePoints.add(point)

        _uiState.update { currentState ->
            val updatedStrokes = currentState.strokes.toMutableList()
            if (updatedStrokes.isNotEmpty()) {
                updatedStrokes[updatedStrokes.lastIndex] = StrokeModel(
                    points = currentStrokePoints.toList(),
                    color = currentColor,
                    width = currentStrokeWidth
                )
            }
            currentState.copy(strokes = updatedStrokes)
        }
    }

    fun endStroke() {

        currentStrokePoints.clear()
    }


    fun eraseAt(point: Point) {
        val newStrokes = _uiState.value.strokes.mapNotNull { stroke ->
            val filtered = stroke.points.filter { p ->
                val dx = p.x - point.x
                val dy = p.y - point.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble())
                dist > 40
            }
            if (filtered.isEmpty()) null else stroke.copy(points = filtered)
        }
        _uiState.update { it.copy(strokes = newStrokes) }
    }

    fun startShape(point: Point) {
        shapeStartPoint = point
    }

    fun endShape(end: Point) {
        val start = shapeStartPoint ?: return
        val shape = ShapeModel(
            type = selectedShapeType,
            points = listOf(start, end),
            color = currentColor
        )

        _uiState.update { it.copy(shapes = it.shapes + shape) }
        shapeStartPoint = null
    }


    fun addText(
        text: String,
        position: Point,
        color: String = currentColor,
        size: Int = 24
    ) {
        val textModel = TextModel(
            text = text,
            position = position,
            color = color,
            size = size
        )

        _uiState.update { it.copy(texts = it.texts + textModel) }
    }


    fun getShapeStartPoint(): Point? = shapeStartPoint

    fun loadFromState(state: WhiteboardState) {
        _uiState.value = state
    }

    fun clearBoard() {
        _uiState.value = WhiteboardState()
    }
}
