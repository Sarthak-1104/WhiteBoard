package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
data class WhiteboardState(
    val strokes: List<StrokeModel> = emptyList(),
    val shapes: List<ShapeModel> = emptyList(),
    val texts: List<TextModel> = emptyList()
)