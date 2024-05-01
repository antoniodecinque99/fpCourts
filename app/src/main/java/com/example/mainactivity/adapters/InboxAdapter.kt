package com.example.mainactivity.adapters

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.models.FireBookingCourtPending
import com.example.mainactivity.models.TimeSlot
import com.example.mainactivity.setViewColor
import com.example.mainactivity.viewmodel.InboxViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

class InboxAdapter(private val vm: InboxViewModel): RecyclerView.Adapter<InboxAdapter.InboxViewHolder>(){
    var data: List<FireBookingCourtPending> = listOf()

    class InboxViewHolder(v: View): RecyclerView.ViewHolder(v) {

        val pendingDate = v.findViewById<TextView>(R.id.pendingDate)
        val pendingHour = v.findViewById<TextView>(R.id.pendingHour)
        val pendingSportIcon = v.findViewById<ImageView>(R.id.pendingSportIcon)
        val pendingSportWrap = v.findViewById<CardView>(R.id.pendingSportWrap)
        val pendingCourtName = v.findViewById<TextView>(R.id.pendingCourtName)
        val pendingCourtAddress = v.findViewById<TextView>(R.id.pendingCourtAddress)
        val pendingOrganizerEmail = v.findViewById<TextView>(R.id.pendingOrganizerEmail)

        val pendingAcceptButton = v.findViewById<Button>(R.id.pendingAcceptButton)
        val pendingRefuseButton = v.findViewById<Button>(R.id.pendingRejectButton)
        val pendingSatusText = v.findViewById<TextView>(R.id.pendingStatusText)

        val pendingOrganizerUserIcon = v.findViewById<ImageView>(R.id.pendingOrganizerUserIcon)
        val pendingItemLayout = v.findViewById<ConstraintLayout>(R.id.pendingItemLayout)

        val db = Firebase.firestore

        fun bind(p: FireBookingCourtPending, notifyDataSetChanged: () -> Unit) {

            pendingDate.text = convertDateToDisplay(p.date!!)
            pendingHour.text = TimeSlot(p.time_slot_id!!.toInt()).toString()

            pendingSportIcon.setImageResource(getSportIcon(p.court_sport!!))
            pendingSportWrap.setViewColor(getSportColor(p.court_sport!!))

            pendingCourtName.text = p.court_name
            pendingCourtAddress.text = p.court_address

            pendingOrganizerEmail.text = p.user_email_organizer

            pendingAcceptButton.setOnClickListener {
                db.collection("pendings")
                    .whereEqualTo("booking_id", p.booking_id)
                    .whereEqualTo("user_email", LoggedInUser.user.email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            val documentId = document.id

                            val updates = hashMapOf<String, Any>(
                                "status" to "accepted",
                                "seen" to true
                            )

                            db.collection("pendings")
                                .document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    p.status = "accepted"
                                    p.seen = true

                                    db.collection("bookings")
                                        .whereEqualTo("booking_id", p.booking_id)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener { bookingQuerySnapshot ->
                                            if (!bookingQuerySnapshot.isEmpty) {
                                                val bookingDocument = bookingQuerySnapshot.documents[0]
                                                val teamsMap = bookingDocument.get("teams") as? HashMap<String, String>

                                                if (teamsMap != null) {
                                                    teamsMap[LoggedInUser.user.email] = p.team!!

                                                    val bookingUpdates = hashMapOf<String, Any>(
                                                        "teams" to teamsMap
                                                    )

                                                    db.collection("bookings")
                                                        .document(bookingDocument.id)
                                                        .update(bookingUpdates)
                                                        .addOnSuccessListener {
                                                            notifyDataSetChanged()
                                                        }
                                                }
                                            }
                                        }
                                }
                        }
                    }
            }


            pendingRefuseButton.setOnClickListener {
                db.collection("pendings")
                    .whereEqualTo("booking_id", p.booking_id)
                    .whereEqualTo("user_email", LoggedInUser.user.email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            val documentId = document.id

                            val updates = hashMapOf<String, Any>(
                                "status" to "rejected",
                                "seen" to true
                            )

                            db.collection("pendings")
                                .document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    p.status = "rejected"
                                    p.seen = true
                                    notifyDataSetChanged()
                                }
                        }
                    }
            }

            // Fetch and display the user's pictureUri using coroutines
            Storage.getProfilePic(p.user_email_organizer ?: "") {
                pendingOrganizerUserIcon.setImageBitmap(it)
            }
            /*GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("email", p.user_email_organizer)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]
                        val pictureUri = userDocument.getString("pictureUri")

                        pictureUri?.let {
                            try {
                                val inputStream: InputStream = URL(pictureUri).openStream()
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                withContext(Dispatchers.Main) {
                                    pendingOrganizerUserIcon.setImageBitmap(bitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }*/

            if (p.seen == false) {
                pendingItemLayout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.unseen_notification))
            } else {
                pendingItemLayout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.white))
            }


            if (p.status == "accepted") {
                pendingAcceptButton.visibility = View.GONE
                pendingRefuseButton.visibility = View.GONE
                pendingSatusText.visibility = View.VISIBLE
                pendingSatusText.text = "You accepted this invitation"
                pendingSatusText.setTextColorRes(R.color.green)
            } else if (p.status == "rejected") {
                pendingAcceptButton.visibility = View.GONE
                pendingRefuseButton.visibility = View.GONE
                pendingSatusText.visibility = View.VISIBLE
                pendingSatusText.text = "You rejected this invitation"
                pendingSatusText.setTextColorRes(R.color.red)
            } else if (p.status == "pending") {
                pendingAcceptButton.visibility = View.VISIBLE
                pendingRefuseButton.visibility = View.VISIBLE
                pendingSatusText.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.inbox_pending_item_layout, parent, false)
        return InboxViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val pending = data[position]
        holder.bind(pending, ::notifyDataSetChanged)
    }

    fun setPending(p: List<FireBookingCourtPending>) {
        this.data = p
        notifyDataSetChanged()
    }

    fun getPendingItem(position: Int): FireBookingCourtPending {
        return data[position]
    }
}