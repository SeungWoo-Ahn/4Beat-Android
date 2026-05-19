package com.fourbeat.data.database.di

import android.content.Context
import androidx.room.Room
import com.fourbeat.data.database.FourBeatDatabase
import com.fourbeat.data.database.dao.PostDao
import com.fourbeat.data.database.dao.SlotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFourBeatDatabase(@ApplicationContext context: Context): FourBeatDatabase =
        Room.databaseBuilder(
            context,
            FourBeatDatabase::class.java,
            "four-beat.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePostDao(db: FourBeatDatabase): PostDao = db.postDao()

    @Provides
    fun provideSlotDao(db: FourBeatDatabase): SlotDao = db.slotDao()
}
