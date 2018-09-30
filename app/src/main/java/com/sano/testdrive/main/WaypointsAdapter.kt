package com.sano.testdrive.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sano.testdrive.R
import com.sano.testdrive.model.SimplePrediction

const val MAX_COUNT = 5

class WaypointsAdapter(private val placeAutocompleteAdapter: PlaceAutocompleteAdapter,
                       private val listener: ((List<SimplePrediction>) -> Unit)): RecyclerView.Adapter<WaypointViewHolder>() {

    private val predictions: ArrayList<SimplePrediction?> = ArrayList(5)

    init {
        predictions.add(null)
        predictions.add(null)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): WaypointViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_item_waypoint, viewGroup, false)
        return WaypointViewHolder(view, placeAutocompleteAdapter,
                { i: Int, s: SimplePrediction? ->
                    predictions[i] = s
                    listener.invoke(predictions.filterNotNull())
                },
                { i: Int ->
                    predictions.removeAt(i)
                    listener.invoke(predictions.filterNotNull())
                    notifyDataSetChanged()
                })
    }

    override fun getItemCount(): Int {
        return predictions.size
    }

    override fun onBindViewHolder(viewHolder: WaypointViewHolder, position: Int) {
        viewHolder.bind(position > 1,
                position == predictions.lastIndex,
                predictions[position]?.text)
    }

    fun addItem() {
        if(predictions.size == MAX_COUNT) return
        predictions.add(predictions.lastIndex, null)
        notifyDataSetChanged()
    }

    fun getPlaceIds(): List<SimplePrediction> {
        return predictions.filterNotNull()
    }

    fun setItems(list: ArrayList<SimplePrediction>) {
        predictions.clear()
        predictions.addAll(list)
        notifyDataSetChanged()
        listener.invoke(predictions.filterNotNull())
    }
}