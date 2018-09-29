package com.sano.testdrive

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.directions.route.*
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.toast


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {

    private val COLORS = intArrayOf(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorPrimaryLight, R.color.colorAccent, R.color.primary_dark_material_light)

    private lateinit var mMap: GoogleMap
    private var markerStart: Marker? = null
    private var markerSecond: Marker? = null
    private var markerThird: Marker? = null
    private var markerFourth: Marker? = null
    private var markerEnd: Marker? = null
    private var polylines: ArrayList<Polyline> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        map.onCreate(savedInstanceState)
        map.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        val sydney = LatLng(-34.0, 151.0)

        val mGeoDataClient = Places.getGeoDataClient(this)
        val adapter = PlaceAutocompleteAdapter(this, mGeoDataClient, null, null)

        et_start_point.prepare(mGeoDataClient, adapter, 0)
        et_second_point.prepare(mGeoDataClient, adapter, 1)
        et_third_point.prepare(mGeoDataClient, adapter, 2)
        et_fourth_point.prepare(mGeoDataClient, adapter, 3)
        et_end_point.prepare(mGeoDataClient, adapter, 4)
    }

    private fun addPoint(latLng: LatLng, position: Int) {
        getMarkerByPosition(position)?.remove()

        setMarkerToPosition(mMap.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .title("Marker in Sydney")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(getColorByPosition(position)))
        ), position)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        onPointAdded()
    }

    private var routing: AsyncTask<Void, Void, ArrayList<Route>>? = null

    private fun onPointAdded() {
        val markers = listOf(markerStart, markerSecond, markerThird, markerFourth, markerEnd)
        val waypoints = arrayListOf<LatLng>()

        markers.forEach { marker ->
            marker?.let {  waypoints.add(it.position) }
        }

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

    override fun onRoutingFailure(p0: RouteException?) {
        toast("Routing failuer, ${p0?.message}")
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, p1: Int) {
        toast("Routing success, routes: ${route?.size}")
        if (polylines.isNotEmpty()) {
            for (poly in polylines) {
                poly.remove()
            }
        }

        route ?: return

        //add route(s) to the map.
        for (i in route.indices) {
            //In case of more than 5 alternative routes
            val colorIndex = i % COLORS.size;

            val polyOptions = PolylineOptions()
            polyOptions.color(ContextCompat.getColor(this, COLORS[colorIndex]))
            polyOptions.width(10 + i * 3.0f)
            polyOptions.addAll(route[i].points);
            val polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline)

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    override fun onRoutingCancelled() {
        toast("Routing cancelled")
    }

    override fun onRoutingStart() {
        toast("Routing start")
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
            (parent.getItemAtPosition(adapterPosition) as AutocompletePrediction)
                    .getLatLng(client) { addPoint(it, position) }
        }
    }

    private fun AutocompletePrediction.getLatLng(client: GeoDataClient, listener: (LatLng) -> Unit) {
        val task = client.getPlaceById(this.placeId)
        task.addOnCompleteListener {
            val result = it.result
            val freeze = result.get(0).freeze()
            result.release()
            listener.invoke(freeze.latLng)
        }
    }

    public override fun onResume() {
        map.onResume()
        super.onResume()
    }


    public override fun onPause() {
        super.onPause()
        map.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }
}
