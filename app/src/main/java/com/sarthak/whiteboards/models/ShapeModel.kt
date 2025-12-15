package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
data class ShapeModel(
    val type: ShapeType,
    val topLeft: Point,
    val bottomRight: Point,
    val color: String
)
