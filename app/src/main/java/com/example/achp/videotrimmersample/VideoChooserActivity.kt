package com.example.achp.videotrimmersample

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button

import android.content.pm.PackageManager

import android.os.Build
import android.app.Activity
import android.support.v4.app.ActivityCompat





class VideoChooserActivity : AppCompatActivity() {

    private lateinit var chooseVideoButton: Button
    private val selectVideoConstant = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chooser)

        chooseVideoButton = findViewById(R.id.choose_video_button)
        defineListeners()
    }

    private fun defineListeners(){
        chooseVideoButton.setOnClickListener {
            if(checkPermissionForReadExtertalStorage()){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent , selectVideoConstant)
            }else{
                requestPermissionForReadExtertalStorage()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==selectVideoConstant){
            val dataUriString = getPath(data.data)
            println("Ok $dataUriString")

            val intent = Intent(this, VideoPreviewActivity::class.java)
            intent.putExtra("path", dataUriString)

            startActivity(intent)

        }else if(requestCode==2){
            println("Ok")
        }
    }

    fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {

            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    private fun checkPermissionForReadExtertalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }


    private fun requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    2)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

}
