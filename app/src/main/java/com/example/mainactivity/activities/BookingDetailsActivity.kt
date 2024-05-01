package com.example.mainactivity.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.commit
import com.example.mainactivity.*
import com.example.mainactivity.fragments.SearchPlayersFragment
import com.example.mainactivity.models.FireBooking
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.viewmodel.BookingDetailsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL


class BookingDetailsActivity : AppCompatActivity() {
    private lateinit var viewModel: BookingDetailsViewModel
    private lateinit var courtName: TextView
    private lateinit var courtAddress: TextView
    private lateinit var courtImage: ImageView
    private lateinit var bookingDate: TextView
    private lateinit var timeSlotStart: TextView
    private lateinit var timeSlotEnd: TextView
    private lateinit var price: TextView
    private lateinit var additionalRequests: TextView
    private lateinit var sportName: TextView
    private lateinit var sportImage: ImageView
    private lateinit var deleteButton: Button
    private lateinit var backButton: ImageButton

    var whiteTeamMemberImages: Array<ShapeableImageView> = arrayOf()
    private var whiteTeamMemberNames: Array<TextView> = arrayOf()
    var whiteTeamUsers: MutableList<FireUser> = mutableListOf()

    var blackTeamMemberImages: Array<ShapeableImageView> = arrayOf()
    private var blackTeamMemberNames: Array<TextView> = arrayOf()
    var blackTeamUsers: MutableList<FireUser> = mutableListOf()

    var whiteTeamMemberDelete: Array<FloatingActionButton> = arrayOf()
    var blackTeamMemberDelete: Array<FloatingActionButton> = arrayOf()

    private var whiteAddIndex = 0
    private var blackAddIndex = 0

    private var maxTeamMembers = 6

    private lateinit var whiteSwitchTeams: ShapeableImageView
    private lateinit var blackSwitchTeams: ShapeableImageView
    private var myTeam = "whiteTeam"
    private var isOrganizer = false
    private var organizerEmail = ""
    private var organizerTeam = ""

    private var pendingWhiteIndex = 0
    private var pendingBlackIndex = 0
    lateinit var booking: FireBooking
    private lateinit var editTeamsButton: Button
    private lateinit var confirmEditButton: Button
    private lateinit var cancelEditButton: Button
    private var teamMembers = 6

    private lateinit var mLoading: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_details)


        // for the loading screen
        setUIref()
        showLoading()

        // get booking id from intent
        val b: Bundle? = intent.extras
        var bookingId = 2
        if (b != null) {
            bookingId = b.getLong("bookingId").toInt()
        }

        supportActionBar?.title = "Reservation Details"
        courtName = findViewById(R.id.courtName)
        courtAddress = findViewById(R.id.courtAddress)
        courtImage = findViewById(R.id.courtImage)
        bookingDate = findViewById(R.id.reservationDate)
        timeSlotStart = findViewById(R.id.timeStart)
        timeSlotEnd = findViewById(R.id.timeEnd)
        price = findViewById(R.id.price)
        additionalRequests = findViewById(R.id.additionalRequests)
        sportName = findViewById(R.id.sportName)
        sportImage = findViewById(R.id.sportIcon)

        // get the data
        viewModel = BookingDetailsViewModel(application, bookingId)
        viewModel.getBooking()

        observeData()

        whiteTeamMemberImages = arrayOf(
            findViewById(R.id.whiteTeamImage1),
            findViewById(R.id.whiteTeamImage2),
            findViewById(R.id.whiteTeamImage3),
            findViewById(R.id.whiteTeamImage4),
            findViewById(R.id.whiteTeamImage5),
            findViewById(R.id.whiteTeamImage6)
        )
        whiteTeamMemberNames = arrayOf(
            findViewById(R.id.whiteTeamText1),
            findViewById(R.id.whiteTeamText2),
            findViewById(R.id.whiteTeamText3),
            findViewById(R.id.whiteTeamText4),
            findViewById(R.id.whiteTeamText5),
            findViewById(R.id.whiteTeamText6),
        )
        blackTeamMemberImages = arrayOf(
            findViewById(R.id.blackTeamImage1),
            findViewById(R.id.blackTeamImage2),
            findViewById(R.id.blackTeamImage3),
            findViewById(R.id.blackTeamImage4),
            findViewById(R.id.blackTeamImage5),
            findViewById(R.id.blackTeamImage6),
        )
        blackTeamMemberNames = arrayOf(
            findViewById(R.id.blackTeamText1),
            findViewById(R.id.blackTeamText2),
            findViewById(R.id.blackTeamText3),
            findViewById(R.id.blackTeamText4),
            findViewById(R.id.blackTeamText5),
            findViewById(R.id.blackTeamText6)
        )

        whiteTeamMemberDelete = arrayOf(
            findViewById(R.id.whiteTeamDelete1),
            findViewById(R.id.whiteTeamDelete2),
            findViewById(R.id.whiteTeamDelete3),
            findViewById(R.id.whiteTeamDelete4),
            findViewById(R.id.whiteTeamDelete5),
            findViewById(R.id.whiteTeamDelete6)
        )
        blackTeamMemberDelete = arrayOf(
            findViewById(R.id.blackTeamDelete1),
            findViewById(R.id.blackTeamDelete2),
            findViewById(R.id.blackTeamDelete3),
            findViewById(R.id.blackTeamDelete4),
            findViewById(R.id.blackTeamDelete5),
            findViewById(R.id.blackTeamDelete6)
        )

        // initialize delete player buttons
        blackTeamMemberDelete.forEach {
            it.visibility = View.GONE
        }
        whiteTeamMemberDelete.forEach {
            it.visibility = View.GONE
        }

        editTeamsButton = findViewById(R.id.editTeamsButton)
        confirmEditButton = findViewById(R.id.confirmEditButton)
        cancelEditButton = findViewById(R.id.cancelEditButton)

        editTeamsButton.visibility = View.GONE

        // whiteTeamUsers.add(LoggedInUser.user) // by default, organizer is in the white team.
        // initialize user profile pic
        val loggedInUser = LoggedInUser.user
        val loggedInPic = LoggedInUser.user.pictureUri
        // whiteTeamMemberNames[0].text = loggedInUser.name.split(" ")[0] + " (You)"

        // switch teams logic
        whiteSwitchTeams = findViewById(R.id.whiteTeamSwapOverlay)
        blackSwitchTeams = findViewById(R.id.blackTeamSwapOverlay)


        deleteButton = findViewById(R.id.deleteButton)
        val alertBuilder = createAlert()
        deleteButton.setOnClickListener {
            alertBuilder.show()
        }
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.bookingLeaveReview).setOnClickListener {
            val intent = Intent(this, CourtDetailsActivity::class.java)
            intent
                .putExtra("booking_id", bookingId)
                .putExtra("court_id", viewModel.court_.value?.court_id)
                .putExtra("booking_date", viewModel.booking.value?.date)
            startActivity(intent)
        }

    }

    private fun observeData() {

        viewModel.booking.observe(this) {
            // get organizer
            organizerEmail = it.user_email
            organizerTeam = it.teams[organizerEmail].toString()

            if (organizerEmail == LoggedInUser.user.email) {
                isOrganizer = true
            }

            additionalRequests.text = it.user_request
            if (it.user_request == "") {
                additionalRequests.visibility = GONE
                findViewById<TextView>(R.id.additionalRequestsTitle).visibility = GONE
            }
            bookingDate.text = convertDateToDisplay(it.date, true)
            timeSlotStart.text = setStartTimeslot(it.time_slot_id)
            timeSlotEnd.text = setEndTimeslot(it.time_slot_id)
            viewModel.getCourt()

            if(today()<=it.date) {
                findViewById<TextView>(R.id.bookingLeaveReview).visibility = GONE
                findViewById<TextView>(R.id.deleteButton).visibility = VISIBLE
            } else {
                findViewById<TextView>(R.id.bookingLeaveReview).visibility = VISIBLE
                findViewById<TextView>(R.id.deleteButton).visibility = GONE
            }
            booking = it
            // populate teams
            it.teams.forEach { it2 ->
                var email = it2.key
                var team = it2.value
                // fetch user data
                viewModel.getUser(email)
                viewModel.getProfilePic(email)
            }

            // get pending users
            viewModel.getInviteStatus()

        }

        viewModel.users.observe(this) {
            for (user in it) {
                if (booking.teams[user!!.email] == "whiteTeam") {
                    whiteTeamUsers.add(user)
                    pendingWhiteIndex = whiteAddIndex
                    whiteTeamMemberNames[whiteAddIndex].text = user.name
                    whiteTeamMemberNames[whiteAddIndex].visibility = VISIBLE
                    // make organizer name bold
                    if (user.email == organizerEmail) {
                        val typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_bold)
                        whiteTeamMemberNames[whiteAddIndex].typeface =  typeface
                    }
                    // highlight logged in player
                    if (user.email == LoggedInUser.user.email) {
                        var typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_regularitalic)
                        if (user.email == organizerEmail) {
                            typeface = ResourcesCompat.getFont(applicationContext, R.font.sf_pro_display_mediumitalic)
                        }
                        whiteTeamMemberNames[whiteAddIndex].typeface =  typeface
                    }
                    whiteAddIndex += 1

                } else {
                    blackTeamUsers.add(user)
                    pendingBlackIndex = blackAddIndex
                    blackTeamMemberNames[blackAddIndex].text = user.name
                    blackTeamMemberNames[blackAddIndex].visibility = VISIBLE
                    // make organizer name bold
                    if (user.email == organizerEmail) {
                        val typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_bold)
                        whiteTeamMemberNames[whiteAddIndex].typeface =  typeface
                    }
                    // highlight logged in player
                    if (user.email == LoggedInUser.user.email) {
                        var typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_regularitalic)
                        if (user.email == organizerEmail) {
                            typeface = ResourcesCompat.getFont(applicationContext, R.font.sf_pro_display_bolditalic)
                        }
                        whiteTeamMemberNames[whiteAddIndex].typeface =  typeface
                    }
                    blackAddIndex += 1
                }
            }
        }

        viewModel.profilePics.observe(this) {
            it.forEach {it2 ->
                for (user in whiteTeamUsers) {
                    if (user.email == it2.key) {
                        val idx = whiteTeamUsers.indexOf(user)
                        whiteTeamMemberImages[idx].setImageBitmap(it2.value)
                        // change properties of image
                        whiteTeamMemberImages[idx].alpha = 1.0F
                        whiteTeamMemberImages[idx].visibility = VISIBLE
                        whiteTeamMemberImages[idx].imageTintList = null

                    }
                }
                for (user in blackTeamUsers) {
                    if (user.email == it2.key) {
                        val idx = blackTeamUsers.indexOf(user)
                        blackTeamMemberImages[idx].setImageBitmap(it2.value)
                        // change properties of image
                        blackTeamMemberImages[idx].alpha = 1.0F
                        blackTeamMemberImages[idx].visibility = VISIBLE
                        blackTeamMemberImages[idx].imageTintList = null

                    }
                }
            }
        }

        viewModel.court_.observe(this) {
            courtName.text = it.name
            courtAddress.text = it.address
            sportName.text = it.sport
            setSportColors(it.sport) // set correct sport colors
            teamMembers = getTeamMembersForSport(it.sport)
            for (i in teamMembers until whiteTeamMemberImages.size) {
                whiteTeamMemberImages[i].visibility = View.INVISIBLE
                whiteTeamMemberNames[i].visibility = View.INVISIBLE
                blackTeamMemberImages[i].visibility = View.INVISIBLE
                blackTeamMemberNames[i].visibility = View.INVISIBLE
            }
            price.text = formatPrice(it.price)
            viewModel.getCourtImage() // old way: local files only
            viewModel.getCourtImageFilename()
            viewModel.getSportImage()
        }

        viewModel.courtImagePath.observe(this) {
            viewModel.setCourtImageCloud()
        }
        viewModel.courtImageBitmap.observe(this) {
            courtImage.setImageBitmap(it)
        }

        viewModel.courtImageId.observe(this) {
            courtImage.setImageResource(it)
        }

        viewModel.sportImageId.observe(this) {
            sportImage.setImageResource(it)
        }

        viewModel.inviteStatuses.observe(this) {
            Log.d("a", it.toString())
            viewModel.getPendingUsers()
        }
        viewModel.pendingUsers.observe(this) {
            Log.d("PUT", "useri " + viewModel.pendingUsers.value.toString())
            for (p in it) {
                if (p != null) {
                    if (viewModel.pendingUserTeams.value?.get(p.email) == "whiteTeam") {
                        whiteTeamUsers.add(p)
                        whiteTeamMemberNames[whiteAddIndex].text = p.name
                        whiteTeamMemberNames[whiteAddIndex].alpha = 0.5F
                        whiteTeamMemberNames[whiteAddIndex].visibility = VISIBLE
                        whiteAddIndex += 1
                    } else {
                        blackTeamUsers.add(p)
                        blackTeamMemberNames[blackAddIndex].text = p.name
                        blackTeamMemberNames[blackAddIndex].alpha = 0.5F
                        blackTeamMemberNames[blackAddIndex].visibility = VISIBLE
                        blackAddIndex += 1
                    }
                }
            }
            // set names, then
            viewModel.getPendingProfilePics()
        }
        viewModel.pendingProfilePics.observe(this) {
            // set profile pics for pending users
            it.forEach {it2 ->
                for (user in whiteTeamUsers) {
                    if (user.email == it2.key) {
                        val idx = whiteTeamUsers.indexOf(user)
                        whiteTeamMemberImages[idx].setImageBitmap(it2.value)
                        // change properties of image
                        whiteTeamMemberImages[idx].alpha = 0.5F
                        whiteTeamMemberImages[idx].visibility = VISIBLE
                        whiteTeamMemberImages[idx].imageTintList = null

                    }
                }
                for (user in blackTeamUsers) {
                    Log.d("pendinghi", user.email)
                    Log.d("immagina", it2.key)
                    if (user.email == it2.key) {
                        Log.d("aiuto", "setto immagine")
                        val idx = blackTeamUsers.indexOf(user)
                        blackTeamMemberImages[idx].setImageBitmap(it2.value)
                        // change properties of image
                        blackTeamMemberImages[idx].alpha = 0.5F
                        blackTeamMemberImages[idx].visibility = VISIBLE
                        blackTeamMemberImages[idx].imageTintList = null

                    }
                }
            }
        }

        viewModel.areTherePendingUsers.observe(this) {
            // hide loading
            hideLoading()
            // now that the loading is all done, users can be edited
            if (isOrganizer) {
                // setup team editing functionality
                editTeamsButton.visibility = GONE // change to VISIBLE if implementing (no time)
                editTeamsButton.setOnClickListener {
                    // add team editing capabilities
                    // make delete buttons visible
                    for (i in 0 until whiteTeamUsers.size) {
                        // can't delete yourself
                        if (whiteTeamUsers[i].email != LoggedInUser.user.email) {
                            whiteTeamMemberDelete[i].visibility = VISIBLE
                        }
                    }
                    for (i in 0 until blackTeamUsers.size) {
                        if (blackTeamUsers[i].email != LoggedInUser.user.email) {
                            blackTeamMemberDelete[i].visibility = VISIBLE
                        }
                    }
                    // TODO: add delete buttonclicklisteners for all users.

                    editTeamsButton.visibility = GONE
                    confirmEditButton.visibility = VISIBLE
                    cancelEditButton.visibility = VISIBLE
                    // set onclicklisteners for confirm and cancel
                    cancelEditButton.setOnClickListener {
                        // restore edit teams button
                        cancelEditButton.visibility = GONE
                        confirmEditButton.visibility = GONE

                        // make delete buttons invisible
                        for (i in 0 until teamMembers) {
                            whiteTeamMemberDelete[i].visibility = GONE
                            blackTeamMemberDelete[i].visibility = GONE
                        }
                        // bring everything back as it was: users, names, profile pictures
                        for (i in 0 until whiteAddIndex) {
                            whiteTeamMemberImages[i].setImageBitmap(
                                viewModel.profilePics.value!![whiteTeamUsers[i].email])
                            if (viewModel.pendingProfilePics.value != null) {
                                if (viewModel.pendingProfilePics.value!!.keys.contains(whiteTeamUsers[i].email)) {
                                    whiteTeamMemberImages[i].alpha = 0.5F
                                } else {
                                    whiteTeamMemberImages[i].alpha = 1.0F
                                }
                            } else {
                                whiteTeamMemberImages[i].alpha = 1.0F
                            }
                            whiteTeamMemberImages[i].imageTintList = null
                            whiteTeamMemberNames[i].text = whiteTeamUsers[i].name
                            whiteTeamMemberNames[i].visibility = VISIBLE
                            // change font weight
                            // make organizer name bold
                            if (whiteTeamUsers[i].email == organizerEmail) {
                                val typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_bold)
                                whiteTeamMemberNames[i].typeface =  typeface
                            }
                            // highlight logged in player
                            if (whiteTeamUsers[i].email == LoggedInUser.user.email) {
                                var typeface = ResourcesCompat.getFont(
                                    applicationContext,
                                    R.font.sf_pro_display_regularitalic
                                )
                                if (whiteTeamUsers[i].email == organizerEmail) {
                                    typeface = ResourcesCompat.getFont(
                                        applicationContext,
                                        R.font.sf_pro_display_mediumitalic
                                    )
                                }
                                whiteTeamMemberNames[i].typeface = typeface
                            }
                        }
                        // black team
                        for (i in 0 until blackAddIndex) {
                            blackTeamMemberImages[i].setImageBitmap(
                                viewModel.profilePics.value!![blackTeamUsers[i].email])
                            if (viewModel.pendingProfilePics.value != null) {
                                if (viewModel.pendingProfilePics.value!!.keys.contains(
                                        blackTeamUsers[i].email
                                    )) {
                                    blackTeamMemberImages[i].alpha = 0.5F
                                } else {
                                    blackTeamMemberImages[i].alpha = 1.0F
                                }
                            } else {
                                blackTeamMemberImages[i].alpha = 1.0F
                            }
                            blackTeamMemberImages[i].imageTintList = null
                            blackTeamMemberNames[i].text = blackTeamUsers[i].name
                            blackTeamMemberNames[i].visibility = VISIBLE
                            // change font weight
                            // make organizer name bold
                            if (blackTeamUsers[i].email == organizerEmail) {
                                val typeface = ResourcesCompat.getFont(applicationContext,R.font.sf_pro_display_bold)
                                blackTeamMemberNames[i].typeface =  typeface
                            }
                            // highlight logged in player
                            if (blackTeamUsers[i].email == LoggedInUser.user.email) {
                                var typeface = ResourcesCompat.getFont(
                                    applicationContext,
                                    R.font.sf_pro_display_regularitalic
                                )
                                if (blackTeamUsers[i].email == organizerEmail) {
                                    typeface = ResourcesCompat.getFont(
                                        applicationContext,
                                        R.font.sf_pro_display_mediumitalic
                                    )
                                }
                                blackTeamMemberNames[i].typeface = typeface
                            }
                        }
                    }
                    confirmEditButton.setOnClickListener {
                        // TODO: viewmodel: POST changes to booking, send/revoke invitations
                    }
                    // show correct switch teams overlay (maybe segare)
                }
            }
        }


    }

    private fun formatPrice(price: Double): String {
        var stringPrice = price.toBigDecimal().toPlainString() // needed to ensure fixed-point representation.
        val nOfDecimals = stringPrice.length - stringPrice.indexOf(".") - 1
        if (nOfDecimals != 2) {
            stringPrice = stringPrice.plus("0")
        }
        return stringPrice.plus(" €")
    }

    private fun formatDate(date: String): String {
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val dateArray = date.split("-")
        val monthIndex = dateArray[1].toInt() - 1 // get month as index
        val monthName = months[monthIndex]
        return (dateArray[2]).plus(" ").plus(monthName).plus(", ").plus(dateArray[0])
    }

    private fun setStartTimeslot(timeSlotId: Int): String {
        val startTimeslotArray = arrayOf("9:00 AM", "10:30 AM", "12:00 PM", "3:00 PM", "4:30 PM", "6:00 PM")
        return startTimeslotArray[timeSlotId - 1] // timeSlotId are indexed starting from 1
    }
    private fun setEndTimeslot(timeSlotId: Int): String {
        val endTimeslotArray = arrayOf("10:30 AM", "12:00 PM", "3:00 PM", "4:30 PM", "6:00 PM", "7:30 PM")
        return endTimeslotArray[timeSlotId - 1] // timeSlotId are indexed starting from 1
    }

    private fun createAlert(): AlertDialog.Builder {
        return AlertDialog.Builder(this).let {
            it.setTitle("Delete booking")
            it.setMessage("Are you sure you want to delete this booking?")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            it.setPositiveButton(android.R.string.yes) { _, _ ->
                // delete reservation
                viewModel.deleteBooking {
                    // This code will be executed after the deletion query is finished
                    val i = Intent(this, ShowBookingActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    //i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    finish()
                }
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.toast_custom_delete, null)
                val toastText = layout.findViewById<TextView>(R.id.toast_text)
                toastText.text = " Booking deleted"

                val toast = Toast(applicationContext)
                toast.duration = Toast.LENGTH_SHORT
                toast.view = layout
                toast.show()
            }

            it.setNegativeButton(android.R.string.no) { _, _ ->
                // go back
            }

        }
    }

    private fun setSportColors(name: String) {
        when(name) {
            "Basketball" -> {
                sportImage.setImageColorRes(R.color.basket_color)
                sportName.setBackgroundColorRes(R.color.basket_color)
            }
            "Football" -> {
                sportImage.setImageColorRes(R.color.football_color)
                sportName.setBackgroundColorRes(R.color.football_color)
            }
            "Tennis" -> {
                sportImage.setImageColorRes(R.color.tennis_color)
                sportName.setBackgroundColorRes(R.color.tennis_color)
            }
            "Volleyball" -> {
                sportImage.setImageColorRes(R.color.volley_color)
                sportName.setBackgroundColorRes(R.color.volley_color)
            }
            "Padel" -> {
                sportImage.setImageColorRes(R.color.padel_color)
                sportName.setBackgroundColorRes(R.color.padel_color)
            }
        }
    }

    fun updateTeam(teamString: String, position: Int, user: FireUser) {
        val teamImages: Array<ShapeableImageView>
        val teamNames: Array<TextView>
        val teamDeleteButtons: Array<FloatingActionButton>
        var addIndex: Int

        if (teamString == "whiteTeam") {
            teamImages = whiteTeamMemberImages
            teamNames = whiteTeamMemberNames
            teamDeleteButtons = whiteTeamMemberDelete
            addIndex = whiteAddIndex

            // add user to right team
            whiteTeamUsers.add(user)
        } else {
            teamImages = blackTeamMemberImages
            teamNames = blackTeamMemberNames
            teamDeleteButtons = blackTeamMemberDelete
            addIndex = blackAddIndex

            // add user to black team
            blackTeamUsers.add(user)
        }

        // update name and picture
        // Fetch and display the user's pictureUri using coroutines
        val latestIndex = addIndex
        Storage.getProfilePic(user.email) {
            teamImages[latestIndex].setImageBitmap(it)
        }
        /*GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                user.pictureUri.let {
                    try {
                        val inputStream: InputStream = URL(user.pictureUri).openStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        withContext(Dispatchers.Main) {
                            teamImages[latestIndex].setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }*/
        teamImages[addIndex].setImageResource(R.drawable.user_pic_round)
        teamNames[addIndex].alpha = 0.5F
        ImageViewCompat.setImageTintList(teamImages[addIndex], ColorStateList.valueOf(Color.argb(64, 255, 255, 255)))
        teamNames[addIndex].text = user.name

        // remove onclicklistener
        teamImages[addIndex].setOnClickListener(null)

        // add delete button visibility
        teamDeleteButtons[addIndex].visibility = VISIBLE
        // add delete button functionality
        teamDeleteButtons[addIndex].setOnClickListener {
            val myId = it.id
            deleteTeamMember(myId)
        }

        // set up next team member
        addIndex += 1
        if (addIndex < maxTeamMembers) {
            teamImages[addIndex].setImageResource(R.drawable.add_friend_icon_48)
            teamNames[addIndex].text = "Add Friend"
            teamNames[addIndex].visibility = VISIBLE
            ImageViewCompat.setImageTintList(
                teamImages[addIndex],
                ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
            )

            teamImages[addIndex].setOnClickListener {
                // open search players fragment
                // vm.booking_team.postValue(teamString)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setCustomAnimations(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom
                    )
                    add(R.id.searchPlayersFragmentView, SearchPlayersFragment(teamString, addIndex))
                }

            }
        }

        // update swap overlays
        if ((teamString == "whiteTeam") and (teamString != myTeam) and (addIndex >= maxTeamMembers)) {
            blackSwitchTeams.visibility = View.GONE
        } else if ((teamString == "blackTeam") and (teamString != myTeam) and (addIndex >= maxTeamMembers)) {
            whiteSwitchTeams.visibility = View.GONE
        }
        // restore indices
        if (teamString == "whiteTeam") {
            whiteAddIndex = addIndex
        } else {
            blackAddIndex = addIndex
        }
    }

    fun deleteTeamMember(id: Int) {
        val teamString: String
        val index: Int
        // determine member's team and position
        if (whiteTeamMemberDelete.contains(findViewById(id))) {
            teamString = "whiteTeam"
            index = whiteTeamMemberDelete.indexOf(findViewById(id))

            // delete from users
            var user = whiteTeamUsers.removeAt(index)
            // vm.deleteUserFromBooking(user.email)

        } else if (blackTeamMemberDelete.contains(findViewById(id))) {
            teamString = "blackTeam"
            index = blackTeamMemberDelete.indexOf(findViewById(id))

            // delete from users
            var user = blackTeamUsers.removeAt(index)
            // vm.deleteUserFromBooking(user.email)
        } else {
            Log.d("a", "Something went wrong with deleting.")
            return
        }

        val teamImages: Array<ShapeableImageView>
        val teamNames: Array<TextView>
        val deleteButtons: Array<FloatingActionButton>
        val addIndex: Int

        if (teamString == "whiteTeam") {
            teamImages = whiteTeamMemberImages
            teamNames = whiteTeamMemberNames
            deleteButtons = whiteTeamMemberDelete
            addIndex = whiteAddIndex
        } else {
            teamImages = blackTeamMemberImages
            teamNames = blackTeamMemberNames
            deleteButtons = blackTeamMemberDelete
            addIndex = blackAddIndex
        }

        // shift all other members
        for (i in index..addIndex) {
            teamImages[i].alpha = teamImages[i+1].alpha
            teamImages[i].setImageDrawable(teamImages[i+1].drawable)
            teamNames[i].alpha = teamNames[i+1].alpha
            teamNames[i].text = teamNames[i+1].text
            teamNames[i].visibility = teamNames[i+1].visibility
            ImageViewCompat.setImageTintList(teamImages[i], teamImages[i+1].imageTintList)
            teamImages[i].setOnClickListener(null) // restore them later
        }
        // toggle visibility of delete button (delete the last one)
        deleteButtons[addIndex-1].visibility = View.GONE
        // manage the last one (add button with onclicklistener)
        teamImages[addIndex-1].alpha = 1.0F
        teamImages[addIndex-1].setImageResource(R.drawable.add_friend_icon_48)
        teamNames[addIndex-1].alpha = 1.0F
        teamNames[addIndex-1].visibility = VISIBLE
        teamNames[addIndex-1].text = "Add Friend"
        teamImages[addIndex-1].imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
        teamImages[addIndex-1].setOnClickListener {
            // open search players fragment
            // vm.booking_team.postValue(teamString)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add(R.id.searchPlayersFragmentView, SearchPlayersFragment(teamString, addIndex-1))
            }
        }


        // update indices & restore swap overlay
        if ((teamString == "whiteTeam") and (teamString != myTeam)) {
            // can switch
            blackSwitchTeams.visibility = VISIBLE
        } else if ((teamString == "blackTeam") and (teamString != myTeam)) {
            whiteSwitchTeams.visibility = VISIBLE
        }
        if (teamString == "whiteTeam") {
            whiteAddIndex -= 1
        } else {
            blackAddIndex -= 1
        }
    }

    fun switchTeams(destinationTeam: String) {
        var opposingTeam: String
        var teamImages = arrayOf<ShapeableImageView>()
        var opposingTeamImages = arrayOf<ShapeableImageView>()
        var teamNames = arrayOf<TextView>()
        var opposingTeamNames = arrayOf<TextView>()
        var deleteButtons = arrayOf<FloatingActionButton>()
        var opposingDeleteButtons = arrayOf<FloatingActionButton>()
        var addIndex: Int
        var opposingAddIndex: Int
        var overlay: ShapeableImageView
        var opposingOverlay: ShapeableImageView

        if (destinationTeam == "blackTeam") {
            whiteSwitchTeams.visibility = View.GONE
            blackSwitchTeams.visibility = VISIBLE
            teamImages = blackTeamMemberImages
            teamNames = blackTeamMemberNames
            deleteButtons = blackTeamMemberDelete
            opposingTeamImages = whiteTeamMemberImages
            opposingTeamNames = whiteTeamMemberNames
            opposingDeleteButtons = whiteTeamMemberDelete
            addIndex = blackAddIndex
            opposingAddIndex = whiteAddIndex
            opposingTeam = "whiteTeam"
            val user = whiteTeamUsers.removeAt(0)
            blackTeamUsers.add(0, user)

        } else {
            blackSwitchTeams.visibility = View.GONE
            whiteSwitchTeams.visibility = VISIBLE
            teamImages = whiteTeamMemberImages
            teamNames = whiteTeamMemberNames
            deleteButtons = whiteTeamMemberDelete
            opposingTeamImages = blackTeamMemberImages
            opposingTeamNames = blackTeamMemberNames
            opposingDeleteButtons = blackTeamMemberDelete
            addIndex = whiteAddIndex
            opposingAddIndex = blackAddIndex
            opposingTeam = "blackTeam"
            val user = blackTeamUsers.removeAt(0)
            whiteTeamUsers.add(0, user)
        }

        if (addIndex < maxTeamMembers) {
            // save player imageview
            var playerImageView = opposingTeamImages[0]
            var playerText = opposingTeamNames[0]
            var playerDelete = opposingDeleteButtons[0]

            if (addIndex < maxTeamMembers - 1) {
                // handle add separately, otherwise do nothing
                teamImages[addIndex+1].alpha = teamImages[addIndex].alpha
                teamImages[addIndex+1].setImageDrawable(teamImages[addIndex].drawable)
                teamNames[addIndex+1].alpha = teamNames[addIndex].alpha
                teamNames[addIndex+1].text = teamNames[addIndex].text
                teamNames[addIndex+1].visibility = teamNames[addIndex].visibility
                ImageViewCompat.setImageTintList(teamImages[addIndex+1], teamImages[addIndex].imageTintList)
                teamImages[addIndex+1].setOnClickListener(null) // restore them later
                teamImages[addIndex].setOnClickListener(null) // restore them later
                deleteButtons[addIndex+1].visibility = deleteButtons[addIndex].visibility
            }
            // shift all members right for destination team
            for (i in (addIndex - 1) downTo 0) {
                teamImages[i+1].alpha = teamImages[i].alpha
                teamImages[i+1].setImageDrawable(teamImages[i].drawable)
                teamNames[i+1].alpha = teamNames[i].alpha
                teamNames[i+1].text = teamNames[i].text
                teamNames[i+1].visibility = teamNames[i].visibility
                ImageViewCompat.setImageTintList(teamImages[i+1], teamImages[i].imageTintList)
                teamImages[i+1].setOnClickListener(null) // restore them later
                teamImages[i].setOnClickListener(null) // restore them later
                deleteButtons[i+1].visibility = deleteButtons[i].visibility
            }

            // restore player
            teamImages[0].alpha = playerImageView.alpha
            teamImages[0].setImageDrawable(playerImageView.drawable)
            teamNames[0].alpha = playerText.alpha
            teamNames[0].text = playerText.text
            teamNames[0].visibility = playerText.visibility
            deleteButtons[0].visibility = View.GONE
            ImageViewCompat.setImageTintList(teamImages[0], playerImageView.imageTintList)

            // shift all members left for source team
            for (i in 0 until opposingAddIndex) {
                opposingTeamImages[i].alpha = opposingTeamImages[i+1].alpha
                opposingTeamImages[i].setImageDrawable(opposingTeamImages[i+1].drawable)
                opposingTeamNames[i].alpha = opposingTeamNames[i+1].alpha
                opposingTeamNames[i].text = opposingTeamNames[i+1].text
                opposingTeamNames[i].visibility = opposingTeamNames[i+1].visibility
                ImageViewCompat.setImageTintList(opposingTeamImages[i], opposingTeamImages[i+1].imageTintList)
                opposingTeamImages[i].setOnClickListener(null) // restore them later
                opposingTeamImages[i+1].setOnClickListener(null) // restore them later
                opposingDeleteButtons[i].visibility = opposingDeleteButtons[i+1].visibility
            }
            if (opposingAddIndex == maxTeamMembers) {
                // handle add separately
                opposingTeamImages[opposingAddIndex-1].alpha = 1.0F
                opposingTeamImages[opposingAddIndex-1].setImageResource(R.drawable.add_friend_icon_48)
                opposingTeamNames[opposingAddIndex-1].text = "Add Friend"
                opposingTeamNames[opposingAddIndex-1].visibility = VISIBLE
                ImageViewCompat.setImageTintList(opposingTeamImages[opposingAddIndex-1], ColorStateList.valueOf(
                    ContextCompat.getColor(applicationContext, R.color.blue)))
                opposingTeamImages[opposingAddIndex-1].setOnClickListener(null) // restore them later
                opposingDeleteButtons[opposingAddIndex-1].visibility = View.GONE
            } else {
                opposingTeamImages[opposingAddIndex].alpha = opposingTeamImages[opposingAddIndex+1].alpha
                opposingTeamImages[opposingAddIndex].setImageDrawable(opposingTeamImages[opposingAddIndex+1].drawable)
                opposingTeamNames[opposingAddIndex].alpha = opposingTeamNames[opposingAddIndex+1].alpha
                opposingTeamNames[opposingAddIndex].text = opposingTeamNames[opposingAddIndex+1].text
                opposingTeamNames[opposingAddIndex].visibility = opposingTeamNames[opposingAddIndex+1].visibility
                ImageViewCompat.setImageTintList(opposingTeamImages[opposingAddIndex], opposingTeamImages[opposingAddIndex+1].imageTintList)
                opposingTeamImages[opposingAddIndex].setOnClickListener(null) // restore them later
                opposingTeamImages[opposingAddIndex+1].setOnClickListener(null) // restore them later
                opposingDeleteButtons[opposingAddIndex].visibility = opposingDeleteButtons[opposingAddIndex+1].visibility
            }

            // fix addIndex and OpposingAddIndex
            addIndex += 1
            opposingAddIndex -= 1

            // restore onclicklisteners
            teamImages[addIndex].setOnClickListener {
                // vm.booking_team.postValue(destinationTeam)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setCustomAnimations(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom
                    )
                    add(R.id.searchPlayersFragmentView, SearchPlayersFragment(destinationTeam, addIndex))
                }
            }
            opposingTeamImages[opposingAddIndex].setOnClickListener {
                // vm.booking_team.postValue(opposingTeam)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setCustomAnimations(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom,
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom
                    )
                    add(R.id.searchPlayersFragmentView, SearchPlayersFragment(opposingTeam, opposingAddIndex))
                }
            }

            // restore delete buttons
            for (i in 0 until addIndex) {
                deleteButtons[i].setOnClickListener {
                    val myId = it.id
                    deleteTeamMember(myId)
                }
            }
            // restore delete buttons
            for (i in 0 until opposingAddIndex) {
                opposingDeleteButtons[i].setOnClickListener {
                    val myId = it.id
                    deleteTeamMember(myId)
                }
            }

            if (destinationTeam == "blackTeam") {
                blackAddIndex = addIndex
                whiteAddIndex = opposingAddIndex
            } else {
                whiteAddIndex = addIndex
                blackAddIndex = opposingAddIndex
            }

        }
    }

    private fun setUIref() {
        // Create a Instance of the Loading Layout
        mLoading = findViewById(R.id.my_loading_layout)
    }

    private fun showLoading() {
        /*Call this function when you want progress dialog to appear*/
        mLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        mLoading.visibility = View.GONE
    }

}