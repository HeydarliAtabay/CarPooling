package com.example.madproject.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/*
    Class which extends the normal behavior of the default divider. It is used to not show the
    divider after the last item in the Recycler View
*/
class DividerItemDecorator(private val mDivider: Drawable) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + mDivider.intrinsicHeight
            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            mDivider.draw(canvas)
        }
    }
}

/*
    Function to determine if the given date time ("date" - "time" + (optional)"duration") is before or
    after current date time
*/
fun isFuture(date: String, time: String, duration: String): Boolean {
    // Depending on the date and time, determine if the trip was terminated or not
    val current = Date()
    val inputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH)

    var tripDateTime = inputFormat.parse("$date $time") ?: return false

    if (duration != "") {
        val inputDurationFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        inputDurationFormat.timeZone = TimeZone.getTimeZone("UTC")
        val tripDuration = inputDurationFormat.parse(duration) ?: return false
        val newTime = tripDateTime.time + tripDuration.time
        tripDateTime = Date(newTime)
    }
    Log.d("test", inputFormat.format(current))
    Log.d("test", inputFormat.format(tripDateTime))
    return tripDateTime.after(current)
}

/*
    Function to create a new image file inside "storagePath"
*/
fun createImageFile(storagePath: String?): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())

    val filename = "$storagePath/$timeStamp.jpg"

    return File(filename)
}

/*
    Function to resize the image in "bigPhotoPath", and save it in "currentPhotoPath"
*/
fun resizeImage(context: Context, bigPhotoPath: String, storagePath: String?): String {
    val currentPhotoPath = "$storagePath/profileImage.jpg"
    val smallImageFile = File(currentPhotoPath)
    val fOut: OutputStream = FileOutputStream(smallImageFile)

    val bigImageFile = File(bigPhotoPath)
    val photoURI = FileProvider.getUriForFile(
        context.applicationContext,
        "com.example.android.fileprovider",
        bigImageFile
    )
    val pic = FixOrientation.handleSamplingAndRotationBitmap(
        context.applicationContext,
        photoURI
    )
    pic?.compress(Bitmap.CompressFormat.JPEG, 30, fOut)
    fOut.flush()
    fOut.close()

    bigImageFile.delete()
    return currentPhotoPath
}

/*
    Function to parse the price string in edit text
*/
fun parsePrice(s: String): String {
    return if (s.contains(".")) {
        val p = s.split(".")
        val integer = if (p[0] == "") "0" else p[0]
        val dec = p[1]
        when (dec.length) {
            0 -> "$integer.00"
            1 -> "$integer.${dec}0"
            else -> "$integer.${dec[0]}${dec[1]}"
        }

    } else {
        if (s != "") {
            "$s.00"
        } else ""
    }
}

/*
    Function to parse the duration string in edit text
 */
fun parseDuration(s: String): String {
    if (s.isEmpty() || s.length!=5) return s

    val p = s.split(":")
    if (p[1].toInt() < 60) return s

    val m = p[1].toInt() - 60
    val h = if (p[0].toInt() == 99) p[0].toInt() else p[0].toInt() +1

    val mStr = if (m > 9) m.toString() else "0$m"
    val hStr = if (h > 9) h.toString() else "0$h"
    return "$hStr:$mStr"
}

/*
    Function to manage the "duration" text listener
 */
fun durationTextListener(duration: EditText, context: Context?, view: View?) {
    duration.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {  // lost focus
            duration.setSelection(0, 0)
            duration.hint = ""
            duration.setText(parseDuration(duration.text.toString()))
        } else {
            view?.findViewById<TextInputLayout>(R.id.tilDuration)?.error = null
            duration.hint = "hh:mm"
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(duration, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    duration.addTextChangedListener(object: TextWatcher {
        var edited = false
        val dividerCharacter = ":"

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (edited) {
                edited = false
                return
            }

            var working = getEditText()

            working = manageDurationDivider(working, start, before)

            edited = true
            duration.setText(working)
            duration.setSelection(duration.text.length)
        }

        private fun manageDurationDivider(working: String, start: Int, before: Int) : String{
            if (working.length == 2) {
                return if (before <= 2 && start < 2)
                    working + dividerCharacter
                else
                    working.dropLast(1)
            }
            return working
        }

        private fun getEditText() : String {
            return if (duration.text.length >= 10)
                duration.text.toString().substring(0,10)
            else
                duration.text.toString()
        }

        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    })
}

/*
    Function to parse the time string in edit text
 */
fun parseTime(hour: Int?, minute: Int?): String {
    if ((hour == null) || (minute == null)) return ""

    val h = if (hour < 10) "0$hour" else hour.toString()
    val m = if (minute < 10) "0$minute" else minute.toString()

    return "$h:$m"
}

/*
    Function to unParse the time string in edit text
 */
fun unParseTime(time: String): Int {
    val first = time[0]
    val second = time[1]
    if (first.toInt() == 0) return second.toInt()

    return time.toInt()
}
