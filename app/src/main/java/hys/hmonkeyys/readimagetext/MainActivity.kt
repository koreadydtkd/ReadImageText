package hys.hmonkeyys.readimagetext

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
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

        initToolbar()
        initWebView()
        initViews()

        checkPermissions()
    }

    private fun initToolbar() {
        binding.goHomeButton.setOnClickListener {
            binding.webView.loadUrl(DEFAULT_URL)
        }

        binding.addressBar.apply {
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val loadingUrl = v.text.toString()

                    if(URLUtil.isNetworkUrl(loadingUrl)) {
                        binding.webView.loadUrl(loadingUrl)
                    } else {
                        binding.webView.loadUrl("http://$loadingUrl")
                    }

                }
                return@setOnEditorActionListener false
            }
        }

        binding.goBackButton.setOnClickListener {
            binding.webView.goBack()
        }

        binding.goForwardButton.setOnClickListener {
            binding.webView.goForward()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            loadUrl(DEFAULT_URL)

            setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                binding.scrollValue = scrollY
            }

        }
    }


    /*private fun getToolBarAnimation(toolbarHeight: Float, state: String): TranslateAnimation {
        val anim: TranslateAnimation = if(state == "SHOW") {
            TranslateAnimation(0f, 0f, 0f, -toolbarHeight)
        } else {
            TranslateAnimation(0f, 0f, -toolbarHeight, 0f)
        }
        anim.duration = ANIMATION_SPEED
        anim.fillAfter = true
        return anim
    }*/

    private fun initViews() {
        binding.toolbar.visibility = View.GONE
        binding.screenshotButton.setOnClickListener {



            val rootView = this.window.decorView.rootView
            val bitmap = getBitmapFromView(rootView)

//            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)

//            bitmap ?: return@setOnClickListener
//            readImageTextBitmap(bitmap)

            binding.cropImageView.setImageBitmap(bitmap)


        }

        binding.speakOutButton.setOnClickListener {
            val test = binding.cropImageView.croppedImage
            readImageTextBitmap(test)
            binding.cropImageView.visibility = View.GONE

//            if(readText.isNotEmpty()) {
//                Log.e(TAG, readText)
//                speakOut(readText)
//            } else {
//                Log.e(TAG, "is Empty!!")
//            }
        }

        binding.refreshLayout.setOnRefreshListener {
            binding.webView.reload()
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
                    Log.e(TAG, readText)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    readText = "Image Read Fail"
                    Log.e(TAG, readText)
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

    override fun onBackPressed() {
        if(binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class WebViewClient: android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            binding.progressBar.show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            with(binding) {
                refreshLayout.isRefreshing = false
                progressBar.hide()

                goBackButton.isEnabled = webView.canGoBack()
                goForwardButton.isEnabled =  webView.canGoForward()
                addressBar.setText(url)
            }

        }
    }

    inner class WebChromeClient: android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            binding.progressBar.progress = newProgress
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val DEFAULT_URL = "https://www.google.com"
    }

}


/**
 *
Copyright [yyyy] [name of copyright owner]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/