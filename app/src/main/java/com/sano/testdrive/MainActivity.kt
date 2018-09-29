package com.sano.testdrive

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sano.testdrive.model.FinishedRoute

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapFragment.newInstance())
                    .commit()
        }
    }
}