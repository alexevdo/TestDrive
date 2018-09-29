package com.sano.testdrive.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.location.places.AutocompletePrediction
import com.sano.testdrive.R

const val MAX_COUNT = 5

class WaypointsAdapter(private val placeAutocompleteAdapter: PlaceAutocompleteAdapter,
                       private val listener: ((ArrayList<AutocompletePrediction?>) -> Unit)): RecyclerView.Adapter<WaypointViewHolder>() {

    private val predictions: ArrayList<AutocompletePrediction?> = ArrayList(2)

    init {
        predictions.add(null)
        predictions.add(null)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): WaypointViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_item_waypoint, viewGroup, false)
        return WaypointViewHolder(view, placeAutocompleteAdapter,
                { i: Int, s: AutocompletePrediction ->
                    predictions[i] = s
                    listener.invoke(predictions)
                },
                {i: Int ->
                    predictions.removeAt(i)
                    listener.invoke(predictions)
                    notifyDataSetChanged()
                })
    }

    override fun getItemCount(): Int {
        return predictions.size
    }

    override fun onBindViewHolder(viewHolder: WaypointViewHolder, position: Int) {
        viewHolder.bind(position != 0 && position != predictions.lastIndex,
                position == predictions.lastIndex,
                predictions[position]?.getFullText(null))
    }

    fun addItem() {
        if(predictions.size == MAX_COUNT) return
        predictions.add(predictions.lastIndex, null)
        notifyDataSetChanged()
    }

    fun getPlaceIds(): List<AutocompletePrediction?> {
        return predictions.toList()
    }
}