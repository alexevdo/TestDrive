package com.sano.testdrive.main

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.example.latlnginterpolation.MarkerAnimation
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sano.testdrive.DriveApplication
import com.sano.testdrive.R
import com.sano.testdrive.Router
import com.sano.testdrive.model.SimplePrediction
import com.sano.testdrive.util.MARKER_COLORS
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.toast

class MainFragment : BaseMapFragment(), MainView {

    companion object {
        private const val FINISHED_ROUTE_EXTRA = "FINISHED_ROUTE_EXTRA"

        fun newInstance(): MainFragment {
            return MainFragment()
        }

        fun newInstance(simplePredictions: ArrayList<SimplePrediction>): MainFragment {
            val bundle = Bundle()
                    .apply { putParcelableArrayList(FINISHED_ROUTE_EXTRA, simplePredictions) }

            return MainFragment()
                    .apply { arguments = bundle }
        }
    }

    private lateinit var presenter: MainPresenter

    private lateinit var map: GoogleMap
    private val carAnimation: MarkerAnimation = MarkerAnimation()

    private val markerPoints: ArrayList<Marker> = arrayListOf()
    private val polylines: ArrayList<Polyline> = arrayListOf()
    private var markerCar: Marker? = null

    private lateinit var waypointsAdapter: WaypointsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val geoDataClient = Places.getGeoDataClient(requireContext())

        presenter = MainPresenter(this,
                (requireContext().applicationContext as DriveApplication).getDriveDao(),
                geoDataClient,
                getString(R.string.google_maps_key))

        btn_start.setOnClickListener { presenter.onRouteStartClick() }

        carAnimation.setOnCompleteListener { presenter.onCarAnimationComplete(waypointsAdapter.getPlaceIds()) }

        setHasOptionsMenu(true)

        setupWaypointsRecyclerView(geoDataClient)

        btn_add_point.setOnClickListener { waypointsAdapter.addItem() }

        arguments
                .getOrNull<ArrayList<SimplePrediction>>(FINISHED_ROUTE_EXTRA)
                ?.let { waypointsAdapter.setItems(it) }
    }

    private fun setupWaypointsRecyclerView(geoDataClient: GeoDataClient) {
        val placeAdapter = PlaceAutocompleteAdapter(requireContext(), geoDataClient, null, null)

        waypointsAdapter = WaypointsAdapter(placeAdapter) { presenter.userPredictionsUpdate(it) }

        rv_waypoint.adapter = waypointsAdapter
        rv_waypoint.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_history) (requireActivity() as Router).openHistoryFragment()
        return true
    }

    override fun moveCamera() {
        val builder = LatLngBounds.Builder()
        for (marker in markerPoints) {
            builder.include(marker.position)
        }
        val bounds = builder.build()

        val padding = 150
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.animateCamera(cameraUpdate)
    }

    override fun clearMap() {
        markerPoints.forEach { it.remove() }
        markerPoints.clear()
        polylines.forEach { it.remove() }
        polylines.clear()
    }

    override fun onStop() {
        super.onStop()

        stopAnimation()
        presenter.onStop()
    }

    private fun addPoint(latLng: LatLng, position: Int): Marker {
        return map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(MARKER_COLORS[position])))
    }

    override fun stopAnimation() {
        carAnimation.stopAnimation()
        markerCar?.apply { remove() }
    }

    override fun showToast(text: String) {
        toast(text)
    }

    override fun addMarkerCar(position: LatLng) {
        markerCar?.remove()
        markerCar = map.addMarker(
                MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.washer_car))
                        .anchor(0.5f, 0.5f))
    }

    override fun animateMarkerCar(routePoints: ArrayList<LatLng>) {
        markerCar?.let { carAnimation.animateMarker(it, routePoints) }
    }

    override fun addMarkers(points: List<LatLng>) {
        points.forEachIndexed { index, point ->
            markerPoints.add(addPoint(point, index))
        }
    }

    override fun addPolylines(routePoints: java.util.ArrayList<LatLng>) {
        val polyOptions = PolylineOptions()
        polyOptions.color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        polyOptions.width(10f)
        polyOptions.addAll(routePoints)
        val polyline = map.addPolyline(polyOptions)
        polylines.add(polyline)
    }
}