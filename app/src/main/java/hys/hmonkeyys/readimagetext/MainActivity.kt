package hys.hmonkeyys.readimagetext

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import hys.hmonkeyys.readimagetext.dialog.BottomDialogFragment
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.utils.getCurrentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val spf: SharedPreferences by lazy {
        getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private var db: WebDatabase? = null

    private var backPressTime = 0L

    /*private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == REQUEST_CODE) {
            activityResult.data?.let { data ->f
                val selectUrl = data.getStringExtra(Util.MAIN_TO_HISTORY_DEFAULT).toString()
                binding.webView.loadUrl(selectUrl)
            } ?: run {
                Log.d(TAG, "data is null!!")
            }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = WebDatabase.getInstance(applicationContext)

        binding.isFabItemVisible = false
        binding.isCropImageViewVisible = false

        initToolbar()
        initWebView()
        initViews()
        initAdmob()
    }

    // 상단 툴바 초기화
    private fun initToolbar() {
        binding.goHomeButton.setOnClickListener {
            binding.webView.loadUrl(getLoadUrl())
        }

        binding.addressBar.setOnEditorActionListener { v, actionId, _ ->
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

            // 앱 실행 시 마지막 방문한 페이지로 이동
            val lastUrl = spf.getString(SharedPreferencesConst.LAST_URL, "")
            if(lastUrl.isNullOrEmpty()) {
                loadUrl(getLoadUrl())
            } else {
                loadUrl(lastUrl)
            }

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

        // 웹뷰 스크롤 맨위로
        binding.fabMoveTop.setOnClickListener {
            closeFloatingButtonWithAnimation()
            binding.webView.pageUp(true)
        }

        // 앱 설정 플로팅 버튼
        binding.fabAppSetting.setOnClickListener {
            closeFloatingButtonWithAnimation()
            startActivity(Intent(this, AppSettingActivity::class.java))
        }

        // 스크린 캡처 플로팅 버튼
        binding.fabScreenCapture.setOnClickListener {
            closeFloatingButtonWithAnimation()

            Handler(mainLooper).postDelayed({
                val rootView = this.window.decorView.rootView
                val bitmap = getBitmapFromView(rootView)

                binding.cropImageView.setImageBitmap(bitmap)
                binding.isCropImageViewVisible = true
            }, 300)
        }

        // 스크린 캡처한 텍스트 추출
        binding.fabCheck.setOnClickListener {
            val selectedBitmap = binding.cropImageView.croppedImage
            selectedBitmap ?: return@setOnClickListener

            readImageTextBitmap(selectedBitmap)

            binding.isCropImageViewVisible = false
        }

    }

    private fun getLoadUrl(): String {
        val url = spf.getString(SharedPreferencesConst.SETTING_URL, DEFAULT_URL)
        return url ?: DEFAULT_URL
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
        ObjectAnimator.ofFloat(binding.fabMoveTopTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabMoveTop, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppSettingTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppSetting, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCaptureTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCapture, TRANSLATION_Y, 0f).apply { start() }
        binding.fabMain.setImageResource(R.drawable.ic_add_24)
        binding.isFabItemVisible = false
    }

    private fun openFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.fabMoveTopTextView, TRANSLATION_Y, -520f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabMoveTop, TRANSLATION_Y, -520f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppSettingTextView, TRANSLATION_Y, -360f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppSetting, TRANSLATION_Y, -360f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCaptureTextView, TRANSLATION_Y, -200f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCapture, TRANSLATION_Y, -200f).apply { start() }
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
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener {
                    if(it.text.length > OCR_TEXT_LIMIT) {
                        Toast.makeText(this, resources.getString(R.string.ocr_text_limit), Toast.LENGTH_SHORT).show()
                    } else {
                        // 하단 프레그먼트(다이얼로그) 띄우기
                        val bottomDialogFragment = BottomDialogFragment(it.text)
                        bottomDialogFragment.show(supportFragmentManager, bottomDialogFragment.tag)
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Toast.makeText(this, resources.getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
                }

        } catch (e: IOException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            Toast.makeText(this, resources.getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "광고가 문제 없이 로드됨 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e(TAG, "광고 로드에 문제 발생 onAdFailedToLoad ${error.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val selectUrl = intent.getStringExtra(Util.MAIN_TO_HISTORY_DEFAULT)
            if(selectUrl.isNullOrEmpty()) {
                return
            }
            binding.webView.loadUrl(selectUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onStop() {
        super.onStop()

        // 마지막 접속한 페이지 저장
        spf.edit().putString(SharedPreferencesConst.LAST_URL, binding.addressBar.text.toString()).apply()
    }

    override fun onBackPressed() {
        if(binding.cropImageView.isVisible) {
            binding.isCropImageViewVisible = false
        } else {
            if(binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                val time = System.currentTimeMillis()
                if (time - backPressTime > ONE_POINT_FIVE_SECOND) {
                    Toast.makeText(this, resources.getString(R.string.backward_finish), Toast.LENGTH_SHORT).show()
                    backPressTime = time
                } else {
                    super.onBackPressed()
                }
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

            CoroutineScope(Dispatchers.IO).launch {
                // 중복 확인을 위한 쿼리 : 0 반환 시 데이터 없음
                val haveData = db?.historyDao()?.findByHistory(url ?: "", getCurrentDate()) ?: 0
                if(haveData < 1) {
                    db?.historyDao()?.insertHistory(WebHistoryModel(null, url, getCurrentDate()))
                }
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
        private const val TAG = "HYS_MainActivity"

        private const val DEFAULT_URL = "https://www.google.com"
        private const val TRANSLATION_Y = "translationY"

        private const val OCR_TEXT_LIMIT = 350

        private const val ONE_POINT_FIVE_SECOND = 1500L
    }
}