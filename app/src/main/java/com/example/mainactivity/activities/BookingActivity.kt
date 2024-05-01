package com.example.mainactivity.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.adapters.CourtAdapter
import com.example.mainactivity.convertDateToDisplay
import com.example.mainactivity.convertDateToDisplay_
import com.example.mainactivity.viewmodel.BookingViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class BookingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener{

    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val vm by viewModels<BookingViewModel>()
    private var date: String = ""
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        db = Firebase.firestore
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val deleteCurrentSport = findViewById<ImageView>(R.id.deleteSport)
        val bundle = intent.extras

        date = bundle?.getString("current_date").toString()
        button2.setText(convertDateToDisplay_(date, false))

        deleteCurrentSport.setOnClickListener {
            vm.getCourts()
            button.setText("Select sport")
            deleteCurrentSport.visibility = View.INVISIBLE
        }

        //displayFormattedDate(calendar.timeInMillis)
        val popupMenu = PopupMenu(this, button)
        popupMenu.menuInflater.inflate(R.menu.popupmenu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item1 -> {
                    button.setText("Football")
                    //button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.football_icon, 0,0,0)
                    vm.getCourtBySport("Football")
                    deleteCurrentSport.visibility = View.VISIBLE
                    true
                }
                R.id.item2 -> {
                    button.setText("Tennis")
                    //button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tennis_icon, 0,0,0)
                    vm.getCourtBySport("Tennis")
                    deleteCurrentSport.visibility = View.VISIBLE

                    true
                }
                R.id.item3 -> {
                    button.setText("Padel")
                   // button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.padel_icon, 0,0,0)
                    vm.getCourtBySport("Padel")
                    deleteCurrentSport.visibility = View.VISIBLE

                    true
                }
                R.id.item4 -> {
                    button.setText("Volleyball")
                    //button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.volley_icon, 0,0,0)
                    vm.getCourtBySport("Volleyball")
                    deleteCurrentSport.visibility = View.VISIBLE
                    true
                }
                R.id.item5 -> {
                    button.setText("Basketball")
                    //button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.basket_icon, 0,0,0)
                    vm.getCourtBySport("Basketball")
                    deleteCurrentSport.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

        button.setOnClickListener {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
            popupMenu.show()
        }

        button2.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            )

            val today = Calendar.getInstance()
            datePicker.datePicker.setMinDate(today.timeInMillis)
            datePicker.show()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = CourtAdapter(vm, button2)
        recyclerView.adapter = adapter
        vm.getBusySlots(date)
        vm.getCourts()
        vm.getFavoriteCourts()

        vm.busyTimeSlots.observe(this) {
            adapter._setBusyTimeSlots(it)
        }

        vm.favoriteList.observe(this){
            adapter._setFavoriteCourts(it)
        }

        vm.courts_.observe(this){
            adapter.setCourt(it)
        }

        val backArrowButton = findViewById<ImageButton>(R.id.backButton)
        backArrowButton.setOnClickListener{
            finish()
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        displayFormattedDate(calendar.timeInMillis)
    }

    fun displayFormattedDate(timestamp: Long) {
       // Log.d("data", formatter.format(timestamp))
        date = formatter.format(timestamp)
        findViewById<Button>(R.id.button2).setText(convertDateToDisplay_(date, false))
        vm.getBusySlots(date)
        val button_time_slot = findViewById<Button>(R.id.button_time_slot)
        if (button_time_slot != null) {
            button_time_slot.setText("Choose time slot")
        }
        findViewById<RecyclerView>(R.id.recyclerView).adapter?.notifyDataSetChanged()
    }
}








