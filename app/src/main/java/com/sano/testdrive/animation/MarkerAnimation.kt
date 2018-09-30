/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

package com.example.latlnginterpolation

import android.location.Location
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.sano.testdrive.util.middlePoint

class MarkerAnimation {

    val speed = 10000 //meters per second
    val step = 16L //ms 60fps
    val handler = Handler()
    private var onCompleteListener: ((Unit) -> Unit)? = null

    fun animateMarker(marker: Marker, points: List<LatLng>) {
        val start = SystemClock.uptimeMillis()
        var lastPointIndex = 0
        var subDistance = 0f

        val pointInfo: ArrayList<FloatArray> = arrayListOf()

        var avgDistance = 0.0
        points.forEachIndexed { index, _ ->
            if (index == points.lastIndex) return@forEachIndexed

            val array = FloatArray(2)
            Location.distanceBetween(points[index].latitude,
                    points[index].longitude,
                    points[index + 1].latitude,
                    points[index + 1].longitude,
                    array)

            avgDistance += array[0]
            pointInfo.add(array)
        }

        val metersPerStep = speed / 1000 * step

        val pointsList: ArrayList<LatLng> = arrayListOf()
        val bearings: ArrayList<Float> = arrayListOf()

        var currentPointIndex = lastPointIndex

        outer@ while(true) {

            var distanceMeters: Float = metersPerStep.toFloat()

            while (true) {
                if (currentPointIndex == points.lastIndex) {
                    pointsList.add(points.last())
                    bearings.add(pointInfo.last()[1])
                    break@outer
                }

                val diff = pointInfo[currentPointIndex][0] - distanceMeters - subDistance

                if (diff > 0) {
                    subDistance += distanceMeters
                    break
                } else {
                    distanceMeters -= pointInfo[currentPointIndex][0] - subDistance
                    currentPointIndex++
                    subDistance = 0f
                }
            }

            val fraction = distanceMeters / pointInfo[currentPointIndex][0]

            lastPointIndex = currentPointIndex
            pointsList.add(middlePoint(fraction, points[lastPointIndex], points[lastPointIndex + 1]))
            bearings.add(pointInfo[lastPointIndex][1])
        }

        var i = 0

        val runnable = object : Runnable {
            override fun run() {
                if(pointsList.lastIndex == i) {
                    onCompleteListener?.invoke(Unit)
                    return
                }
                marker.position = pointsList[i]
                marker.rotation = bearings[i]

                i++

                handler.postDelayed(this, step)
            }
        }

        handler.post(runnable)
    }

    fun stopAnimation() {
        handler.removeCallbacksAndMessages(null)
    }

    fun setOnCompleteListener(listener: ((Unit) -> Unit)) {
        onCompleteListener = listener
    }
}