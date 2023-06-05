package com.terabyte.saveimagefromcameratest

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PickImageResultContract:
ActivityResultContract<Unit, Uri?>() {
    private lateinit var context: Context

    override fun createIntent(context: Context, input: Unit): Intent {
        this.context = context

        val intentGallery = Intent(Intent.ACTION_GET_CONTENT)
        intentGallery.type = "image/*"

        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val intentChooser = Intent.createChooser(intentCamera, "Choose an image from:")
        intentChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentGallery))

        return intentChooser
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if(intent!=null) {
            if(intent.extras!=null && intent.extras!!.containsKey("data")) {
                val inputBitmap = intent.extras!!.get("data") as Bitmap
                val fileInputBitmap = saveInputBitmap(inputBitmap)
                val rotatedBitmap = getRotatedBitmap(inputBitmap, fileInputBitmap)
                return saveRotatedBitmapAndGetUri(context, rotatedBitmap)
            }
            else intent.data
        }
        else null
    }

    private fun saveInputBitmap(inputBitmap: Bitmap): File {
        val quality = 100

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "image_input_${timeStamp}.jpg"

        val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

        inputBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)

        fileOutputStream.close()

        //it is important to split filesDir and fileName using '/' - otherwise there will be one not-existing folder
        return File(context.filesDir.absolutePath+"/"+fileName)
    }

    private fun saveRotatedBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val quality = 100

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "image_rotated_${timeStamp}.jpg"

        val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)

        fileOutputStream.close()

        //it is important to split filesDir and fileName using '/' - otherwise there will be one not-existing folder
        return Uri.fromFile(File(context.filesDir.absolutePath+"/"+fileName))
    }

    private fun getRotatedBitmap(inputBitmap: Bitmap, fileInputBitmap: File): Bitmap {
        fun rotateImage(inputImage: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(inputImage, 0, 0, inputImage.width, inputImage.height, matrix, true)
        }

        val exifInterface = ExifInterface(fileInputBitmap.path)

        return when(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(inputBitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(inputBitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(inputBitmap, 270f)
            }
            else -> inputBitmap
        }
    }

}