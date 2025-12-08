package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
data class StrokeModel(
    val points: List<Point>,
    val color: String,
    val width: Float
)
