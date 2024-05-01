package com.example.mainactivity.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.R
import com.example.mainactivity.getLevelDescription
import com.example.mainactivity.getSportColor
import com.example.mainactivity.getSportIcon
import com.example.mainactivity.setImageColorRes

class MySportsAdapter(private val dataSet: List<String>, private val levels: List<Int>) :
    RecyclerView.Adapter<MySportsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sportName: TextView
        val sportIcon: ImageView
        val levelNumberText: TextView
        val levelDescription: TextView

        init {
            sportName = view.findViewById(R.id.mySportsSportName)
            sportIcon = view.findViewById(R.id.mySportsSportIcon)
            levelNumberText = view.findViewById(R.id.mySportsLevelNumber)
            levelDescription = view.findViewById(R.id.mySportsLevelDescription)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.my_sports_sport_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val level = levels[position]
        viewHolder.sportName.text = dataSet[position]
        viewHolder.sportIcon.setImageResource(getSportIcon(dataSet[position]))
        viewHolder.sportIcon.setImageColorRes(getSportColor(dataSet[position]))
        viewHolder.levelNumberText.text = level.toString()
        viewHolder.levelDescription.text = getLevelDescription(level)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataSet.size
    }

}
