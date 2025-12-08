package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
data class TextModel(
    val text: String,
    val position: Point,
    val color: String,
    val size: Int
)
