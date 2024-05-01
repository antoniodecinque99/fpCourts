package com.example.mainactivity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.mainactivity.R

class SliderAdapter (private val context: Context) : PagerAdapter() {
    private val images = listOf(
        R.drawable.onboarding_picture_1,
        R.drawable.onboarding_picture_2,
        R.drawable.onboarding_picture_3
    )

    private val headings = listOf(
        R.string.first_slide_title,
        R.string.second_slide_title,
        R.string.third_slide_title
    )

    private val descriptions = listOf(
        R.string.first_slide_desc,
        R.string.second_slide_desc,
        R.string.third_slide_desc
    )


    override fun getCount(): Int {
        return headings.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == (`object` as ConstraintLayout)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.slides_layout, container, false)

        val imageView: ImageView = view.findViewById(R.id.slider_image)
        val headingTextView: TextView = view.findViewById(R.id.slider_heading)
        val descTextView: TextView = view.findViewById(R.id.slider_text)

        imageView.setImageResource(images[position])
        headingTextView.setText(headings[position])
        descTextView.setText(descriptions[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}