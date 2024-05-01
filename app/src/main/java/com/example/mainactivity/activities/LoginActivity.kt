package com.example.mainactivity.activities

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.Storage
import com.example.mainactivity.adapters.SliderAdapter
import com.example.mainactivity.models.FireUser
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var dots: Array<TextView>
    private lateinit var letsGetStarted: Button
    private lateinit var animation: Animation
    private var currentPos: Int = 0

    private lateinit var mLoading: View

    private fun startApp() {
        startActivity(Intent(this, ShowBookingActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // for the loading screen
        setUIref()
        showLoading()

        auth = Firebase.auth
        db = Firebase.firestore

        //user is already logged in
        if(auth.currentUser != null) {
            findViewById<Button>(R.id.signInButton).visibility = View.GONE
            db.collection("users").whereEqualTo("email", auth.currentUser!!.email).get()
                .addOnSuccessListener { doc ->
                    Log.d("login", "found ${doc.first().id}")
                    val dbUser = doc.first().toObject(FireUser::class.java)
                    LoggedInUser.user = dbUser
                    startApp()
                }
                .addOnCompleteListener {
                    hideLoading()
                }
        } else {
            hideLoading()
        }

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("339101457429-su824fved1lubr2bqbofpfalst3uqh8h.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {result ->
            if(result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if(idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) {task ->
                                if(task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {

                                        if(task.getResult().additionalUserInfo?.isNewUser == true) {
                                            val name = user.displayName ?: ""
                                            val email = user.email ?: ""
                                            val pictureUri = user.photoUrl
                                            val birthDate = "2000-01-01"
                                            val nickname = "@"+name.replace(" ", "")

                                            val fireUser = FireUser(name, nickname, email, birthDate, pictureUri.toString(), "Location", "User description", null)

                                            //save profile picture to firebase storage
                                            Thread {
                                                val url = URL(pictureUri.toString())
                                                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                                                Storage.saveProfilePic(email, bitmap)
                                            }.start()

                                            db.collection("users").add(fireUser).addOnSuccessListener {
                                                Log.d("new user", "Added ${it.id}")
                                                LoggedInUser.user = fireUser
                                                startApp()
                                            }
                                        } else {
                                            db.collection("users").whereEqualTo("email", user.email).get()
                                                .addOnSuccessListener { doc ->
                                                    Log.d("login", "found ${doc.first().id}")
                                                    val dbUser = doc.first().toObject(FireUser::class.java)
                                                    LoggedInUser.user = dbUser

                                                    //check if the user has a saved profile picture
                                                    Storage.getProfilePic(user.email ?: "") {pic ->
                                                        if(pic == null) {
                                                            Thread {
                                                                val url = URL(user.photoUrl.toString())
                                                                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                                                                Storage.saveProfilePic(user.email ?: "", bitmap) {
                                                                    startApp()
                                                                }
                                                            }.start()
                                                        } else startApp()
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                    }
                    Log.d("login", "End login")
                } catch(e: ApiException) {
                    e.printStackTrace()
                }
            }

            Log.d("login", "Result code: ${result.resultCode}")

        }

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    activityResultLauncher.launch(intentSenderRequest)
                }
                .addOnFailureListener(this) { e ->
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
        }

        // Hooks
        viewPager = findViewById(R.id.slider)
        dotsLayout = findViewById(R.id.dots)
        if(auth.currentUser != null) {
            viewPager.visibility = View.GONE
            dotsLayout.visibility = View.GONE
        }
        letsGetStarted = findViewById(R.id.signInButton)

        // Call adapter
        sliderAdapter = SliderAdapter(this)
        viewPager.adapter = sliderAdapter

        // Dots
        addDots(0)
        viewPager.addOnPageChangeListener(changeListener)
    }

    fun next(view: View) {
        viewPager.currentItem = currentPos + 1
    }

    private fun addDots(position: Int) {
        dots = Array(3) { TextView(this) }
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i].text = Html.fromHtml("&#8226;")
            dots[i].textSize = 35f

            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[position].setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private val changeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            // Not implemented
        }

        override fun onPageSelected(position: Int) {
            addDots(position)
            currentPos = position

            when (position) {
                0, 1 -> letsGetStarted.visibility = View.INVISIBLE
                else -> {
                    animation = AnimationUtils.loadAnimation(this@LoginActivity, R.anim.bottom_anim)
                    letsGetStarted.animation = animation
                    letsGetStarted.visibility = View.VISIBLE
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            // Not implemented
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