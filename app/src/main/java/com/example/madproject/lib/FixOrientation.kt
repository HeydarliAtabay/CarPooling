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



class FixOrientation {
    companion object {
        @Throws(IOException::class)
        fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri?): Bitmap? {
            val MAX_HEIGHT = 1024
            val MAX_WIDTH = 1024

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var imageStream: InputStream? = context.contentResolver.openInputStream(selectedImage!!)
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream?.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            imageStream = context.contentResolver.openInputStream(selectedImage)
            var img = BitmapFactory.decodeStream(imageStream, null, options)
            img = rotateImageIfRequired(context, img, selectedImage)
            return img
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options,
                                          reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {

                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

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
            val ei: ExifInterface
            ei = if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!) else ExifInterface(selectedImage.path!!)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            return when (orientation) {
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