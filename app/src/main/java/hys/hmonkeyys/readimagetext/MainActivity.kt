package hys.hmonkeyys.readimagetext

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
//import com.theartofdev.edmodo.cropper.CropImage
//import com.theartofdev.edmodo.cropper.CropImageView
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initToolbar()
        initWebView()
        initViews()

        checkPermissions()
    }

    // 바인딩 데이터 초기화
    private fun initData() {
        binding.isFabItemVisible = false
        binding.isCropImageViewVisible = false
    }

    // 상단 툴바 초기화
    private fun initToolbar() {
        binding.goHomeButton.setOnClickListener {
            binding.webView.loadUrl(DEFAULT_URL)
        }

        binding.addressBar.setOnEditorActionListener { v, actionId, event ->
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

        binding.goBackButton.setOnClickListener {
            binding.webView.goBack()
        }

        binding.goForwardButton.setOnClickListener {
            binding.webView.goForward()
        }
    }

    // 웹뷰 초기화
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            loadUrl(DEFAULT_URL)

            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                binding.scrollValue = scrollY
            }
        }
    }

    // 뷰 초기화
    private fun initViews() {
        binding.refreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }

        binding.fabMain.setOnClickListener {
            toggleFab()
        }

        binding.fabScreenCapture.setOnClickListener {
            closeFloatingButtonWithAnimation()

            Handler(mainLooper).postDelayed({
                val rootView = this.window.decorView.rootView
                val bitmap = getBitmapFromView(rootView)

                binding.cropImageView.setImageBitmap(bitmap)
                binding.isCropImageViewVisible = true
            }, 400)
        }

        binding.fabAppInfo.setOnClickListener {
            closeFloatingButtonWithAnimation()

            startActivity(Intent(this, AppInfotmationActivity::class.java))
        }

        binding.fabCheck.setOnClickListener {
            val selectedBitmap = binding.cropImageView.croppedImage
            selectedBitmap ?: return@setOnClickListener

            readImageTextBitmap(selectedBitmap)

            binding.isCropImageViewVisible = false
        }

    }

    // 플로팅 액션 버튼 토클
    private fun toggleFab() {
        if (binding.isFabItemVisible == true) {
            closeFloatingButtonWithAnimation()
        } else {
            openFloatingButtonWithAnimation()
        }
    }

    private fun closeFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.fabScreenCapture, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCaptureTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfo, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfoTextView, TRANSLATION_Y, 0f).apply { start() }
        binding.fabMain.setImageResource(R.drawable.ic_add_24)
        binding.isFabItemVisible = false
    }

    private fun openFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.fabScreenCapture, TRANSLATION_Y, -200f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCaptureTextView, TRANSLATION_Y, -200f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfo, TRANSLATION_Y, -400f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfoTextView, TRANSLATION_Y, -400f).apply { start() }
        binding.fabMain.setImageResource(R.drawable.ic_clear_24)
        binding.isFabItemVisible = true
    }

    // 뷰를 bitmap 으로 변환
    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
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
                    Log.d(TAG, it.text)

                    val bottomDialogFragment = BottomDialogFragment(it.text)
                    bottomDialogFragment.show(supportFragmentManager, bottomDialogFragment.tag)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    Toast.makeText(this, resources.getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
                }

        } catch (e: IOException) {
            Toast.makeText(this, resources.getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun checkPermissions() {
        try {
            val rejectedPermissionList = ArrayList<String>()

            // 필요한 퍼미션들을 하나씩 권한을 받았는지 확인
            for(permission in REQUIRED_PERMISSIONS){
                if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없으면 추가
                    rejectedPermissionList.add(permission)
                }
            }

            // 거절한 퍼미션이 있으면 권한 요청
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
                        permissions.forEachIndexed { index, _ ->
                            if(grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                                //권한 획득 실패
                                Toast.makeText(this, resources.getString(R.string.decline_permissions), Toast.LENGTH_LONG).show()
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

    override fun onBackPressed() {
        if(binding.cropImageView.isVisible) {
            binding.isCropImageViewVisible = false
        } else {
            if(binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    inner class WebViewClient: android.webkit.WebViewClient() {
        // 페이지 로드 시작
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            binding.progressBar.show()
        }

        // 페이지 로드 완료
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
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val DEFAULT_URL = "https://www.google.com"
        private const val TRANSLATION_Y = "translationY"
    }

}