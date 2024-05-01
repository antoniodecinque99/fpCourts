package com.example.mainactivity.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.*
import com.example.mainactivity.adapters.CourtReviewsAdapter
import com.example.mainactivity.models.CourtDao
import com.example.mainactivity.models.CourtReviewDao
import com.example.mainactivity.models.PersonDao
import com.example.mainactivity.viewmodel.CourtDetailsViewModel

class CourtDetailsActivity : AppCompatActivity() {

    lateinit var db: BookingDatabase
    lateinit var reviewsDao: CourtReviewDao
    lateinit var courtsDao: CourtDao
    lateinit var personsDao: PersonDao
    val vm by viewModels<CourtDetailsViewModel>()
    val user = LoggedInUser.user
    var enabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_court_details)

        db = BookingDatabase.getDatabase(this)
        reviewsDao = db.courtReviewDao()
        courtsDao = db.courtDao()
        personsDao = db.personDao()

        val courtId = intent.getIntExtra("court_id", 0)
        val bookingId = intent.getIntExtra("booking_id", -1)
        val bookingDate = intent.getStringExtra("booking_date")
        vm.bookingId.postValue(bookingId)
        vm.bookingDate.postValue(bookingDate)
        vm.fetchCourts(courtId)

        if(bookingId != -1) {
            findViewById<TextView>(R.id.courtReview_textView).text = "Review experience"
        }

        //populate view fields
        vm.court.observe(this) {
            Storage.getCourtPic(it.court_id) {
                findViewById<ImageView>(R.id.courtDetails_courtImage).setImageBitmap(it)
            }
            findViewById<TextView>(R.id.courtDetails_courtName).text = it.name
            findViewById<TextView>(R.id.courtDetails_courtAddress).text = it.address
        }

        findViewById<ImageView>(R.id.review_star1).setOnClickListener {
            it.id
        }

        //populate reviews list
        val reviewsList = findViewById<RecyclerView>(R.id.courtReviewsList)
        reviewsList.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                LinearLayout.VERTICAL
            )
        )
        reviewsList.layoutManager = LinearLayoutManager(this)
        val listAdapter = CourtReviewsAdapter()

        vm.fetchReviews(courtId, bookingId)

        vm.userReview.observe(this) {
            val reviewEditText = findViewById<EditText>(R.id.courtDetails_reviewText)
            vm.userReviewRating.postValue(it.rating)
            reviewEditText.isEnabled=false
            setStarsEnabled(false)
            reviewEditText.setText(it.review)

            val addReviewButton = findViewById<Button>(R.id.courtDetails_addReviewButton)
            addReviewButton.text = "Edit review"
            addReviewButton.setOnClickListener {

                if(reviewEditText.isEnabled) {
                    addReviewButton.text = "Edit review"
                    setStarsEnabled(false)
                    vm.updateReview()
                } else {
                    addReviewButton.text = "Save review"
                    setStarsEnabled(true)
                }

                reviewEditText.isEnabled = !reviewEditText.isEnabled
            }
        }

        vm.reviews.observe(this) { it ->
            //get the user review, if any
            val userReview = it.filter { r ->
                if(r.user_email != user.email) {
                    false
                } else if((bookingId == -1) && (r.booking_id == null)) {
                    true
                } else bookingId == r.booking_id
            }
            var userReviewId = -1
            if(userReview.isNotEmpty()) {
                val userReviewObject = userReview.first()
                vm.userReview.postValue(userReviewObject)
                userReviewId = userReviewObject.review_id
            }

            if(it.isNotEmpty()) {
                findViewById<TextView>(R.id.noReviewsText).visibility = GONE
                findViewById<TextView>(R.id.noReviewsText2).visibility = GONE
                findViewById<RecyclerView>(R.id.courtReviewsList).visibility = VISIBLE
            } else {
                findViewById<TextView>(R.id.noReviewsText).visibility = VISIBLE
                findViewById<TextView>(R.id.noReviewsText2).visibility = VISIBLE
                findViewById<RecyclerView>(R.id.courtReviewsList).visibility = GONE
            }
            listAdapter.setReviewsAndUserReview(it, userReviewId)
        }
        reviewsList.adapter = listAdapter

        //set stars onClickListeners
        val stars = listOf(
            findViewById<ImageView>(R.id.review_star1),
            findViewById(R.id.review_star2),
            findViewById(R.id.review_star3),
            findViewById(R.id.review_star4),
            findViewById(R.id.review_star5),
        )
        stars.forEachIndexed {index, element ->
            element.setOnClickListener {
                vm.postUserReviewRating(index+1)
            }
        }

        //observe the user selected rating

        //default rating
        //vm.postUserReviewRating(3)
        vm.userReviewRating.observe(this) {
            Log.d("rating", it.toString())
            for(i in 0 until 5) {
                if(i<it) {
                    stars[i].setImageResource(R.drawable.baseline_star_24)
                } else {
                    stars[i].setImageResource(R.drawable.baseline_star_empty_24)
                }
            }
        }

        //save the user review in the view model
        findViewById<EditText>(R.id.courtDetails_reviewText).doOnTextChanged {text, pos1, pos2, pos3 ->
            vm.postUserReviewText(text.toString())
        }

        //save review
        findViewById<Button>(R.id.courtDetails_addReviewButton).setOnClickListener {
            vm.saveReview()
        }

        findViewById<ImageButton>(R.id.courtDetails_backButton).setOnClickListener {
            finish()
        }
    }

    private fun setStarsEnabled(enabled: Boolean) {
        val stars = listOf(
            findViewById<ImageView>(R.id.review_star1),
            findViewById(R.id.review_star2),
            findViewById(R.id.review_star3),
            findViewById(R.id.review_star4),
            findViewById(R.id.review_star5),
        )

        if(enabled) {
            findViewById<TextView>(R.id.courtReview_textView).setTextColorRes(R.color.black)
            stars.forEachIndexed {index, element ->
                element.setImageColorRes(R.color.yellow)
                element.setOnClickListener {
                    vm.postUserReviewRating(index+1)
                }
            }
        } else {
            //findViewById<TextView>(R.id.courtReview_textView).setTextColorRes(R.color.light_grey)
            stars.forEach {
                it.setImageColorRes(R.color.light_grey)
                it.setOnClickListener{

                }
            }
        }
    }
}