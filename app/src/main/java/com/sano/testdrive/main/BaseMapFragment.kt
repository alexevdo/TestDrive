package com.sano.testdrive.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.fragment_main.*

abstract class BaseMapFragment : Fragment(), OnMapReadyCallback {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync(this)
    }

    override fun onResume() {
        map_view.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
    }

    inline fun <reified T> Bundle?.getOrNull(key: String): T? {
        this?.get(key)?.let { return it as T } ?: return null
    }
}