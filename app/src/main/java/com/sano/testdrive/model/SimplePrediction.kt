package com.sano.testdrive.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SimplePrediction(
        val text: String,
        val placeId: String) : Parcelable