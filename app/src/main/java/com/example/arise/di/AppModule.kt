package com.example.arise.di

import android.content.Context
import androidx.room.Room
import com.example.arise.data.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AriseDatabase {
        return Room.databaseBuilder(
            context,
            AriseDatabase::class.java,
            "arise_database"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideHunterDao(db: AriseDatabase): HunterDao = db.hunterDao()

    @Provides
    fun provideQuestDao(db: AriseDatabase): QuestDao = db.questDao()

    @Provides
    fun provideSystemLogDao(db: AriseDatabase): SystemLogDao = db.systemLogDao()

    @Provides
    fun provideSnapshotDao(db: AriseDatabase): SnapshotDao = db.snapshotDao()

    @Provides
    fun providePenaltySessionDao(db: AriseDatabase): PenaltySessionDao = db.penaltySessionDao()
}
