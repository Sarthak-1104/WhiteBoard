package com.sarthak.whiteboards.models.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whiteboard_files")
data class WhiteboardFileEntity(
    @PrimaryKey
    val fileName: String,
    val json: String
)

