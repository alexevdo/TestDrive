package com.sano.testdrive

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sano.testdrive.history.HistoryFragment
import com.sano.testdrive.main.MainFragment
import com.sano.testdrive.model.SimplePrediction

class MainActivity : AppCompatActivity(), Router {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            openMainFragment(null)
        }
    }

    override fun openHistoryFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, HistoryFragment.newInstance())
                .commit()
    }

    override fun openMainFragment(simplePredictions: ArrayList<SimplePrediction>?) {
        val fragment =
                if (simplePredictions == null) MainFragment.newInstance()
                else MainFragment.newInstance(simplePredictions)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.container) is HistoryFragment) openMainFragment(null)
        else super.onBackPressed()
    }
}