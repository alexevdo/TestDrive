package com.sano.testdrive

import android.app.Application
import android.arch.persistence.room.Room
import com.sano.testdrive.database.DriveDatabase

class DriveApplication : Application() {

    private lateinit var database: DriveDatabase

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, DriveDatabase::class.java, "drive_database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    fun getDriveDao() = database.getDriveDao()
}