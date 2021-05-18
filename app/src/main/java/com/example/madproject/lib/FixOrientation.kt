package com.example.madproject.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

class FixOrientation {
    companion object {
        @Throws(IOException::class)
        fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri?): Bitmap? {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var imageStream: InputStream? = context.contentResolver.openInputStream(selectedImage!!)
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            imageStream = context.contentResolver.openInputStream(selectedImage)
            var img = BitmapFactory.decodeStream(imageStream, null, options)
            img = rotateImageIfRequired(context, img, selectedImage)
            return img
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
            // Raw height and width of image
            val reqWidth = 1024
            val reqHeight = 1024
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {

                val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()

                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

                val totalPixels = (width * height).toFloat()

                val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++
                }
            }
            return inSampleSize
        }

        @Throws(IOException::class)
        private fun rotateImageIfRequired(context: Context, img: Bitmap?, selectedImage: Uri): Bitmap? {
            val input = context.contentResolver.openInputStream(selectedImage)
            val ei: ExifInterface = if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!) else ExifInterface(selectedImage.path!!)
            return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
                else -> img
            }
        }


        private fun rotateImage(img: Bitmap?, degree: Int): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedImg = img?.let { Bitmap.createBitmap(it, 0, 0, img.width, img.height, matrix, true) }
            img?.recycle()
            return rotatedImg
        }
    }

}