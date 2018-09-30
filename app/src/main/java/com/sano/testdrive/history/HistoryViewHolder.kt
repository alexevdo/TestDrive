package com.sano.testdrive.history

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.sano.testdrive.R
import com.sano.testdrive.model.FinishedRoute
import org.jetbrains.anko.find
import java.text.DateFormat

class HistoryViewHolder(itemView: View,
                        private val listener: ((Int) -> Unit)) : RecyclerView.ViewHolder(itemView) {

    companion object {
        val dateFormat = DateFormat.getDateInstance(java.text.DateFormat.SHORT)!!
    }

    private val startTextView: TextView = itemView.find(R.id.tv_start)
    private val endTextView: TextView = itemView.find(R.id.tv_end)
    private val dateTextView: TextView = itemView.find(R.id.tv_date)

    fun bind(finishedRoute: FinishedRoute) {
        startTextView.text = finishedRoute.predictions.first().text
        endTextView.text = finishedRoute.predictions.last().text
        dateTextView.text = dateFormat.format(finishedRoute.date)
        itemView.setOnClickListener { listener.invoke(adapterPosition) }
    }
}