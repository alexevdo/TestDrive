package com.sano.testdrive.history

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sano.testdrive.R
import com.sano.testdrive.model.FinishedRoute
import com.sano.testdrive.model.SimplePrediction

class HistoryAdapter(private val finishedRoutes: List<FinishedRoute>,
                     private val listener: ((List<SimplePrediction>) -> Unit)): RecyclerView.Adapter<HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view) {
            listener.invoke(finishedRoutes[it].predictions)
        }
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(finishedRoutes[position])
    }

    override fun getItemCount() = finishedRoutes.size

}