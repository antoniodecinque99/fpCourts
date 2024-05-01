package com.example.mainactivity.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.models.FireCourtReview
import com.example.mainactivity.setViewColor
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CourtReviewsAdapter: RecyclerView.Adapter<CourtReviewsAdapter.CourtReviewViewHolder>() {

    var courtReviews: List<FireCourtReview> = listOf()
    var currentReviewId = 0


    class CourtReviewViewHolder(v: View): RecyclerView.ViewHolder(v) {

        private val view = v

        private fun loadPicture(): Bitmap? {
            return try {
                val imageFile = File(view.context.filesDir, "profile_pic.png")
                BitmapFactory.decodeStream(FileInputStream(imageFile))
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun bind(r: FireCourtReview, currentReviewId: Int) {
            val pic = view.findViewById<ImageView>(R.id.reviewerImage)
            val db = Firebase.firestore

            Storage.getProfilePic(r.user_email) {
                pic.setImageBitmap(it)
            }

            view.findViewById<TextView>(R.id.reviewText).text = r.review
            view.findViewById<TextView>(R.id.reviewerName).text = "${r.user?.name ?: "Name Surname"}"
            view.findViewById<TextView>(R.id.reviewerNickname).text = r.user!!.nickname

            val stars = listOf(
                view.findViewById<ImageView>(R.id.reviewStar1),
                view.findViewById<ImageView>(R.id.reviewStar2),
                view.findViewById<ImageView>(R.id.reviewStar3),
                view.findViewById<ImageView>(R.id.reviewStar4),
                view.findViewById<ImageView>(R.id.reviewStar5),
            )

            for(star in stars) {
                star.setImageResource(R.drawable.baseline_star_empty_24)
            }

            view.findViewById<TextView>(R.id.reviewRate).text = String.format("%.1f", r.rating.toDouble())

            for(i in 0..r.rating-1) {
                stars[i].setImageResource(R.drawable.baseline_star_24)
            }

            val visitedOnDate = view.findViewById<TextView>(R.id.visitedOnDate)
            if(r.booking_date != null) {
                visitedOnDate.setText("Court visited on: ${convertDateToDisplay(r.booking_date!!, false)}")
            } else {
                visitedOnDate.visibility = View.GONE
            }


            if(currentReviewId == r.review_id) {
                view.setViewColor(R.color.light_green)
            }

            val reviewDateTextView = view.findViewById<TextView>(R.id.reviewDate)
            val reviewDate = LocalDate.parse(r.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val currentTimeMillis = System.currentTimeMillis()
            val timeAgo = DateUtils.getRelativeTimeSpanString(reviewDate.toEpochDay() * 24 * 60 * 60 * 1000, currentTimeMillis, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)

            reviewDateTextView.text = timeAgo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtReviewViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.court_review_layout, parent, false)
        return CourtReviewViewHolder(v)
    }

    override fun getItemCount(): Int {
        return courtReviews.size
    }

    override fun onBindViewHolder(holder: CourtReviewViewHolder, position: Int) {
        val data = courtReviews[position]
        holder.bind(data, currentReviewId)
    }

    fun setReviewsAndUserReview(reviews: List<FireCourtReview>, currentReviewId: Int) {
        this.courtReviews = reviews
        this.currentReviewId = currentReviewId
        notifyDataSetChanged()
    }
}
