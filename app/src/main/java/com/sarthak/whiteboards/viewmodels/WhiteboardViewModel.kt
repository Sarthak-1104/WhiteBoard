package com.sarthak.whiteboards.viewmodels

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.whiteboards.models.Point
import com.sarthak.whiteboards.models.ShapeModel
import com.sarthak.whiteboards.models.ShapeType
import com.sarthak.whiteboards.models.StrokeModel
import com.sarthak.whiteboards.models.TextModel
import com.sarthak.whiteboards.models.ToolMode
import com.sarthak.whiteboards.models.WhiteboardFile
import com.sarthak.whiteboards.models.WhiteboardState
import com.sarthak.whiteboards.services.WhiteBoardSavingServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    private val _savedFiles = MutableStateFlow<List<WhiteboardFile>>(emptyList())
    val savedFiles: StateFlow<List<WhiteboardFile>> = _savedFiles

    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var graphicsLayer: GraphicsLayer? = null

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
            val timestamp = System.currentTimeMillis()
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "whiteboard_${formatter.format(Date(timestamp))}.json"

            storageServices.save(
                fileName = fileName,
                state = _uiState.value
            )
        }
    }


    fun getSavedFiles(): Flow<List<WhiteboardFile>> =
        flow { emit(storageServices.getAll()) }
            .flowOn(Dispatchers.IO)


    fun loadFile(fileName: String) {
        viewModelScope.launch {
            val state = storageServices.load(fileName)
            loadFromState(state)
        }
    }

    fun refreshSavedFiles() {
        viewModelScope.launch {
            _savedFiles.value = storageServices.getAll()
        }
    }

    fun exportAndShareCanvas(context: Context) {
        viewModelScope.launch {
            val layer = graphicsLayer ?: return@launch

            try {
                val bitmap = layer.toImageBitmap().asAndroidBitmap()

                val filename = "Whiteboard_${System.currentTimeMillis()}.png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                uri?.let { targetUri ->
                    resolver.openOutputStream(targetUri).use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream!!)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(targetUri, values, null, null)
                    }

                    shareImage(context, targetUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun shareImage(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Whiteboard via")
        context.startActivity(chooser)
    }

}
