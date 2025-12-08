package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
data class ShapeModel(
    val type: ShapeType,
    val points: List<Point>,
    val color: String
)
