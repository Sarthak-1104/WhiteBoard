package com.sarthak.whiteboards.models.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WhiteboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WhiteboardFileEntity)

    @Query("SELECT * FROM whiteboard_files ORDER BY fileName DESC")
    suspend fun getAll(): List<WhiteboardFileEntity>

    @Query("SELECT * FROM whiteboard_files WHERE fileName = :fileName")
    suspend fun getByName(fileName: String): WhiteboardFileEntity
}
