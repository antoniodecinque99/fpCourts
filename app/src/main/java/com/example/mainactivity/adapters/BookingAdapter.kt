package com.example.mainactivity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.joins.BookingCourt

class BookingAdapter(val data: List<BookingCourt>): RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(v: View): RecyclerView.ViewHolder(v) {

        val icon = v.findViewById<ImageView>(R.id.showBookingIcon)
        val sportName = v.findViewById<TextView>(R.id.showBookingSportName)
        val timeslot = v.findViewById<TextView>(R.id.showBookingTimeslot)
        val courtName = v.findViewById<TextView>(R.id.showBookingCourtName)
        val courtAddress = v.findViewById<TextView>(R.id.showBookingCourtAddress)

        fun bind(b: BookingCourt) {
            icon.setImageResource(R.drawable.basket_icon)
            when(b.court_sport) {
                "Basketball" -> icon.setImageResource(R.drawable.basket_icon)
                "Football" -> icon.setImageResource(R.drawable.football_icon)
                "Tennis" -> icon.setImageResource(R.drawable.tennis_icon)
                "Volleyball" -> icon.setImageResource(R.drawable.volley_icon)
                "Padel" -> icon.setImageResource(R.drawable.padel_icon)
                else -> icon.setImageResource(R.drawable.undefined_icon)
            }
            sportName.text = "${b.court_sport}"
            courtName.text = "${b.court_name}"
            courtAddress.text = "${b.court_address}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.booking, parent, false)
        return BookingViewHolder(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = data[position]
        holder.bind(booking)
    }
}