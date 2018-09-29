package com.sano.testdrive

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.directions.route.*
import com.example.latlnginterpolation.MarkerAnimation
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sano.testdrive.model.FinishedRoute
import com.sano.testdrive.util.MARKER_COLORS
import com.sano.testdrive.view.PlaceAutocompleteAdapter
import com.sano.testdrive.view.WaypointsAdapter
import kotlinx.android.synthetic.main.fragment_maps.*
import org.jetbrains.anko.support.v4.toast

class MapFragment : BaseMapFragment(), RoutingListener {

    companion object {
        private const val FINISHED_ROUTE_EXTRA = "FINISHED_ROUTE_EXTRA"

        fun newInstance(): MapFragment {
            return MapFragment()
        }

        fun newInstance(finishedRoute: FinishedRoute): MapFragment {
            val bundle = Bundle()
                    .apply {
                        putParcelable(FINISHED_ROUTE_EXTRA, finishedRoute)
                    }

            val fargment = MapFragment()
            fargment.arguments = bundle

            return fargment
        }
    }

    private lateinit var map: GoogleMap
    private val markerAnimation: MarkerAnimation = MarkerAnimation()

    private val markerPoints: ArrayList<Marker> = arrayListOf()
    private var polylines: ArrayList<Polyline> = arrayListOf()
    private var routePoints: ArrayList<LatLng> = arrayListOf()

    private var markerCar: Marker? = null
    private var routingTask: AsyncTask<Void, Void, ArrayList<Route>>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_start.setOnClickListener {
            if(routePoints.isEmpty()) {
                toast("Route is empty")
            } else {
                markerCar?.remove()
                markerCar = map.addMarker(
                        MarkerOptions()
                                .position(routePoints!![0])
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.washer_car))
                                .anchor(0.5f, 0.5f))

                markerAnimation.animateMarker(markerCar!!, routePoints!!)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val mGeoDataClient = Places.getGeoDataClient(requireContext())
        val placeAdapter = PlaceAutocompleteAdapter(requireContext(), mGeoDataClient, null, null)
        val waypointsAdapter = WaypointsAdapter(placeAdapter) { predicitons ->
            val filteredPlaceIds: List<String> =
                    predicitons
                            .asSequence()
                            .filter { it != null && it.placeId != null }
                            .map { it!!.placeId!! }
                            .toList()

            val task = mGeoDataClient.getPlaceById(*filteredPlaceIds.toTypedArray())
            task.addOnCompleteListener {
                val result = it.result
                val list = DataBufferUtils.freezeAndClose(result)
                markerPoints.forEach { it.remove() }
                markerPoints.clear()
                list.forEachIndexed { index, place ->
                    markerPoints.add(addPoint(place.latLng, index))
                    updateRouting()
                }
            }
        }

        btn_add_point.setOnClickListener {
            waypointsAdapter.addItem()
            view?.requestFocus()
        }

        rv_waypoint.adapter = waypointsAdapter
        rv_waypoint.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onRoutingCancelled() {
        routePoints.clear()
    }

    override fun onRoutingStart() {
    }

    override fun onRoutingFailure(p0: RouteException?) {
        routePoints.clear()
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, p1: Int) {
        toast("Routing success, routes: ${route?.size}")
        if (polylines.isNotEmpty()) {
            for (poly in polylines) {
                poly.remove()
            }
        }

        routePoints.clear()

        if(route == null || route.isEmpty()) {
            return
        }

        routePoints.addAll(route[0].points)

        val polyOptions = PolylineOptions()
        polyOptions.color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        polyOptions.width(10f)
        polyOptions.addAll(routePoints)
        val polyline = map.addPolyline(polyOptions)
        polylines.add(polyline)

        toast("Route: distance - " + route[0].distanceValue + ": duration - " + route[0].durationValue)
    }

    private fun addPoint(latLng: LatLng, position: Int): Marker {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        return map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(MARKER_COLORS[position])))
    }

    private fun updateRouting() {
        if (getWaypoints().size < 2) {
            return
        }

        routingTask = Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(getWaypoints())
                .build()
                .execute()
    }

    private fun getWaypoints(): List<LatLng> = markerPoints.map { it.position }
}