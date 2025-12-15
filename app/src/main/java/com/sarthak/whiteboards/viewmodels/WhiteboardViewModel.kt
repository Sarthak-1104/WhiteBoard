package com.sarthak.whiteboards.viewmodels

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.whiteboards.models.Point
import com.sarthak.whiteboards.models.ShapeModel
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.StrokeModel
import com.sarthak.whiteboards.models.TextModel
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.models.WhiteboardState
import com.sarthak.whiteboards.models.db.WhiteboardFileEntity
import com.sarthak.whiteboards.services.WhiteBoardSavingServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhiteboardViewModel @Inject constructor(private val storageServices: WhiteBoardSavingServices): ViewModel() {

    private val _uiState = MutableStateFlow(WhiteboardState())
    val uiState: StateFlow<WhiteboardState> = _uiState

    var toolMode: ToolMode = ToolMode.NONE
    var currentColor by mutableStateOf("#000000")
    var currentStrokeWidth by mutableFloatStateOf(6f)
    var currentEraserStrokeWidth by mutableFloatStateOf(6f)
    var selectedShapeType: ShapeType = ShapeType.LINE

    private var currentStrokePoints = mutableListOf<Point>()
    var currentEraserPosition = mutableStateOf<Point?>(null)
        private set
    private var shapeStartPoint: Point? = null
    private val undoStack = mutableListOf<WhiteboardState>()
    private val redoStack = mutableListOf<WhiteboardState>()

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
        saveCurrentStateToUndoStack()
    }

    fun startShape(point: Point) {
        shapeStartPoint = point
    }

    fun endShape(end: Point) {
        val start = shapeStartPoint ?: return
        val shape = ShapeModel(
            type = selectedShapeType,
            topLeft = start,
            bottomRight = end,
            color = currentColor
        )
        _uiState.update { it.copy(shapes = it.shapes + shape) }
        shapeStartPoint = null
        saveCurrentStateToUndoStack()
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
        saveCurrentStateToUndoStack()
    }


    fun getShapeStartPoint(): Point? = shapeStartPoint

    fun loadFromState(state: WhiteboardState) {
        _uiState.value = state
        undoStack.clear()
        redoStack.clear()
        undoStack.add(state)
    }

    fun clearBoard() {
        _uiState.value = WhiteboardState()
    }

    private fun saveCurrentStateToUndoStack() {
        redoStack.clear()
        undoStack.add(_uiState.value)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastState = undoStack.removeLast()
            redoStack.add(lastState)
            _uiState.value = if (undoStack.isNotEmpty()) {
                undoStack.last()
            } else {
                WhiteboardState()
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val redoState = redoStack.removeLast()
            undoStack.add(redoState)
            _uiState.value = redoState
        }
    }

    fun saveCurrentBoard() {
        viewModelScope.launch {
            storageServices.save(_uiState.value)
        }
    }

    fun getSavedFiles(): Flow<List<WhiteboardFileEntity>> = flow {
        emit(storageServices.getAll())
    }

    fun loadFile(fileName: String) {
        viewModelScope.launch {
            val state = storageServices.load(fileName)
            loadFromState(state)
        }
    }
}
