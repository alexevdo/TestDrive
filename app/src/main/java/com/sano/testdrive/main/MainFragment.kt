package com.sano.testdrive.main

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.directions.route.*
import com.example.latlnginterpolation.MarkerAnimation
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sano.testdrive.DriveApplication
import com.sano.testdrive.R
import com.sano.testdrive.Router
import com.sano.testdrive.model.FinishedRoute
import com.sano.testdrive.model.SimplePrediction
import com.sano.testdrive.util.MARKER_COLORS
import kotlinx.android.synthetic.main.fragment_maps.*
import org.jetbrains.anko.support.v4.toast
import java.util.*
import kotlin.collections.ArrayList

class MainFragment : BaseMapFragment(), RoutingListener {

    companion object {
        private const val FINISHED_ROUTE_EXTRA = "FINISHED_ROUTE_EXTRA"

        fun newInstance(): MainFragment {
            return MainFragment()
        }

        fun newInstance(simplePredictions: ArrayList<SimplePrediction>): MainFragment {
            val bundle = Bundle()
                    .apply { putParcelableArrayList(FINISHED_ROUTE_EXTRA, simplePredictions) }

            val fargment = MainFragment()
            fargment.arguments = bundle

            return fargment
        }
    }

    private lateinit var map: GoogleMap
    private val carAnimation: MarkerAnimation = MarkerAnimation()

    private val markerPoints: ArrayList<Marker> = arrayListOf()
    private val polylines: ArrayList<Polyline> = arrayListOf()
    private val routePoints: ArrayList<LatLng> = arrayListOf()

    private var markerCar: Marker? = null
    private var routingTask: AsyncTask<Void, Void, ArrayList<Route>>? = null

    private lateinit var waypointsAdapter: WaypointsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_start.setOnClickListener {
            if (routePoints.isEmpty()) {
                toast("Route is empty")
            } else {
                markerCar?.remove()
                map.addMarker(
                        MarkerOptions()
                                .position(routePoints[0])
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.washer_car))
                                .anchor(0.5f, 0.5f))
                        .let {
                            markerCar = it
                            carAnimation.animateMarker(it, routePoints)
                        }
            }
        }

        carAnimation.setOnCompleteListener {
            toast("Route finished, stored in history")
            val route = FinishedRoute(date = Calendar.getInstance().time, predictions = waypointsAdapter.getPlaceIds())
            (requireContext().applicationContext as DriveApplication).getDriveDao().insertFinishedRoute(route)
        }

        setHasOptionsMenu(true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val mGeoDataClient = Places.getGeoDataClient(requireContext())
        val placeAdapter = PlaceAutocompleteAdapter(requireContext(), mGeoDataClient, null, null)
        waypointsAdapter = WaypointsAdapter(placeAdapter) { predicitons ->
            clearMap()

            if (predicitons.isEmpty()) return@WaypointsAdapter

            val filteredPlaceIds: List<String> = predicitons.map { it.placeId }

            mGeoDataClient
                    .getPlaceById(*filteredPlaceIds.toTypedArray())
                    .addOnCompleteListener {
                        val list = DataBufferUtils.freezeAndClose(it.result)

                        list.forEachIndexed { index, place ->
                            markerPoints.add(addPoint(place.latLng, index))
                        }

                        moveCamera()
                        updateRouting()
                    }
        }

        rv_waypoint.adapter = waypointsAdapter
        rv_waypoint.layoutManager = LinearLayoutManager(requireContext())

        btn_add_point.setOnClickListener {
            waypointsAdapter.addItem()
            view?.requestFocus()
        }

        val list: ArrayList<SimplePrediction>? = arguments.getOrNull(FINISHED_ROUTE_EXTRA)
        list?.let { waypointsAdapter.setItems(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_history) {
            (requireActivity() as Router).openHistoryFragment()
            return true
        }

        return true
    }

    private fun moveCamera() {
        val builder = LatLngBounds.Builder()
        for (marker in markerPoints) {
            builder.include(marker.position)
        }
        val bounds = builder.build()

        val padding = 150
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.animateCamera(cameraUpdate)
    }

    private fun clearMap() {
        markerPoints.forEach { it.remove() }
        markerPoints.clear()
        polylines.forEach { it.remove() }
        polylines.clear()
        routePoints.clear()
    }

    override fun onRoutingCancelled() {
        toast("Routing canceled")
    }

    override fun onRoutingStart() {
    }

    override fun onRoutingFailure(exception: RouteException?) {
        toast("Routing fail: ${exception?.message}")
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, p1: Int) {
        if (route == null || route.isEmpty()) {
            toast("Routing fail: empty route")
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

    override fun onStop() {
        super.onStop()

        stopAnimation()
        routingTask?.cancel(true)
    }

    private fun addPoint(latLng: LatLng, position: Int): Marker {
        return map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(MARKER_COLORS[position])))
    }

    private fun updateRouting() {
        stopAnimation()

        if (markerPoints.size < 2) {
            return
        }

        routingTask = Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(markerPoints.map { it.position })
                .build()
                .execute()
    }

    private fun stopAnimation() {
        carAnimation.stopAnimation()
        markerCar?.apply { remove() }
    }
}