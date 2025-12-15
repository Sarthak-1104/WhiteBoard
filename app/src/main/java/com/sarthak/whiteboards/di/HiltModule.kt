package com.sarthak.whiteboards.di

import android.content.Context
import androidx.room.Room
import com.sarthak.whiteboards.models.db.WhiteboardDao
import com.sarthak.whiteboards.models.db.WhiteboardDatabase
import com.sarthak.whiteboards.services.WhiteBoardSavingServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WhiteboardDatabase =
        Room.databaseBuilder(
            context,
            WhiteboardDatabase::class.java,
            "whiteboard_db"
        ).build()

    @Provides
    fun provideDao(db: WhiteboardDatabase): WhiteboardDao =
        db.dao()

    @Provides
    @Singleton
    fun provideWhiteboardSavingService(
        dao: WhiteboardDao
    ): WhiteBoardSavingServices =
        WhiteBoardSavingServices(dao)
}