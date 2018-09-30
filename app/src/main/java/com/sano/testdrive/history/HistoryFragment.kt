package com.sano.testdrive.history

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sano.testdrive.DriveApplication
import com.sano.testdrive.R
import com.sano.testdrive.Router
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment: Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routes = (requireContext().applicationContext as DriveApplication).getDriveDao().getFinishedRoutes()

        val adapter = HistoryAdapter(routes) {
            (requireActivity() as Router).openMainFragment(ArrayList(it))
        }

        rv_history.adapter = adapter
        rv_history.layoutManager = LinearLayoutManager(requireContext())
    }
}