package hys.hmonkeyys.readimagetext.views.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.views.appsetting.AppSettingActivity
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import hys.hmonkeyys.readimagetext.fragment.bottomdialog.BottomDialogFragment
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.views.BaseActivity
import java.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class MainActivity : BaseActivity<MainViewModel>(

) {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override val viewModel: MainViewModel by viewModel()

    private var backPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.isFabItemVisible = false
        binding.isCropImageViewVisible = false
    }

    override fun observeData() {
        viewModel.mainStateLiveData.observe(this) {
            when(it) {
                is MainState.Initialized -> { // 초기화
                    initToolbar()
                    initViews()
                    initAdmob()
                }
                is MainState.TextExtractionComplete -> { // 텍스트 추출 완료
                    extractionComplete(it.result)
                }
            }
        }
    }

    // 상단 툴바 초기화
    private fun initToolbar() {
        binding.goHomeButton.setOnClickListener {
            binding.webView.loadUrl(viewModel.getSettingUrl())
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

    // 뷰 초기화
    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        binding.refreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }

        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true

            // 앱 실행 시 마지막 방문한 페이지로 이동
            val lastUrl = viewModel.getLastUrl()
            loadUrl(lastUrl)

            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                binding.scrollValue = scrollY
            }
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
                val rootView = window.decorView.rootView
                val bitmap = viewModel.getBitmapFromView(rootView)

                binding.cropImageView.setImageBitmap(bitmap)
                binding.isCropImageViewVisible = true
            }, CAPTURE_DELAY)
        }

        // 스크린 캡처한 텍스트 추출
        binding.fabCheck.setOnClickListener {
            val selectedBitmap = binding.cropImageView.croppedImage
            selectedBitmap ?: return@setOnClickListener

            viewModel.readImageTextBitmap(selectedBitmap)
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

    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG, "광고가 문제 없이 로드됨 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e(TAG, "광고 로드에 문제 발생 onAdFailedToLoad ${error.message}")
                }
            }
        }
    }

    // 추출결과에 따른 분기
    private fun extractionComplete(extractionResult: String) {
        when(extractionResult) {
            Util.TEXT_LIMIT_EXCEEDED -> {
                Toast.makeText(this, getString(R.string.ocr_text_limit), Toast.LENGTH_SHORT).show()
            }
            Util.EXTRACTION_ERROR -> {
                Toast.makeText(this, getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
            }
            else -> {
                val bottomDialogFragment = BottomDialogFragment(extractionResult)
                bottomDialogFragment.show(supportFragmentManager, bottomDialogFragment.tag)
            }
        }
    }

    // intent 에 데이터 있는지 확인 후 있으면 웹뷰 페이지 이동.
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

    override fun onPause() {
        super.onPause()

        // 마지막 접속한 페이지 저장
        viewModel.setLastUrl(binding.addressBar.text.toString())
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

            viewModel.insertAfterDuplicateDataLookup(url)
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

        private const val TRANSLATION_Y = "translationY"

        private const val ONE_POINT_FIVE_SECOND = 1500L
        private const val CAPTURE_DELAY = 400L
    }
}