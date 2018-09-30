package com.sano.testdrive.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class FinishedRoute(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0,

        @ColumnInfo(name= "date")
        val date: Date,

        @ColumnInfo(name = "predictions")
        val predictions: List<SimplePrediction>): Parcelable