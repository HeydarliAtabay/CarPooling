package com.example.madproject.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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