package com.example.mainactivity

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun dpToPx(dp: Int, context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics,
    ).toInt()

internal val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

internal val Context.inputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

internal fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(this, drawable))

internal fun Context.getColorCompat(@ColorRes color: Int) =
    ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

internal fun TextView.setBackgroundColorRes(@ColorRes color: Int) {
    val _color = context.getColorCompat(color)
    val backgroundDrawable = this.background as GradientDrawable

    val newDrawable = GradientDrawable()
    newDrawable.shape = backgroundDrawable.shape
    newDrawable.cornerRadius = backgroundDrawable.cornerRadius

    newDrawable.setColor(_color)
    this.background = newDrawable
}

internal fun View.setBackgroundColorRes(@ColorRes color: Int) {
    val _color = context.getColorCompat(color)

    val backgroundDrawable = this.background as GradientDrawable

    val newDrawable = GradientDrawable()
    newDrawable.shape = backgroundDrawable.shape
    newDrawable.cornerRadius = backgroundDrawable.cornerRadius

    newDrawable.setColor(_color)
    this.background = newDrawable
}

internal fun View.setViewColor(@ColorRes color: Int) {
    val background = this.background
    if (background != null) {
        val wrappedDrawable = DrawableCompat.wrap(background.mutate())
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this.context, color))
        this.background = wrappedDrawable
    }
}

internal fun ImageView.setImageColorRes(@ColorRes color: Int) =
    setColorFilter(context.getColorCompat(color))

fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun setFont(textView: TextView, context: Context, font_path: String) {
    val customFont = Typeface.createFromAsset(context.getAssets(), font_path)
    textView.setTypeface(customFont)
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}


fun getSportNames(): Array<String> {
    return arrayOf("Basketball", "Football", "Tennis", "Volleyball", "Padel")
}

fun getLevelDescription(level: Int): String {
    if (level <= 20) {
        return "NOVICE"
    }
    if (level <= 40) {
        return "AMATEUR"
    }
    if (level <= 60) {
        return "EXPERT"
    }
    if (level <= 70) {
        return "SEMI-PRO"
    }
    if (level <= 95) {
        return "PRO"
    }
    return "LEGEND"
}
fun getSportColor(sportName: String): Int {
    return when (sportName) {
        "Basketball" -> R.color.basket_color
        "Football" -> R.color.football_color
        "Tennis" -> R.color.tennis_color
        "Volleyball" -> R.color.volley_color
        "Padel" -> R.color.padel_color
        else -> R.color.white // add a default color for undefined sports
    }
}

fun getSportIcon(sportName: String): Int {
    return when (sportName) {
        "Basketball" -> R.drawable.basket_icon
        "Football" -> R.drawable.football_icon
        "Tennis" -> R.drawable.tennis_icon
        "Volleyball" -> R.drawable.volley_icon
        "Padel" -> R.drawable.padel_icon
        else -> R.drawable.football_icon
    }
}

fun getCourtImage(courtId: Int): Int {
    return when (courtId) {
        1 -> R.drawable.cit
        2 -> R.drawable.robilant
        3 -> R.drawable.turinsportcenter
        4 -> R.drawable.lepleiadi
        5 -> R.drawable.skylinevolleyclub
        6 -> R.drawable.padelclubturin
        else -> R.drawable.cit
    }
}

fun getTeamMembersForSport(sportName: String): Int {
    return when (sportName) {
        "Basketball" -> 3
        "Football" -> 5
        "Tennis" -> 1
        "Volleyball" -> 6
        "Padel" -> 2
        else -> 6
    }
}

fun today(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.now().format(formatter)
}

fun convertDate(inputFormatter: DateTimeFormatter, outputFormatter: DateTimeFormatter, date: String): String {
    return LocalDate.parse(date, inputFormatter).format(outputFormatter)
}

// convert a date string like "2023-06-01" to "Thursday, 1 Jun 2023" or "Thu, 1 Jun 2023"
fun convertDateToDisplay(dateString: String, useLongFormat: Boolean = true): String {
    val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = if (useLongFormat) {
        DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())
    } else {
        DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    }
    val date = LocalDate.parse(dateString, inputFormat)
    return outputFormat.format(date)
}

fun convertDisplayToDate(dateString: String): String {
    val inputFormats = listOf(
        DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()),
        DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())
    )
    val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormats
        .asSequence()
        .mapNotNull { inputFormat ->
            runCatching { LocalDate.parse(dateString, inputFormat) }.getOrNull()
        }
        .firstOrNull()

    return if (date != null) {
        outputFormat.format(date)
    } else {
        throw IllegalArgumentException("Invalid date format: $dateString")
    }
}

fun convertDateToDisplay_(dateString: String, useLongFormat: Boolean = true): String {
    val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = if (useLongFormat) {
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    } else {
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    }
    val date = LocalDate.parse(dateString, inputFormat)
    return outputFormat.format(date)
}

fun convertDisplayToDate_(dateString: String): String {
    val inputFormats = listOf(
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
    )
    val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormats
        .asSequence()
        .mapNotNull { inputFormat ->
            runCatching { LocalDate.parse(dateString, inputFormat) }.getOrNull()
        }
        .firstOrNull()

    return if (date != null) {
        outputFormat.format(date)
    } else {
        throw IllegalArgumentException("Invalid date format: $dateString")
    }
}
