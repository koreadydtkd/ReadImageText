package hys.hmonkeyys.readimagetext

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding

    private val requiredPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val tts: TextToSpeech by lazy { TextToSpeech(this, this) }

    private var readText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()
        initButtons()

        checkPermissions()
    }

    private fun initWebView() {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl("http://www.google.com")
    }

    private fun initButtons() {
        binding.screenshotButton.setOnClickListener {
            val rootView = this.window.decorView.rootView
            val bitmap = getBitmapFromView(rootView)

            bitmap ?: return@setOnClickListener
            readImageTextBitmap(bitmap)
        }

        binding.speakOutButton.setOnClickListener {
            if(readText.isNotEmpty()) {
                Log.e(TAG, readText)
                speakOut(readText)
            } else {
                Log.e(TAG, "is Empty!!")
            }
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
//        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(1000, 1500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 광학문자인식(OCR, Optical Character Recognition)
     * 이미지에서 텍스트 추출
     */
    private fun readImageTextBitmap(bitmap: Bitmap) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient()

            recognizer.process(image)
                .addOnSuccessListener {
                    readText = it.text
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    readText = "Image Read Fail"
                }

        } catch (e: IOException) {
            e.printStackTrace()
            readText = "Error"
        }
    }

    // TTS 실행
    private fun speakOut(speechText: String) {
        try {
            tts.setPitch(1.0F)
            tts.setSpeechRate(1.0F)
            tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "id1")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "예기치 못한 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

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





    /*private fun getImageUrl(image: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)


        val path = MediaStore.Images.Media.insertImage(
            applicationContext.contentResolver,
            image,
            "Title" + "-" + Calendar.getInstance().time,
            null
        )
        return Uri.parse(path)
    }*/

    /*private fun readImageText(uri: Uri): String {
        var readTextContents = ""
        try {
            val image = InputImage.fromFilePath(applicationContext, uri)
            val recognizer = TextRecognition.getClient()

            recognizer.process(image)
                .addOnSuccessListener {
                    readTextContents = it.text
//                    readText = resultText
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    readTextContents = "Image Read Fail"
                }

//            return readTextContents
        } catch (e: IOException) {
            e.printStackTrace()
            readTextContents = "Error"
        }

        return readTextContents
    }*/



    // TTS 초기화
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

    // 퍼미션 권한 허용 요청에 대한 결과
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            when (requestCode) {
                PERMISSIONS_REQUEST_CODE -> {
                    if(grantResults.isNotEmpty()) {
                        permissions.forEachIndexed { index, _ ->
                            if(grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                                //권한 획득 실패
                                Toast.makeText(this, "권한 허용 후 이용할 수 있습니다.", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
//                        for((i, permission) in permissions.withIndex()) {
//                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                //권한 획득 실패
//                                Toast.makeText(this, "권한 허용 후 이용할 수 있습니다.", Toast.LENGTH_LONG).show()
//                                finish()
//                            }
//                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        tts?.let {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

}