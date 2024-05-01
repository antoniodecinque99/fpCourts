package com.example.mainactivity.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.Storage
import com.example.mainactivity.adapters.MySportsEditAdapter
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.viewmodel.EditProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    var image_uri: Uri? = null
    private val RESULT_LOAD_IMAGE = 123
    val IMAGE_CAPTURE_CODE = 654
    var requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        if(isGranted) {
            openCamera()
        } else {
            Toast.makeText(applicationContext, "Could not open the camera because the permission has been denied", Toast.LENGTH_SHORT).show()
        }
    }
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)

    val vm by viewModels<EditProfileViewModel>()
    lateinit var userSports: HashMap<String, HashMap<String, Int>>
    lateinit var modifiedUserSports: HashMap<String, HashMap<String, Int>>
    val user = LoggedInUser.user
    //var correctName = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        //val civ = findViewById<CircleImageView>(R.id.imageView)
        //loadData(user)
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        val ib = findViewById<ImageButton>(R.id.cameraPortraitView)
        registerForContextMenu(ib)
        val backButton : ImageButton = findViewById(R.id.editProfileBackButton)
        backButton.setOnClickListener {
            // TODO: add exit without save confirmation
            val homePage = Intent(this, ShowProfileActivity::class.java)
            startActivity(homePage)
            finish()
        }
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation3)
        bottomNavigation.setSelectedItemId(R.id.page_profile)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.page_showBooking-> {
                    val intent = Intent(this, ShowBookingActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.page_courtsList -> {
                    val intent = Intent(this, CourtsListActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }

        val imageButton = findViewById<Button>(R.id.save)
        imageButton.setOnClickListener {
            saveData()
        }


        val recyclerView: RecyclerView = findViewById(R.id.mySportsEditRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        // observe sport data
        var userSportNames: Array<String> = arrayOf()
        var userLevels: Array<Int> = arrayOf()

        vm.getUser(user.email)
        vm.getPicture(user.email)

        vm.userStatistics.observe(this) { it1 ->
            // get all played sport names and levels
            userSports = it1
            modifiedUserSports = it1
            it1.forEach {
                userSportNames = userSportNames.plus(it.key)
                userLevels = userLevels.plus(it.value["level"] ?: 0)
            }
            val mySportsAdapter = MySportsEditAdapter(modifiedUserSports)
            recyclerView.adapter = mySportsAdapter
        }

        vm.user.observe(this) {u ->
            loadData(u)
        }

        vm.picture.observe(this) {
            findViewById<ImageView>(R.id.imageView).setImageBitmap(it)
        }

        vm.validName.observe(this){

        }

        vm.saved.observe(this) {
            if(it) {
                val homePage = Intent(this, ShowProfileActivity::class.java)
                startActivity(homePage)
                finish()
            }
        }

        val addSportButton: Button = findViewById(R.id.addSportButton)
        addSportButton.setOnClickListener {
            val popupMenu = PopupMenu(this,addSportButton)
            val menu = popupMenu.menu
            popupMenu.menuInflater.inflate(R.menu.edit_profile_add_sport_menu,popupMenu.menu)
            // hide sports that are already in user's sports
            for (sport in modifiedUserSports.keys) {
                if (sport == "Basketball")
                    menu.findItem(R.id.add_sport_basket).isEnabled = false
                if (sport == "Football")
                    menu.findItem(R.id.add_sport_football).isEnabled = false
                if (sport == "Tennis")
                    menu.findItem(R.id.add_sport_tennis).isEnabled = false
                if (sport == "Volleyball")
                    menu.findItem(R.id.add_sport_volley).isEnabled = false
                if (sport == "Padel")
                    menu.findItem(R.id.add_sport_padel).isEnabled = false
            }
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_sport_basket -> {
                        // add sport to list
                        modifiedUserSports.put("Basketball", hashMapOf(
                            "level" to 0,
                            "played" to 0,
                            "won" to 0,
                            "lost" to 0,
                            "seenUsers" to 0,
                            "organized" to 0,
                            "planned" to 0
                        ))
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    R.id.add_sport_football -> {
                        modifiedUserSports.put("Football", hashMapOf(
                            "level" to 0,
                            "played" to 0,
                            "won" to 0,
                            "lost" to 0,
                            "seenUsers" to 0,
                            "organized" to 0,
                            "planned" to 0
                        ))
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    R.id.add_sport_tennis -> {
                        Log.d("modify tennis", "pdodpdododood")
                        modifiedUserSports.put("Tennis", hashMapOf(
                            "level" to 0,
                            "played" to 0,
                            "won" to 0,
                            "lost" to 0,
                            "seenUsers" to 0,
                            "organized" to 0,
                            "planned" to 0
                        ))
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    R.id.add_sport_volley -> {
                        modifiedUserSports.put("Volleyball", hashMapOf(
                            "level" to 0,
                            "played" to 0,
                            "won" to 0,
                            "lost" to 0,
                            "seenUsers" to 0,
                            "organized" to 0,
                            "planned" to 0
                        ))
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    R.id.add_sport_padel -> {
                        modifiedUserSports.put("Padel", hashMapOf(
                            "level" to 0,
                            "played" to 0,
                            "won" to 0,
                            "lost" to 0,
                            "seenUsers" to 0,
                            "organized" to 0,
                            "planned" to 0
                        ))
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                true
            }
            // show menu icons
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception){
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popupMenu.show()
            }
        }


    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.floatingmenu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        when (item.itemId) {
            R.id.cam -> {
                when {
                    ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
                    else -> {
                        this.requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
                return true
            }

            R.id.gal -> {
                showGallery()
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun checkForNumbers(inputString: String): Boolean {
        var digits = false
       // Log.d("len", inputString.toString())
        for (char in inputString) {
           // Log.d("char", char.toString())
            if (char.isDigit()) {
                //Log.d("is digit", char.isDigit().toString())
                digits = true
            }
        }
        return digits
    }

    private fun saveData() {
        val newUser = vm.user.value
        newUser?.name = findViewById<EditText>(R.id.edit_name).text.toString()
        val name = findViewById<EditText>(R.id.edit_name).text.toString()
        val newNickname = findViewById<EditText>(R.id.edit_nickname).text.toString()
        val originalNickname = newUser?.nickname

        val numbers = checkForNumbers(newUser?.name!!)
        //true if there are digits, false if not
        if (name.length < 3) {
            showToast("Insert a valid name with at least 3 characters!")
        } else if (checkForNumbers(name)) {
            showToast("Name cannot have digits!")
        } else if (newNickname.length < 3) {
            showToast("Insert a valid nickname with at least 3 characters!")
        } else {
            // Check if the nickname has changed and if it already exists in the database
            if (originalNickname != newNickname) {
                checkNicknameAvailability(newNickname) { isNicknameAvailable ->
                    if (isNicknameAvailable) {
                        updateProfileData(newUser, newNickname)
                    } else {
                        showToast("Nickname already exists!")
                    }
                }
            } else {
                updateProfileData(newUser, newNickname)
            }
        }
    }

    private fun updateProfileData(newUser: FireUser?, newNickname: String) {
        newUser?.nickname = newNickname
        newUser?.location = findViewById<EditText>(R.id.edit_location).text.toString()
        newUser?.birthDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
            SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).parse(
                findViewById<Button>(R.id.edit_age).text.toString()
            )
        )
        newUser?.description = findViewById<EditText>(R.id.edit_description).text.toString()
        newUser?.statistics = modifiedUserSports

        vm.updateUserStatistics(newUser!!)

        // Display the "Profile updated correctly" toast message
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_custom_confirm, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = "Profile updated correctly"
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    private fun checkNicknameAvailability(nickname: String, callback: (Boolean) -> Unit) {
        val db = Firebase.firestore
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("nickname", nickname)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result?.documents
                    val isNicknameAvailable = documents == null || documents.isEmpty()
                    callback(isNicknameAvailable)
                } else {
                    callback(false) // Error occurred, consider nickname as unavailable
                }
            }
    }

    private fun showToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_custom_delete, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    private fun loadData(user: FireUser) {
        val edit_name = findViewById<TextView>(R.id.edit_name)
        val edit_age = findViewById<Button>(R.id.edit_age)
        edit_age.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            )
            datePicker.show()
        }
        val edit_loc = findViewById<EditText>(R.id.edit_location)
        val edit_nick = findViewById<EditText>(R.id.edit_nickname)
        val edit_description = findViewById<EditText>(R.id.edit_description)

        edit_name.setText(user.name)
        edit_age.setText(
            SimpleDateFormat(
                "d MMM yyyy",
                Locale.ENGLISH
            ).format(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(user.birthDate))
        )
        edit_loc.setText(user.location)
        edit_nick.setText(user.nickname)
        edit_description.setText(user.description)
    }

    private fun savePicture(imageUri: Uri) {

        fun rotateBitmap(bitmap: Bitmap): Bitmap {
            val orientationColumn =
                arrayOf(MediaStore.Images.Media.ORIENTATION)
            val cur: Cursor? =
                contentResolver.query(image_uri!!, orientationColumn, null, null, null)
            var orientation = -1
            if (cur != null && cur.moveToFirst()) {
                var colIndex = cur.getColumnIndex(orientationColumn[0])
                if (colIndex >= 0) {
                    orientation = cur.getInt(colIndex)
                }
            }
            Log.d("Orientation", orientation.toString())
            Log.d("Orientation column", orientationColumn[0])
            val rotationMatrix = Matrix()
            rotationMatrix.setRotate(orientation.toFloat())
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                rotationMatrix,
                true
            )
        }

        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(imageUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            var imageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            imageBitmap = rotateBitmap(imageBitmap)
            parcelFileDescriptor.close()
            vm.picture.postValue(imageBitmap)
            //findViewById<ImageView>(R.id.imageView).setImageBitmap(imageBitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityIfNeeded(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun showGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityIfNeeded(galleryIntent, RESULT_LOAD_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            image_uri?.let { savePicture(it) }
        }
        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            image_uri = data.data
            image_uri?.let { savePicture(it) }
        }
    }




    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        displayFormattedDate(calendar.timeInMillis)
    }

    fun displayFormattedDate(timestamp: Long) {
        val age_changed = findViewById<Button>(R.id.edit_age)
        val date = formatter.format(timestamp)
        age_changed.setText(date)

        Log.d("date", date)
    }
}