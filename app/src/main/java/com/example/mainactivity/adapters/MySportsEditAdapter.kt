package com.example.mainactivity.adapters

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.getLevelDescription
import com.example.mainactivity.getSportColor
import com.example.mainactivity.getSportIcon
import com.example.mainactivity.models.PersonSports
import com.example.mainactivity.setImageColorRes
import com.example.mainactivity.viewmodel.EditProfileViewModel


class MySportsEditAdapter(private var dataSet: HashMap<String, HashMap<String, Int>>) :
    RecyclerView.Adapter<MySportsEditAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sportName: TextView
        val sportIcon: ImageView
        val levelNumberText: TextView
        val levelDescription: TextView
        val seekBar: SeekBar
        val deleteButton: ImageButton

        init {
            sportName = view.findViewById(R.id.mySportsSportName)
            sportIcon = view.findViewById(R.id.mySportsSportIcon)
            levelNumberText = view.findViewById(R.id.mySportsLevelNumber)
            levelDescription = view.findViewById(R.id.mySportsLevelDescription)
            seekBar = view.findViewById(R.id.editSportSeekBar)
            deleteButton = view.findViewById(R.id.deleteSportButton)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.my_sports_edit_sport_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d("map", dataSet.toString())
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val name = dataSet.keys.toList().sorted()[position]
        val level = dataSet[name]?.get("level") ?: 0
        //val level = 1
        viewHolder.sportName.text = name
        viewHolder.sportIcon.setImageResource(getSportIcon(name))
        viewHolder.sportIcon.setImageColorRes(getSportColor(name))
        viewHolder.levelNumberText.text = level.toString()
        viewHolder.levelDescription.text = getLevelDescription(level)
        viewHolder.seekBar.progress = level

        // update level based on seek bar
        viewHolder.seekBar.setOnSeekBarChangeListener(
            (object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    viewHolder.levelNumberText.text = progress.toString()
                    // save dataset
                    viewHolder.levelDescription.text = getLevelDescription(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    dataSet[name]?.set("level", seekBar.progress)
                }
            })
        )

        // delete sport
        val builder = AlertDialog.Builder(viewHolder.deleteButton.context)
        viewHolder.deleteButton.setOnClickListener {
            builder.setTitle("Delete sport")
            builder.setMessage("Are you sure you want to delete " + name + " from your sports?")

            builder.setPositiveButton("YES, DELETE") { dialog, which ->
                // delete the sport (not here, on clicking save)
                // refresh activity
                dataSet.remove(name)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, dataSet.size)
            }

            builder.setNegativeButton("NO, KEEP") { dialog, which ->

            }
            builder.show()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
