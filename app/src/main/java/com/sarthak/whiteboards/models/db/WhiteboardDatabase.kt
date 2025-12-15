package com.sarthak.whiteboards.models.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WhiteboardFileEntity::class], version = 1)
abstract class WhiteboardDatabase : RoomDatabase() {
    abstract fun dao(): WhiteboardDao
}
