package com.sano.testdrive.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class FinishedRoute(val date: Date, val placeIds: List<String>, val name: String): Parcelable