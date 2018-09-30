package com.sano.testdrive.main

import android.os.AsyncTask
import com.directions.route.*
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.maps.model.LatLng
import com.sano.testdrive.database.DriveDao
import com.sano.testdrive.model.FinishedRoute
import com.sano.testdrive.model.SimplePrediction
import java.util.*

class MainPresenter(private val view: MainView,
                    private val driveDao: DriveDao,
                    private val geoDataClient: GeoDataClient,
                    private val mapKey: String) : RoutingListener {

    private val routePoints: ArrayList<LatLng> = arrayListOf()
    private var markerPoints: List<LatLng> = arrayListOf()
    private var routingTask: AsyncTask<Void, Void, ArrayList<Route>>? = null

    fun onRouteStartClick() {
        if (routePoints.isEmpty()) {
            view.showToast("Route is empty")
        } else {
            view.addMarkerCar(routePoints[0])
            view.animateMarkerCar(routePoints)
        }
    }

    fun onCarAnimationComplete(placeIds: List<SimplePrediction>) {
        view.showToast("Route finished, stored in history")
        val route = FinishedRoute(date = Calendar.getInstance().time, predictions = placeIds)
        driveDao.insertFinishedRoute(route)
    }

    fun userPredictionsUpdate(predictions: List<SimplePrediction>) {
        routePoints.clear()
        view.clearMap()

        if (predictions.isEmpty()) return

        val placeIds: List<String> = predictions.map { it.placeId }

        geoDataClient
                .getPlaceById(*placeIds.toTypedArray())
                .addOnCompleteListener {
                    markerPoints = DataBufferUtils.freezeAndClose(it.result).map { it.latLng }

                    view.addMarkers(markerPoints)
                    view.moveCamera()
                    view.stopAnimation()

                    if (markerPoints.size < 2) return@addOnCompleteListener

                    routingTask = Routing.Builder()
                            .key(mapKey)
                            .travelMode(AbstractRouting.TravelMode.DRIVING)
                            .withListener(this)
                            .waypoints(markerPoints)
                            .build()
                            .execute()
                }
    }

    override fun onRoutingCancelled() {
        view.showToast("Routing canceled")
    }

    override fun onRoutingStart() {

    }

    override fun onRoutingFailure(exception: RouteException?) {
        view.showToast("Routing fail: ${exception?.message}")
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, p1: Int) {
        if (route == null || route.isEmpty()) {
            view.showToast("Routing fail: empty route")
            return
        }

        routePoints.addAll(route.first().points)
        view.addPolylines(routePoints)
        view.showToast("Route: distance - " + route[0].distanceValue + ": duration - " + route[0].durationValue)
    }

    fun onStop() {
        routingTask?.cancel(true)
    }
}