package com.sano.testdrive.util

import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.sano.testdrive.model.SimplePrediction

val MARKER_COLORS = listOf(
        BitmapDescriptorFactory.HUE_AZURE,
        BitmapDescriptorFactory.HUE_BLUE,
        BitmapDescriptorFactory.HUE_RED,
        BitmapDescriptorFactory.HUE_ORANGE,
        BitmapDescriptorFactory.HUE_VIOLET)

fun middlePoint(fraction: Float, a: LatLng, b: LatLng): LatLng {
    val lat = (b.latitude - a.latitude) * fraction + a.latitude
    val lng = (b.longitude - a.longitude) * fraction + a.longitude
    return LatLng(lat, lng)
}

fun AutocompletePrediction.toSimplePrediction(): SimplePrediction? {
    val fullText = getFullText(null)
    val placeId = placeId

    return if (!fullText.isNullOrEmpty() && placeId != null) {
        SimplePrediction(fullText, placeId)
    } else null
}