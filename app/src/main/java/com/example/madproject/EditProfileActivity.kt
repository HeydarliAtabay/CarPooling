package com.example.madproject

import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

var t: Toast? = null

enum class Requests(val value : Int)
{
    INTENT_CAPTURE_PHOTO(1);
    companion object {
        fun from(findValue: Int): Requests = Requests.values().first { it.value == findValue }
    }
}

fun setToast(message: String?, context: Context) {
    t?.cancel()
    t = Toast.makeText(context, message, Toast.LENGTH_LONG)
    t?.show()
}

class EditProfileActivity : AppCompatActivity() {
    private lateinit var fullName:EditText
    private lateinit var nickName:EditText
    private lateinit var email:EditText
    private lateinit var location:EditText
    private lateinit var imageView:ImageView
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        this.setTitle("Edit your profile...")

        fullName = findViewById(R.id.fullName)
        nickName = findViewById(R.id.nickName)
        email = findViewById(R.id.email)
        location = findViewById(R.id.location)
        imageView = findViewById(R.id.imageView)

        fullName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                fullName.setSelection(0, 0)
            } }

        nickName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                nickName.setSelection(0, 0)
            } }

        email.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                email.setSelection(0, 0)
            } }

        location.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                location.setSelection(0, 0)
            } }

        val editPhoto = findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(editPhoto)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Requests.INTENT_CAPTURE_PHOTO.value -> setPic()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.saveButton -> {
                val text = findViewById<TextView>(R.id.fullName)
                setToast("Saving...", applicationContext)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.imageButton -> {
                menuInflater.inflate(R.menu.menu_change_photo, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_gallery -> {
                setToast("Opening the gallery...", applicationContext)
                true
            }
            R.id.action_camera -> {
                dispatchTakePictureIntent()
                true
            }
            else -> false
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, Requests.INTENT_CAPTURE_PHOTO.value)
                }
            }
        }
    }

    /* Save image into the phone Gallery
    private fun insertImage(cr: ContentResolver,
                            source: Bitmap?,
                            title: String?,
                            description: String?): String? {
        val values = ContentValues()
        values.put(Images.Media.TITLE, title)
        values.put(Images.Media.DISPLAY_NAME, title)
        values.put(Images.Media.DESCRIPTION, description)
        values.put(Images.Media.MIME_TYPE, "image/jpeg")
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
        var url: Uri? = null
        var stringUrl: String? = null /* value to be returned */
        try {
            url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
            if (source != null) {
                val imageOut: OutputStream? = cr.openOutputStream(url!!)
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
                } finally {
                    imageOut?.close()
                }
                val id = ContentUris.parseId(url!!)
                // Wait until MINI_KIND thumbnail is generated.

            } else {
                cr.delete(url!!, null, null)
                url = null
            }
        } catch (e: Exception) {
            if (url != null) {
                cr.delete(url, null, null)
                url = null
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }

    */

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            //BitmapFactory.decodeFile(currentPhotoPath, bmOptions)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true

        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            val bit = FixOrientation.handleSamplingAndRotationBitmap(this, photoURI)
            //insertImage(contentResolver,bit,"Profile MAD","Prova")
            imageView.setImageBitmap(bit)
        }
    }

}