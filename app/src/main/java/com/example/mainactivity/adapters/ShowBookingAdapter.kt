package com.example.mainactivity.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.activities.BookingDetailsActivity
import com.example.mainactivity.models.FireBookingCourt
import com.example.mainactivity.models.TimeSlot
import com.example.mainactivity.viewmodel.ShowBookingViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL


class ShowBookingAdapter (private val vm: ShowBookingViewModel) : RecyclerView.Adapter<ShowBookingAdapter.ShowBookingViewHolder>() {
    var data: List<FireBookingCourt> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowBookingViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.booking, parent, false)
        val db = BookingDatabase.getDatabase(parent.context)
        return ShowBookingViewHolder(v, db, parent.context)
    }

    override fun onBindViewHolder(holder: ShowBookingViewHolder, position: Int) {
        val bookingCourt = data[position]
        holder.bind(bookingCourt, vm)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ShowBookingViewHolder(v: View, db: BookingDatabase, context: Context): RecyclerView.ViewHolder(v) {
        val showBookingIconContainer = v.findViewById<CardView>(R.id.showBookingIconContainer)
        val showBookingTimeslot = v.findViewById<TextView>(R.id.showBookingTimeslot)
        val showBookingCourtName = v.findViewById<TextView>(R.id.showBookingCourtName)
        val showBookingCourtAddress = v.findViewById<TextView>(R.id.showBookingCourtAddress)
        val infoButton = v.findViewById<ImageView>(R.id.infoButton)
        val showBookingSportIcon = v.findViewById<ImageView>(R.id.showBookingSportIcon)
        val context = v.context

        val organizedByMeText = v.findViewById<TextView>(R.id.bookingOrganizedByMeText)
        val bookingParticipantsText = v.findViewById<TextView>(R.id.bookingParticipantsText)
        val bookingParticipant1 = v.findViewById<ImageView>(R.id.bookingParticipant1)
        val bookingParticipant2 = v.findViewById<ImageView>(R.id.bookingParticipant2)
        val bookingParticipant3 = v.findViewById<ImageView>(R.id.bookingParticipant3)
        val bookingParticipant4 = v.findViewById<ImageView>(R.id.bookingParticipant4)
        val address = v.findViewById<ImageView>(R.id.showBookingNavigation)

        val db = Firebase.firestore

        fun bind(b: FireBookingCourt, vm: ShowBookingViewModel) {
            showBookingSportIcon.setImageResource(getSportIcon(b.court_sport!!))
            showBookingIconContainer.setViewColor(getSportColor(b.court_sport!!))

            if (b.user_email == LoggedInUser.user.email) {
                organizedByMeText.visibility = View.VISIBLE
            } else {
                organizedByMeText.visibility = View.GONE
            }

            address.setOnClickListener{
                val location = b.court_address
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
                it.context.startActivity(intent)
            }

            if (b.teams!!.isEmpty()) {
                bookingParticipantsText.visibility = View.GONE
                bookingParticipant1.visibility = View.GONE
                bookingParticipant2.visibility = View.GONE
                bookingParticipant3.visibility = View.GONE
                bookingParticipant4.visibility = View.GONE
            }
            else {
                bookingParticipantsText.visibility = View.VISIBLE
                // Extract the participant email addresses from the teams map
                val participantEmails = b.teams!!.keys.toList()

                // Create a list of bookingParticipant ImageViews
                val bookingParticipants = listOf(
                    bookingParticipant1,
                    bookingParticipant2,
                    bookingParticipant3,
                    bookingParticipant4
                )

                // Call the function to fill the profile pictures
                fillParticipantProfilePictures(b.teams!!, bookingParticipants)

                // Show the appropriate number of ImageViews based on the number of participants
                bookingParticipants.take(participantEmails.size).forEach {
                    it.visibility = View.VISIBLE
                }

                // Hide the remaining ImageViews if there are fewer participants than available ImageViews
                bookingParticipants.drop(participantEmails.size).forEach {
                    it.visibility = View.GONE
                }
            }


            showBookingTimeslot.text = b.time_slot_id?.let { TimeSlot(it.toInt()).toString() }
            showBookingCourtName.text = b.court_name
            showBookingCourtAddress.text = b.court_address
            infoButton.setOnClickListener {
                val intent = Intent(context, BookingDetailsActivity::class.java)
                intent.putExtra("bookingId", b.booking_id) // Give reservationDetails
                context.startActivity(intent)
            }
        }

        // Function to fetch and display profile pictures of participants
        private fun fillParticipantProfilePictures(teams: Map<String, String>, bookingParticipants: List<ImageView>) {
            val maxParticipants = bookingParticipants.size

            // Iterate over the teams map and fetch profile pictures for each participant
            teams.entries.take(maxParticipants).forEachIndexed { index, entry ->
                val userEmail = entry.key
                val participantImageView = bookingParticipants[index]
                Storage.getProfilePic(userEmail) {
                    participantImageView.setImageBitmap(it)
                }
            }

            // Hide the remaining ImageViews if there are fewer participants than available ImageViews
            for (i in teams.size until maxParticipants) {
                bookingParticipants[i].visibility = View.GONE
            }
        }

    }

    fun setTodayBooking(list: List<FireBookingCourt>) {
        this.data = list
        this.notifyDataSetChanged()
    }

}