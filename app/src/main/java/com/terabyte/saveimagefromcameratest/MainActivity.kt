package com.terabyte.saveimagefromcameratest

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var image: ImageView

    private val pickImageLauncher = registerForActivityResult(PickImageResultContract()) {
        if(it!=null) image.setImageURI(it)
        else Toast.makeText(this, "Something went wrong and imageUri is null.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonOpenCamera = findViewById<Button>(R.id.button_open_camera)
        image = findViewById<ImageView>(R.id.image)

        buttonOpenCamera.setOnClickListener {
            if(checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch(Unit)
            }
            else {
                ActivityCompat.requestPermissions(this, arrayOf(CAMERA), REQUIRE_PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== REQUIRE_PERMISSIONS_REQUEST_CODE) {
            //only 0 index, because now we have only 1 permission to request
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch(Unit)
            }
            else Toast.makeText(this, "Please, accept the camera permission for proper working.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUIRE_PERMISSIONS_REQUEST_CODE = 0
    }
}