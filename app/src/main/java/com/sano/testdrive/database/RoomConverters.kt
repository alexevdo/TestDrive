package com.sano.testdrive.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sano.testdrive.model.SimplePrediction
import java.util.*

class RoomConverters {

    private val gson = Gson()

    @TypeConverter
    fun stringToSimplePredictionList(data: String?): List<SimplePrediction> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<SimplePrediction>>() {}.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun simplePredictionListToString(someObjects: List<SimplePrediction>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun longToDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToLong(value: Date?): Long? {
        return value?.time
    }
}