package com.example.arise.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        HunterProfile::class,
        QuestEntity::class,
        SubObjectiveEntity::class,
        SystemLogEntryEntity::class,
        DailySnapshot::class,
        PenaltySessionEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AriseDatabase : RoomDatabase() {
    abstract fun hunterDao(): HunterDao
    abstract fun questDao(): QuestDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun penaltySessionDao(): PenaltySessionDao
}
