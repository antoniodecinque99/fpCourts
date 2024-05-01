package com.example.mainactivity.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.models.Court
import com.example.mainactivity.models.FireCourt
import com.example.mainactivity.setBackgroundColorRes
import com.example.mainactivity.setImageColorRes
import com.example.mainactivity.viewmodel.CourtsListViewModel

class CourtsListAdapter(private val vm: CourtsListViewModel):
    RecyclerView.Adapter<CourtsListAdapter.CourtsListViewHolder>() {

    var courts: List<FireCourt> = listOf()

    class CourtsListViewHolder(v: View): RecyclerView.ViewHolder(v) {

        private val view = v

        fun bind(c: FireCourt, vm: CourtsListViewModel) {
            view.findViewById<TextView>(R.id.courtName).text = c.name
            view.findViewById<TextView>(R.id.courtAddress).text = c.address
            //view.findViewById<ImageView>(R.id.court_image).setImageResource(getCourtImage(c.court_id))
            Storage.getCourtPic(c.court_id) {
                view.findViewById<ImageView>(R.id.court_image).setImageBitmap(it)
            }
            view.findViewById<TextView>(R.id.rating).text = String.format("%.1f", c.averageRating)

            Log.d("sport", "${c.sport}")
            view.findViewById<TextView>(R.id.reviewBookingSportName).text = c.sport
            view.findViewById<ImageView>(R.id.reviewBookingIcon).setImageResource(getSportIcon(c.sport))
            view.findViewById<CardView>(R.id.reviewSportWrap).setViewColor(getSportColor(c.sport))

            view.findViewById<ConstraintLayout>(R.id.courtItem).setOnClickListener {
                vm.showCourtInfo(c.court_id)
            }

            //fill the review stars
            val lastFullStar = c.averageRating.toInt()
            val decimalRating = c.averageRating-lastFullStar
            val stars = listOf(
                view.findViewById<ImageView>(R.id.star1),
                view.findViewById(R.id.star2),
                view.findViewById(R.id.star3),
                view.findViewById(R.id.star4),
                view.findViewById(R.id.star5),
            )
            stars.forEach {
                it.setImageResource(R.drawable.baseline_star_empty_24)
            }
            for(i in 0..lastFullStar-1) {
                stars[i].setImageResource(R.drawable.baseline_star_24)
            }
            if(decimalRating>=0.5) stars[lastFullStar].setImageResource(R.drawable.baseline_star_half_24)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtsListViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.court_list_item_layout, parent, false)
        return CourtsListViewHolder(v)
    }

    override fun onBindViewHolder(holder: CourtsListViewHolder, position: Int) {
        val data = courts[position]
        holder.bind(data, vm)
    }

    override fun getItemCount(): Int {
        return courts.size
    }

    fun _setCourts(courts: List<FireCourt>) {
        this.courts = courts
        notifyDataSetChanged()
    }
}