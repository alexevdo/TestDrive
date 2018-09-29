package com.sano.testdrive.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import com.google.android.gms.location.places.AutocompletePrediction
import com.sano.testdrive.R

class WaypointViewHolder(itemView: View,
                         private val autocompleteAdapter: PlaceAutocompleteAdapter,
                         private val onItemSelectListener: ((Int, AutocompletePrediction) -> Unit)?,
                         private val onItemRemoveListener: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    private val autoCompleteTextView: AutoCompleteTextView = itemView.findViewById(R.id.ac_waypoint)
    private val closeImageView: ImageView = itemView.findViewById(R.id.iv_close)

    fun bind(isClosable: Boolean, isLast: Boolean, prediction: CharSequence?) {
        closeImageView.visibility = if(isClosable) View.VISIBLE else View.GONE
        closeImageView.setOnClickListener { onItemRemoveListener?.invoke(adapterPosition) }

        autoCompleteTextView.setAdapter(autocompleteAdapter)
        autoCompleteTextView.setOnItemClickListener { parent, view, position, _ ->
            val clickedPrediction = (parent.getItemAtPosition(position) as AutocompletePrediction)
            onItemSelectListener?.invoke(adapterPosition, clickedPrediction)
        }

        autoCompleteTextView.setText(prediction)

        autoCompleteTextView.hint = if(isLast) {
            itemView.resources.getStringArray(R.array.waypoint_hints).last()
        } else {
            itemView.resources.getStringArray(R.array.waypoint_hints)[adapterPosition]
        }
    }
}