package com.starnoh.cameraexample

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.starnoh.cameraexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val CAMERA_REQUEST_CODE = 1
    private  val GALLERY_REQUEST_CODE = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            cameraCheckPermission()
        }
        binding.btnGallery.setOnClickListener {
            galleryCheckPermission()
        }

        binding.imageView.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItem = arrayOf("Select photo from Gallery ","Capture photo using Camera")
            pictureDialog.setItems(pictureDialogItem){dialog , which ->

                when(which){
                    0 -> gallery()
                    1 -> camera()
                }
            }
            pictureDialog.show()
        }




    }

    private fun galleryCheckPermission(){
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object: PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                gallery()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@MainActivity, "You have denied storage Permissions to select an image",
                    Toast.LENGTH_SHORT).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {

            }

        }).onSameThread().check()
    }

    private fun gallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun cameraCheckPermission() {
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA).withListener(

                object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()){
                                camera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }
                }
            ).onSameThread().check()
    }

    private fun camera(){
        val intent  = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent , CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
                when(requestCode){
                    CAMERA_REQUEST_CODE -> {

                        val bitmap = data?.extras?.get("data") as Bitmap
                        // we are using coroutine image loader  (coil)
                        binding.imageView.load(bitmap){
                            crossfade(true)
                            crossfade(1000)
                            transformations(CircleCropTransformation())

                        }
                    }

                    GALLERY_REQUEST_CODE -> {
                        binding.imageView.load(data?.data){
                            crossfade(true)
                            crossfade(1000)
                            transformations(CircleCropTransformation())
                        }
                    }
                }
        }
    }

    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("You have turned off Permissions for this feature . Turn on Permissions in App Settings")
            .setPositiveButton("Go to settings"){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName , null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }


            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }.show()
    }
}