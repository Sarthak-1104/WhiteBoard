package com.sarthak.whiteboards.models

import kotlinx.serialization.Serializable

@Serializable
enum class ShapeType {
    RECTANGLE,
    CIRCLE,
    LINE,
    POLYGON
}
