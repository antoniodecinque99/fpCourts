package com.example.mainactivity.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.joins.BookingCourt
import com.example.mainactivity.viewmodel.ShowBookingViewModel
import java.time.LocalDate

class ShowBookingDotsAdapter (private val vm: ShowBookingViewModel) : RecyclerView.Adapter<ShowBookingDotsAdapter.ShowBookingDotsViewHolder>() {
    var data: List<BookingCourt> = listOf();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowBookingDotsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.dot, parent, false)
        val db = BookingDatabase.getDatabase(parent.context)
        return ShowBookingDotsViewHolder(v, db, parent.context)
    }

    override fun onBindViewHolder(holder: ShowBookingDotsViewHolder, position: Int) {
        val bookingCourt = data[position]
        holder.bind(bookingCourt, vm)
    }

    override fun getItemCount(): Int {
        if (data.size <= 2) return data.size
        return 2
    }

    class ShowBookingDotsViewHolder(v: View, db: BookingDatabase, context: Context): RecyclerView.ViewHolder(v) {
        fun bind(b: BookingCourt, vm: ShowBookingViewModel) {

        }
    }

    fun setTodayBookingCount(list: List<BookingCourt>) {
        this.data = list
        this.notifyDataSetChanged()
    }

}