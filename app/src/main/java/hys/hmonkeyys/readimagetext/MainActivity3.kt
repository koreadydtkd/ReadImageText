package hys.hmonkeyys.readimagetext

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import hys.hmonkeyys.readimagetext.databinding.ActivityMain3Binding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess


class MainActivity3 : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMain3Binding

    private val tts: TextToSpeech by lazy {
        TextToSpeech(this, this)
    }

    private val requiredPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var readText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        binding.screenshotButton.setOnClickListener {
            val rootView = this.window.decorView.rootView
            val bitmap = getBitmapFromView(rootView)

            bitmap?.let {
                val bitmapUri = getImageUrl(bitmap)
                readText = readImageText(bitmapUri)
            }
        }

        binding.speakOutButton.setOnClickListener {
            if(readText.isNotEmpty()) {
                Log.e(TAG ,readText.toString())
                speakOut(readText)
            } else {
                Log.e(TAG, "is Empty")
            }
        }
    }

    override fun onDestroy() {
        tts?.let {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
//        val bitmap = Bitmap.createBitmap(view.width, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
//        activity.window?.let { window ->
//            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
//            val locationOfViewInWindow = IntArray(2)
//            view.getLocationInWindow(locationOfViewInWindow)
//
//            try {
//                PixelCopy.request(window, Rect(
//                    locationOfViewInWindow[0],
//                    locationOfViewInWindow[1],
//                    locationOfViewInWindow[0] + view.width,
//                    locationOfViewInWindow[1] + view.height,)
//                , bitmap, { copyResult ->
//                        if(copyResult == PixelCopy.SUCCESS) {
//                            callback(bitmap)
//                        }
//                }, Handler(mainLooper))
//            } catch (e: IllegalArgumentException) {
//                e.printStackTrace()
//            }
//        }
//    }

    private fun checkPermissions() {
        try {
            val rejectedPermissionList = ArrayList<String>()

            //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
            for(permission in requiredPermissions){
                if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    //만약 권한이 없다면 추가
                    rejectedPermissionList.add(permission)
                }
            }
            //거절된 퍼미션이 있다면 권한 요청
            if(rejectedPermissionList.isNotEmpty()){
                val array = arrayOfNulls<String>(rejectedPermissionList.size)
                ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), PERMISSIONS_REQUEST_CODE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // 퍼미션 권한 허용 요청에 대한 결과
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            when (requestCode) {
                PERMISSIONS_REQUEST_CODE -> {
                    if(grantResults.isNotEmpty()) {
                        for((i, permission) in permissions.withIndex()) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                //권한 획득 실패
                                Log.i(TAG, "The user has denied to $permission")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onInit(status: Int) {
        try {
            if(status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.ENGLISH)
                if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "This Language is not supported")
                } else {
                    if(readText.isNotEmpty()) {
                        speakOut(readText)
                    }

                }
            } else {
                Log.e(TAG, "Initilization Failed!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun speakOut(speechText: String) {
        try {
            tts.setPitch(1.0F)
            tts.setSpeechRate(1.0F)
            tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "id1")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getImageUrl(image: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        if(image != null) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        }
        val path = MediaStore.Images.Media.insertImage(
            applicationContext.contentResolver,
            image,
            "Title" + "-" + Calendar.getInstance().time,
            null
        )
        return Uri.parse(path)
    }

    private fun readImageText(uri: Uri): String {
        var readTextContents = ""
        try {
            val image = InputImage.fromFilePath(applicationContext, uri)
            val recognizer = TextRecognition.getClient()

            recognizer.process(image).addOnSuccessListener {
                val resultText = it.text

//                for (block in it.textBlocks) {
//                    for (line in block.lines) {
//                        val lineText = line.text
//                        val lineCornerPoints = line.cornerPoints
//                        val lineFrame = line.boundingBox
//
//                        for (element in line.elements) {
//                            val elementText = element.text
//                            val elementCornerPoints = element.cornerPoints
//                            val elementFrame = element.boundingBox
//                        }
//
//                    }
//
//                }
                Log.e(TAG, resultText)
                readText = resultText
            }.addOnFailureListener {
                it.printStackTrace()
                readText = "Image Read Fail"
            }
            return readTextContents
        } catch (e: IOException) {
            e.printStackTrace()
            readText = "Error"
        }
        return readTextContents

    }

    companion object {
        private const val TAG = "MainActivity3"
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

}