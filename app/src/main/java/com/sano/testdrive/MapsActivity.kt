package com.sano.testdrive

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AutoCompleteTextView
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var markerStart: Marker? = null
    private var markerSecond: Marker? = null
    private var markerThird: Marker? = null
    private var markerFourth: Marker? = null
    private var markerEnd: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
}
