package com.sano.testdrive.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.sano.testdrive.model.FinishedRoute

@Dao
interface DriveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFinishedRoute(finishedRoute: FinishedRoute)

    @Query("select * from finishedroute")
    fun getFinishedRoutes(): List<FinishedRoute>
}