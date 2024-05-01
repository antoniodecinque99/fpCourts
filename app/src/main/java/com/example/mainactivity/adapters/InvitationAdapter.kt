package com.example.mainactivity.adapters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView;
import com.example.mainactivity.*;
import com.example.mainactivity.activities.BookingDetailsActivity;
import com.example.mainactivity.models.FireBookingCourt;
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.models.TimeSlot;
import com.example.mainactivity.viewmodel.ConfirmBookingViewModel;
import com.example.mainactivity.viewmodel.ShowBookingViewModel
import com.example.mainactivity.viewmodel.ShowProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL


class InvitationAdapter (private val vm: ConfirmBookingViewModel, private val inviteButton: Button) : RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder>() {
    var data: List<FireUser> = listOf()
    var pictures: Map<String, Bitmap> = mapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.invitation_layout, parent, false)
        return InvitationViewHolder(v)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val bookingCourt = data[position]
        holder.bind(bookingCourt, vm)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setUsers(list: List<FireUser>) {
        this.data = list
        if (list.size == 1) {
            // enable invite button
            inviteButton.isClickable = true
            inviteButton.alpha = 1.0F
        } else {
            // enable invite button
            inviteButton.isClickable = false
            inviteButton.alpha = 0.5F
        }
        // this.notifyItemRangeChanged(0, list.size)
        this.notifyDataSetChanged()
    }

    fun setImages(list: Map<String, Bitmap>) {
        this.pictures = list
    }

    class InvitationViewHolder(v: View) :
        RecyclerView.ViewHolder(v) {

        val namePlayer = v.findViewById<TextView>(R.id.playerName)
        val imagePlayer = v.findViewById<CircleImageView>(R.id.playerImage)
        val usernamePlayer = v.findViewById<TextView>(R.id.playerUsername)


        fun bind(user: FireUser, vm: ConfirmBookingViewModel) {
            namePlayer.text = user.name
            usernamePlayer.text = user.nickname
            // Fetch and display the user's pictureUri using coroutines
            Storage.getProfilePic(user.email) {
                imagePlayer.setImageBitmap(it)
            }
            /*GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    user.pictureUri.let {
                        try {
                            val inputStream: InputStream = URL(user.pictureUri).openStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            withContext(Dispatchers.Main) {
                                imagePlayer.setImageBitmap(bitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }*/
        }
    }
}


