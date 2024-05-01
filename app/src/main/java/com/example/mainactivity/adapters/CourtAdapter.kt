package com.example.mainactivity.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.activities.ConfirmBookingActivity
import com.example.mainactivity.models.FireCourt
import com.example.mainactivity.viewmodel.BookingViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CourtAdapter (private val vm: BookingViewModel, private val b: Button) :RecyclerView.Adapter<CourtAdapter.BookingViewHolder>(){

    var data: List<FireCourt> = listOf()
    var busyTimeSlots: Map<Int, List<Int>> = mapOf()
    var listFavoriteCourts: MutableList<Long> = mutableListOf()

    class BookingViewHolder(v: View, listFavorite: MutableList<Long>): RecyclerView.ViewHolder(v) {

        private val imageView = v.findViewById<ImageView>(R.id.imageview1)
        private val maps = v.findViewById<ImageView>(R.id.navigation)
        private val fav = v.findViewById<ImageView>(R.id.favorite)
        private val courtName = v.findViewById<TextView>(R.id.text1)
        private val courtAddress = v.findViewById<TextView>(R.id.text2)
        private val courtPrice = v.findViewById<TextView>(R.id.courtDetailsPrice)
        private val buttonTimeSlot = v.findViewById<Button>(R.id.button_time_slot)
        private val completeButton = v.findViewById<Button>(R.id.complete_button)
        private val popupMenu = PopupMenu(v.context, buttonTimeSlot)
        private val listFavoriteCourts = listFavorite
        private val BookingIcon = v.findViewById<ImageView>(R.id.showBookingIcon)
        private val BookingSportName = v.findViewById<TextView>(R.id.showBookingSportName)
        private val sportIconAndText = v.findViewById<CardView>(R.id.pendingSportWrap)

        val db = Firebase.firestore

        fun bind(c: FireCourt, vm: BookingViewModel, b: Button, busyTimeSlots: Map<Int, List<Int>>, notifyDataSetChanged: () -> Unit) {

            buttonTimeSlot.setText("Choose time slot")
            completeButton.visibility = View.GONE

            Log.d("list", this.listFavoriteCourts.toString())
            if (this.listFavoriteCourts.contains(c.court_id.toLong()) == false){
                fav.setImageResource(R.drawable.baseline_favorite_border_24)
            }else{
                fav.setImageResource(R.drawable.baseline_favorite_24)
            }

            fav.setOnClickListener {
                db.collection("favorites")
                    .whereEqualTo("user_email", LoggedInUser.user.email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            val documentId = document.id
                            val courtIds = document.get("court_id") as MutableList<Long>
                            if (!courtIds.contains(c.court_id.toLong())) {
                                courtIds.add(c.court_id.toLong())
                                this.listFavoriteCourts.add(c.court_id.toLong())
                                fav.setImageResource(R.drawable.baseline_favorite_24)
                            }else{
                                courtIds.remove(c.court_id.toLong())
                                this.listFavoriteCourts.remove(c.court_id.toLong())
                                fav.setImageResource(R.drawable.baseline_favorite_border_24)
                            }
                            db.collection("favorites")
                                .document(documentId)
                                .update("court_id", courtIds)
                                .addOnSuccessListener {
                                    notifyDataSetChanged()
                                }
                        }
                    }
            }



            when(c.sport) {
                "Basketball" -> {
                    BookingIcon.setImageResource(R.drawable.basket_icon)
                    sportIconAndText.setViewColor(R.color.basket_color)
                }
                "Football" -> {
                    BookingIcon.setImageResource(R.drawable.football_icon)
                    sportIconAndText.setViewColor(R.color.football_color)
                }
                "Tennis" -> {
                    BookingIcon.setImageResource(R.drawable.tennis_icon)
                    sportIconAndText.setViewColor(R.color.tennis_color)
                }
                "Volleyball" -> {
                    BookingIcon.setImageResource(R.drawable.volley_icon)
                    sportIconAndText.setViewColor(R.color.volley_color)
                }
                "Padel" -> {
                    BookingIcon.setImageResource(R.drawable.padel_icon)
                    sportIconAndText.setViewColor(R.color.padel_color)
                }
                else -> BookingIcon.setImageResource(R.drawable.undefined_icon)
            }

            BookingSportName.setText(c.sport)

            Storage.getCourtPic(c.court_id) {
                imageView.setImageBitmap(it)
            }

            courtName.text = "${c.name}"
            courtAddress.text = "${c.address}"
            val priceText: String = String.format("%.2f", c.price)
            courtPrice.text = priceText + " € / 1:30 h"

            maps.setOnClickListener{
                val location = courtAddress.text.toString()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
                it.context.startActivity(intent)
            }

            popupMenu.menuInflater.inflate(R.menu.timeslot, popupMenu.menu)
            popupMenu.menu.clear()
            popupMenu.menu.add(Menu.NONE, R.id.item1, Menu.NONE, "9:00 AM - 10:30 AM")
            popupMenu.menu.add(Menu.NONE, R.id.item2, Menu.NONE, "10:30 AM - 12:00 PM")
            popupMenu.menu.add(Menu.NONE, R.id.item3, Menu.NONE, "12:00 PM - 1:30 PM")
            popupMenu.menu.add(Menu.NONE, R.id.item4, Menu.NONE, "3:00 PM - 4:30 PM")
            popupMenu.menu.add(Menu.NONE, R.id.item5, Menu.NONE, "4:30 PM - 6:00 PM")
            popupMenu.menu.add(Menu.NONE, R.id.item6, Menu.NONE, "6:00 PM - 7:30 PM")

            if(buttonTimeSlot.text == "Unavailable"){
                buttonTimeSlot.setText("Choose time slot")
                buttonTimeSlot.isClickable = true
                vm.busyTimeSlots.postValue(busyTimeSlots)
            }

            lateinit var date: String

            date = convertDisplayToDate_(b.text.toString())

            val courtBusyTimeSlots = busyTimeSlots[c.court_id]

            var id = 0
            if (courtBusyTimeSlots != null) {
                for (item_id in courtBusyTimeSlots) {
                    when(item_id) {
                        1 -> {
                            id = R.id.item1
                        }
                        2 -> {
                            id = R.id.item2
                        }
                        3 -> {
                            id = R.id.item3
                        }
                        4 -> {
                            id = R.id.item4
                        }

                        5 -> {
                            id = R.id.item5
                        }
                        6 -> {
                            id = R.id.item6
                        }
                    }
                    popupMenu.menu.removeItem(id)
                }
            }


            if (courtBusyTimeSlots?.size == 6) {
                buttonTimeSlot.setText("Unavailable")
                buttonTimeSlot.isClickable = false
            }

            popupMenu.setOnMenuItemClickListener {
                vm.date.postValue(date)
                vm.court_id.postValue(c.court_id)

                when(it.itemId) {
                    R.id.item1 -> {
                        vm.timeslot_id.postValue(1)
                        buttonTimeSlot.setText("9:00 AM - 10:30 AM")
                        //complete_button.setVisibility(View.VISIBLE)
                        completeButton.visibility = View.VISIBLE
                        true
                    }
                    R.id.item2 -> {
                        vm.timeslot_id.postValue(2)
                        buttonTimeSlot.setText("10:30 AM - 12:00 PM")
                        completeButton.setVisibility(View.VISIBLE)
                        true
                    }
                    R.id.item3 -> {
                        vm.timeslot_id.postValue(3)
                        buttonTimeSlot.setText("12:00 PM - 1:30 PM")
                        completeButton.setVisibility(View.VISIBLE)
                        true
                    }
                    R.id.item4 -> {
                        vm.timeslot_id.postValue(4)
                        buttonTimeSlot.setText("3:00 PM - 4:30 PM")
                        completeButton.setVisibility(View.VISIBLE)
                        true
                    }
                    R.id.item5 -> {
                        vm.timeslot_id.postValue(5)
                        buttonTimeSlot.setText("4:30 PM - 6:00 PM")
                        completeButton.setVisibility(View.VISIBLE)
                        true
                    }
                    R.id.item6 -> {
                        vm.timeslot_id.postValue(6)
                        buttonTimeSlot.setText("6:00 PM - 7:30 PM")
                        completeButton.setVisibility(View.VISIBLE)

                        true
                    }
                    else -> false
                }
            }

            buttonTimeSlot.setOnClickListener {

                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenu)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
                popupMenu.show()
            }



            completeButton.setOnClickListener {
                val intent = Intent(it.context, ConfirmBookingActivity::class.java)
                    .putExtra("DATE", vm.date.value)
                    .putExtra("TIMESLOT_ID", vm.timeslot_id.value)
                    .putExtra("COURT_ID", vm.court_id.value)
                it.context.startActivity(intent)
            }

        }

    }
    fun setCourt(list: List<FireCourt>) {
        this.data = list
        this.notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.courts_details_layout, parent, false)
        return BookingViewHolder(v, listFavoriteCourts)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val court = data[position]
        holder.bind(court, vm, b, busyTimeSlots, ::notifyDataSetChanged)
    }

    override fun getItemCount() = data.size

    fun _setBusyTimeSlots(list: Map<Int, List<Int>>) {
        this.busyTimeSlots = list
        notifyDataSetChanged()
    }

    fun _setFavoriteCourts(list: MutableList<Long>) {
        this.listFavoriteCourts = list
        notifyDataSetChanged()
    }

}


