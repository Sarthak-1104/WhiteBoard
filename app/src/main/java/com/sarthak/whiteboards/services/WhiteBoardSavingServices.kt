package com.sarthak.whiteboards.services

import com.sarthak.whiteboards.models.WhiteboardState
import com.sarthak.whiteboards.models.db.WhiteboardDao
import com.sarthak.whiteboards.models.db.WhiteboardFileEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WhiteBoardSavingServices @Inject constructor(private val dao: WhiteboardDao) {

    private val json = Json { prettyPrint = true }

    suspend fun save(state: WhiteboardState) {
        val timestamp = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "whiteboard_${formatter.format(Date(timestamp))}.json"

        val jsonString = json.encodeToString(state)

        dao.insert(
            WhiteboardFileEntity(fileName = fileName, json = jsonString)
        )
    }

    suspend fun getAll(): List<WhiteboardFileEntity> =
        dao.getAll()

    suspend fun load(fileName: String): WhiteboardState {
        val entity = dao.getByName(fileName)
        return json.decodeFromString(entity.json)
    }
}
