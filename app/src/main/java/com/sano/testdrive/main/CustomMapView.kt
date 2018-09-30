package com.sano.testdrive.main

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import com.google.android.gms.maps.MapView

class CustomMapView(context: Context, attrs: AttributeSet) : MapView(context, attrs) {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP -> {
                println("unlocked")
                this.parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_DOWN -> {
                println("locked")
                this.parent.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}