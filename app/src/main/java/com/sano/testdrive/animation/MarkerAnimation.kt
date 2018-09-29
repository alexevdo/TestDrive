/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

package com.example.latlnginterpolation

import android.location.Location
import android.os.Handler
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.sano.testdrive.util.middlePoint

class MarkerAnimation {

    val speed = 10000 //meters per second
    val handler = Handler()

    fun animateMarker(marker: Marker, points: List<LatLng>) {
        val start = SystemClock.uptimeMillis()
        var lastUpdate = start
        var lastPointIndex = 0
        var subDistance = 0f

        val pointInfo : ArrayList<FloatArray> = arrayListOf()
        points.forEachIndexed { index, latLng ->
            if(index == points.lastIndex) return@forEachIndexed

            val array = FloatArray(2)
            Location.distanceBetween(points[index].latitude,
                    points[index].longitude,
                    points[index + 1].latitude,
                    points[index + 1].longitude,
                    array)

            pointInfo.add(array)
        }

        val runnable = object : Runnable {
            override fun run() {
                val currentTime = SystemClock.uptimeMillis()
                val elapsed = currentTime - lastUpdate
                lastUpdate = currentTime
                var distanceMeters = speed * elapsed / 1000f

                var currentPointIndex = lastPointIndex

                while (true) {
                    if (currentPointIndex == points.lastIndex) {
                        marker.position = points.last()
                        return
                    }

                    val diff = pointInfo[currentPointIndex][0] - distanceMeters - subDistance

                    if(diff > 0) {
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
                marker.position = middlePoint(fraction, points[lastPointIndex], points[lastPointIndex + 1])
                marker.rotation = pointInfo[lastPointIndex][1]

                handler.postDelayed(this, 500)
            }
        }

        handler.post(runnable)
    }

    fun stopAnimation() {
        handler.removeCallbacksAndMessages(null)
    }
}