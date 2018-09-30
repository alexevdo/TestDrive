package com.sano.testdrive

import com.sano.testdrive.model.SimplePrediction

interface Router {
    fun openHistoryFragment()
    fun openMainFragment(simplePredictions: ArrayList<SimplePrediction>?)
}