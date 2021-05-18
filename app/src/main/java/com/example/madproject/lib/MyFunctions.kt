package com.example.madproject.lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.FileProvider
import com.example.madproject.R
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MyFunctions {
    companion object {

        @SuppressLint("SimpleDateFormat")
        fun createImageFile(storagePath: String?): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            val filename = "$storagePath/$timeStamp.jpg"

            return File(filename)
        }

        fun resizeSetImage(context: Context, bigPhotoPath: String, storagePath: String?): String {
            val currentPhotoPath = "$storagePath/profileImage.jpg"
            val smallImageFile = File(currentPhotoPath)
            val fout: OutputStream = FileOutputStream(smallImageFile)

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
            pic?.compress(Bitmap.CompressFormat.JPEG, 30, fout)
            fout.flush()
            fout.close()

            bigImageFile.delete()
            return currentPhotoPath
        }

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

        fun parseTime(hour: Int?, minute: Int?): String {
            if ((hour == null) || (minute == null)) return ""

            val h = if (hour < 10) "0$hour" else hour.toString()
            val m = if (minute < 10) "0$minute" else minute.toString()

            return "$h:$m"
        }

        fun unParseTime(time: String): Int {
            val first = time[0]
            val second = time[1]
            if (first.toInt() == 0) return second.toInt()

            return time.toInt()
        }
    }
}