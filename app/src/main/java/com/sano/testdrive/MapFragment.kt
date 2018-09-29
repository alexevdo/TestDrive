package com.sano.testdrive

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import com.directions.route.*
import com.example.latlnginterpolation.MarkerAnimation
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sano.testdrive.model.FinishedRoute
import com.sano.testdrive.view.PlaceAutocompleteAdapter
import kotlinx.android.synthetic.main.fragment_maps.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

class MapFragment : BaseMapFragment(), RoutingListener {

    companion object {
        const val FINISHED_ROUTE_EXTRA = "FINISHED_ROUTE_EXTRA"

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

    private var markerStart: Marker? = null
    private var markerSecond: Marker? = null
    private var markerThird: Marker? = null
    private var markerFourth: Marker? = null
    private var markerEnd: Marker? = null
    private var polylines: ArrayList<Polyline> = arrayListOf()
    private var routePoints: List<LatLng>? = null
    private var markerCar: Marker? = null

    private var routing: AsyncTask<Void, Void, ArrayList<Route>>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_start.setOnClickListener {
            if (routePoints != null) {
                markerCar?.remove()
                markerCar = map.addMarker(
                        MarkerOptions()
                                .position(routePoints!![0])
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.washer_car))
                                .anchor(0.5f, 0.5f))

                markerAnimation.animateMarker(markerCar!!, routePoints!!)
            } else {
                toast("Route is empty")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        val sydney = LatLng(-34.0, 151.0)

        val mGeoDataClient = Places.getGeoDataClient(requireContext())
        val adapter = PlaceAutocompleteAdapter(requireContext(), mGeoDataClient, null, null)

        et_start_point.prepare(mGeoDataClient, adapter, 0)
        et_second_point.prepare(mGeoDataClient, adapter, 1)
        et_third_point.prepare(mGeoDataClient, adapter, 2)
        et_fourth_point.prepare(mGeoDataClient, adapter, 3)
        et_end_point.prepare(mGeoDataClient, adapter, 4)
    }

    override fun onRoutingCancelled() {
        routePoints = null
    }

    override fun onRoutingStart() {
    }

    override fun onRoutingFailure(p0: RouteException?) {
        routePoints = null
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, p1: Int) {
        toast("Routing success, routes: ${route?.size}")
        if (polylines.isNotEmpty()) {
            for (poly in polylines) {
                poly.remove()
            }
        }

        routePoints = route?.let { it[0].points }

        route ?: return

        val polyOptions = PolylineOptions()
        polyOptions.color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        polyOptions.width(10f)
        polyOptions.addAll(route[0].points)
        val polyline = map.addPolyline(polyOptions)
        polylines.add(polyline)

        toast("Route: distance - " + route[0].distanceValue + ": duration - " + route[0].durationValue)
    }

    private fun addPoint(latLng: LatLng, position: Int) {
        getMarkerByPosition(position)?.remove()

        setMarkerToPosition(map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(getColorByPosition(position)))
        ), position)

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        onPointAdded()
    }

    private fun onPointAdded() {
        val waypoints = getWaypoints()

        if (waypoints.size < 2) {
            return
        }

        routing = Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(waypoints)
                .build()
                .execute()
    }

    private fun getWaypoints(): List<LatLng> {
        val markers = listOf(markerStart, markerSecond, markerThird, markerFourth, markerEnd)
        val waypoints = arrayListOf<LatLng>()

        for (marker in markers) {
            marker?.let { waypoints.add(it.position) }
        }

        return waypoints
    }

    private fun getColorByPosition(position: Int): Float =
            when (position) {
                0 -> BitmapDescriptorFactory.HUE_AZURE
                1 -> BitmapDescriptorFactory.HUE_BLUE
                2 -> BitmapDescriptorFactory.HUE_GREEN
                3 -> BitmapDescriptorFactory.HUE_RED
                else -> BitmapDescriptorFactory.HUE_ORANGE
            }


    private fun getMarkerByPosition(position: Int) =
            when (position) {
                0 -> markerStart
                1 -> markerSecond
                2 -> markerThird
                3 -> markerFourth
                else -> markerEnd
            }

    private fun setMarkerToPosition(marker: Marker, position: Int) {
        when (position) {
            0 -> markerStart = marker
            1 -> markerSecond = marker
            2 -> markerThird = marker
            3 -> markerFourth = marker
            else -> markerEnd = marker
        }
    }

    private fun AutoCompleteTextView.prepare(client: GeoDataClient, adapter: PlaceAutocompleteAdapter, position: Int) {
        this.setAdapter(adapter)
        this.setOnItemClickListener { parent, _, adapterPosition, _ ->
            val prediction = (parent.getItemAtPosition(adapterPosition) as AutocompletePrediction)

            val task = client.getPlaceById(prediction.placeId)
            task.addOnCompleteListener {
                val result = it.result
                val freeze = result.get(0).freeze()
                result.release()
                addPoint(freeze.latLng, position)
            }
        }
    }

}