package com.sarthak.whiteboards.services

import android.content.Context
import android.os.Environment
import com.sarthak.whiteboards.models.WhiteboardFile
import com.sarthak.whiteboards.models.WhiteboardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class WhiteBoardSavingServices @Inject constructor(
    private val context: Context
) {

    private val json = Json { prettyPrint = true }

    private val whiteboardDir: File
        get() {
            val downloads = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dir = File(downloads, "Whiteboards")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    suspend fun save(
        fileName: String,
        state: WhiteboardState
    ) = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(state)
        File(whiteboardDir, fileName).writeText(jsonString)
    }

    suspend fun getAll(): List<WhiteboardFile> =
        withContext(Dispatchers.IO) {
            whiteboardDir
                .listFiles { file -> file.extension == "json" }
                ?.map {
                    WhiteboardFile(
                        fileName = it.name,
                        lastModified = it.lastModified()
                    )
                }
                ?: emptyList()
        }

    suspend fun load(fileName: String): WhiteboardState =
        withContext(Dispatchers.IO) {
            val file = File(whiteboardDir, fileName)
            val jsonString = file.readText()
            json.decodeFromString(jsonString)
        }
}

