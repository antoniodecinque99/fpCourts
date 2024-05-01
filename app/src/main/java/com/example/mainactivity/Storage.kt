package com.example.mainactivity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object Storage {
    private val storage = Firebase.storage.reference
    private val fileCache = hashMapOf<String, ByteArray>()
    private val proPicStorage = storage.child("profilePictures")
    private val courtPicStorage = storage.child("courtImages")

    fun saveProfilePic(email: String, pic: Bitmap, callback: (UploadTask.TaskSnapshot?) -> Unit = {}) {
        val pictureStream = ByteArrayOutputStream()
        pic.compress(Bitmap.CompressFormat.JPEG, 100, pictureStream)

        val pictureReference = proPicStorage.child("${email}.jpg")
        pictureReference.putBytes(pictureStream.toByteArray())
            .addOnSuccessListener {
                Log.d("storage", "Profile pic of $email uploaded")
                fileCache["profilePictures/${email}.jpg"] = pictureStream.toByteArray()
                callback(it)
            }
            .addOnFailureListener {
                Log.d("storage", "Profile pic of $email upload failure")
                callback(null)
            }
    }

    fun getProfilePic(email: String, callback: (Bitmap?) -> Unit = {}) {
        var pictureByteArray = fileCache["profilePictures/${email}.jpg"]
        val ONE_MEGABYTE: Long = 1024 * 1024

        if(pictureByteArray == null) {
            Log.d("file_cache", "Cache miss for user $email")
            proPicStorage.child("${email}.jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(it))
                    this.fileCache["profilePictures/${email}.jpg"] = it
                    callback(bitmap)
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else {
            Log.d("file_cache", "Cache hit for user $email")
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(pictureByteArray))
            callback(bitmap)
        }
    }

    fun getCourtPic(id: Int, callback: (Bitmap?) -> Unit = {}) {
        var pictureByteArray = fileCache["courtImages/${id}.jpg"]
        val TWO_MEGABYTE: Long = (1024 * 1024)*2

        if(pictureByteArray == null) {
            Log.d("file_cache", "Cache miss for court $id")
            courtPicStorage.child("$id.jpg").getBytes(TWO_MEGABYTE)
                .addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(it))
                    this.fileCache["courtImages/$id.jpg"] = it
                    callback(bitmap)
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else {
            Log.d("file_cache", "Cache hit for court $id")
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(pictureByteArray))
            callback(bitmap)
        }
    }
}