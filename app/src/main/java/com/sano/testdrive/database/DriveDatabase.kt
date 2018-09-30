package com.sano.testdrive.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.sano.testdrive.model.FinishedRoute

@Database(entities = [FinishedRoute::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class DriveDatabase : RoomDatabase() {
    abstract fun getDriveDao(): DriveDao
}