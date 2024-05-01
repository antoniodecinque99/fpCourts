package com.example.mainactivity.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.commit
import com.example.mainactivity.*
import com.example.mainactivity.fragments.SearchPlayersFragment
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.models.TimeSlot
import com.example.mainactivity.viewmodel.ConfirmBookingViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ConfirmBookingActivity : AppCompatActivity() {
    val vm by viewModels<ConfirmBookingViewModel>()

    var whiteTeamMemberImages: Array<ShapeableImageView> = arrayOf()
    private var whiteTeamMemberNames: Array<TextView> = arrayOf()
    var whiteTeamUsers: MutableList<FireUser> = mutableListOf()

    var blackTeamMemberImages: Array<ShapeableImageView> = arrayOf()
    private var blackTeamMemberNames: Array<TextView> = arrayOf()
    var blackTeamUsers: MutableList<FireUser> = mutableListOf()

    var whiteTeamMemberDelete: Array<FloatingActionButton> = arrayOf()
    var blackTeamMemberDelete: Array<FloatingActionButton> = arrayOf()

    private var whiteAddIndex = 1
    private var blackAddIndex = 0

    private var maxTeamMembers = 6

    private lateinit var whiteSwitchTeams: ShapeableImageView
    private lateinit var blackSwitchTeams: ShapeableImageView
    private var myTeam = "whiteTeam"

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_booking)
        val intent = intent
        vm.setTimeslot(intent.getIntExtra("TIMESLOT_ID", 0))
        vm.setCourt(intent.getIntExtra("COURT_ID", 0))
        intent.getStringExtra("DATE")?.let { vm.setDate(it) }

        val button_white = findViewById<Button>(R.id.white_add_button)
        val button_black = findViewById<Button>(R.id.black_add_button)


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

        whiteTeamUsers.add(LoggedInUser.user) // by default, organizer is in the white team.

        // initialize user profile pic
        val loggedInUser = LoggedInUser.user
        val loggedInPic = LoggedInUser.user.pictureUri
        Storage.getProfilePic(LoggedInUser.user.email) {
            whiteTeamMemberImages[0].setImageBitmap(it)
        }
        /*GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                loggedInPic.let {
                    try {
                        val inputStream: InputStream = URL(loggedInPic).openStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        withContext(Dispatchers.Main) {
                            whiteTeamMemberImages[0].setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }*/
        whiteTeamMemberNames[0].text = loggedInUser.name.split(" ")[0] + " (You)"

        // switch teams logic
        whiteSwitchTeams = findViewById(R.id.whiteTeamSwapOverlay)
        blackSwitchTeams = findViewById(R.id.blackTeamSwapOverlay)

        whiteSwitchTeams.setOnClickListener {
            myTeam = "blackTeam"
            switchTeams(myTeam)
        }
        blackSwitchTeams.setOnClickListener {
            myTeam = "whiteTeam"
            switchTeams(myTeam)
        }

        whiteTeamMemberImages[whiteAddIndex].setOnClickListener {
            // open search players fragment
            vm.booking_team.postValue("whiteTeam")
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add(R.id.searchPlayersFragmentView, SearchPlayersFragment("whiteTeam", whiteAddIndex))
            }

        }

        blackTeamMemberImages[blackAddIndex].setOnClickListener {
            // open search players fragment
            vm.booking_team.postValue("blackTeam")
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add(R.id.searchPlayersFragmentView, SearchPlayersFragment("blackTeam", blackAddIndex))
            }
        }


        val searchText = findViewById<EditText>(R.id.editTextSearch)
        val searchBotton = findViewById<Button>(R.id.searcButton)

        button_white.setOnClickListener {
            searchText.visibility = View.VISIBLE
            searchBotton.visibility = View.VISIBLE
            vm.booking_team.postValue("whiteTeam")

        }


        button_black.setOnClickListener {
            searchText.visibility = View.VISIBLE
            searchBotton.visibility = View.VISIBLE
            vm.booking_team.postValue("blackTeam")

        }

        searchBotton.setOnClickListener {
            vm.getUserByNick(searchText.text.toString())
        }

        searchText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                vm.getUserByNick(searchText.text.toString())
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })


        var info_regole_text = findViewById<TextView>(R.id.info_regole)
        var info_regole_expanded_text = findViewById<TextView>(R.id.info_regole_expanded)
        var expanded_regole = false
        info_regole_text.setOnClickListener {
            expanded_regole = !expanded_regole
            if (expanded_regole) {
                info_regole_expanded_text.text = getString(R.string.full_text_regole)
                info_regole_expanded_text.visibility = View.VISIBLE
                info_regole_text.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_keyboard_arrow_down_24,0)

            } else {
                info_regole_expanded_text.text = ""
                info_regole_expanded_text.visibility = View.GONE
                info_regole_text.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_keyboard_arrow_up_24,0)

            }
        }

        var info_note_text = findViewById<TextView>(R.id.info_note)
        var info_note_expanded_text = findViewById<TextView>(R.id.info_note_expanded)
        var expanded_note = false
        info_note_text.setOnClickListener {
            expanded_note = !expanded_note
            if (expanded_note) {
                info_note_expanded_text.text = getString(R.string.full_text_note)
                info_note_expanded_text.visibility = View.VISIBLE
                info_note_text.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_keyboard_arrow_down_24,0)

            } else {
                info_note_expanded_text.text = ""
                info_note_expanded_text.visibility = View.GONE
                info_note_text.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_keyboard_arrow_up_24,0)
            }
        }

/*
        val confirmBookingTabViewFragment = ConfirmBookingTabViewFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view_confirm, confirmBookingTabViewFragment)
            .commit() */

        vm.court_.observe(this) {
            findViewById<TextView>(R.id.courtName).text = it.name ?: ""
            findViewById<TextView>(R.id.courtAddress).text = it.address ?: ""
            val price: String = String.format("%.2f", it.price) + " €"
            findViewById<TextView>(R.id.price).text = price
            findViewById<TextView>(R.id.timeslot).text = vm.timeslot.value?.let { it1 ->
                TimeSlot(
                    it1
                ).toString()
            }

            val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatterOutput = DateTimeFormatter.ofPattern("dd LLLL yyyy")
            val parsed = LocalDate.parse(vm.date.value, formatterInput)
            findViewById<TextView>(R.id.reservationDate).text = parsed.format(formatterOutput)

            val sportName = findViewById<TextView>(R.id.sportName)
            sportName.text = it.sport
            sportName.setBackgroundColorRes(getSportColor(it.sport))

            // set correct number of teammates for the sport
            var teamMembers = getTeamMembersForSport(it.sport)
            maxTeamMembers = teamMembers
            for (i in teamMembers until whiteTeamMemberImages.size) {
                whiteTeamMemberImages[i].visibility = View.GONE
                whiteTeamMemberNames[i].visibility = View.GONE
                blackTeamMemberImages[i].visibility = View.GONE
                blackTeamMemberNames[i].visibility = View.GONE
            }

            val sportIcon = findViewById<ImageView>(R.id.sportIcon)
            sportIcon.setImageResource(getSportIcon(it.sport))
            sportIcon.setImageColorRes(getSportColor(it.sport))

            Storage.getCourtPic(it.court_id) {
                findViewById<ShapeableImageView>(R.id.courtImage).setImageBitmap(it)
            }
        }

        vm.bookingSaved.observe(this) {
            if (it == 1) {
                val i = Intent(this, ShowBookingActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(i)
                finish()
            }
        }

        findViewById<EditText>(R.id.additionalRequests).addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                vm.additionalRequests.postValue(p0.toString())
            }

        })

        findViewById<Button>(R.id.confirmBookingButton).setOnClickListener {
            Log.d("map", vm.map.value.toString())
            val email = LoggedInUser.user.email
            vm.saveBooking(email, myTeam)
            vm.saveCurrentBooking()
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast_custom_confirm, null)
            val toastText = layout.findViewById<TextView>(R.id.toast_text)
            toastText.text = " Booking completed correctly"

            val toast = Toast(applicationContext)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()

            finish()
        }


        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
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
        teamNames[addIndex].alpha = 0.5F
        ImageViewCompat.setImageTintList(teamImages[addIndex], ColorStateList.valueOf(Color.argb(64, 255, 255, 255)))
        teamNames[addIndex].text = user.name

        // remove onclicklistener
        teamImages[addIndex].setOnClickListener(null)

        // add delete button visibility
        teamDeleteButtons[addIndex].visibility = View.VISIBLE
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
            teamNames[addIndex].visibility = View.VISIBLE
            ImageViewCompat.setImageTintList(
                teamImages[addIndex],
                ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
            )

            teamImages[addIndex].setOnClickListener {
                // open search players fragment
                vm.booking_team.postValue(teamString)
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
            vm.deleteUserFromBooking(user.email)

        } else if (blackTeamMemberDelete.contains(findViewById(id))) {
            teamString = "blackTeam"
            index = blackTeamMemberDelete.indexOf(findViewById(id))

            // delete from users
            var user = blackTeamUsers.removeAt(index)
            vm.deleteUserFromBooking(user.email)
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

        // delete player from viewModel (TODO!)

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
        teamNames[addIndex-1].visibility = View.VISIBLE
        teamNames[addIndex-1].text = "Add Friend"
        teamImages[addIndex-1].imageTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
        teamImages[addIndex-1].setOnClickListener {
            // open search players fragment
            vm.booking_team.postValue(teamString)
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
            blackSwitchTeams.visibility = View.VISIBLE
        } else if ((teamString == "blackTeam") and (teamString != myTeam)) {
            whiteSwitchTeams.visibility = View.VISIBLE
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
            blackSwitchTeams.visibility = View.VISIBLE
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
            whiteSwitchTeams.visibility = View.VISIBLE
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
                opposingTeamNames[opposingAddIndex-1].visibility = View.VISIBLE
                ImageViewCompat.setImageTintList(opposingTeamImages[opposingAddIndex-1], ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue)))
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
                vm.booking_team.postValue(destinationTeam)
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
                vm.booking_team.postValue(opposingTeam)
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
}


