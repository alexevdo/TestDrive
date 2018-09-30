package com.sano.testdrive.main

import com.google.android.gms.maps.model.LatLng

interface MainView {
    fun showToast(text: String)
    fun addMarkerCar(position: LatLng)
    fun animateMarkerCar(routePoints: ArrayList<LatLng>)
    fun clearMap()
    fun addMarkers(points: List<LatLng>)
    fun moveCamera()
    fun stopAnimation()
    fun addPolylines(routePoints: java.util.ArrayList<LatLng>)
}