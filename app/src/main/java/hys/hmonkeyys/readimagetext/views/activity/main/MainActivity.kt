package hys.hmonkeyys.readimagetext.views.activity.main

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.utils.Utility.EXTRACTION_ERROR
import hys.hmonkeyys.readimagetext.utils.Utility.MAIN_TO_HISTORY_DEFAULT
import hys.hmonkeyys.readimagetext.utils.Utility.TEXT_LIMIT_EXCEEDED
import hys.hmonkeyys.readimagetext.utils.Utility.hideKeyboardAndCursor
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.activity.appsetting.AppSettingActivity
import hys.hmonkeyys.readimagetext.views.activity.note.NoteActivity
import hys.hmonkeyys.readimagetext.views.fragment.bottomsheetdialog.BottomDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

internal class MainActivity : BaseActivity<MainViewModel>(

) {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val viewModel: MainViewModel by viewModel()

    private var backPressTime = 0L

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == 200) {
            val data = activityResult.data
            data ?: return@registerForActivityResult

            val selectUrl = data.getStringExtra(MAIN_TO_HISTORY_DEFAULT).toString()
            binding.webView.loadUrl(selectUrl)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.isFabItemVisible = false
        binding.isCropImageViewVisible = false
    }

    override fun onPause() {
        super.onPause()

        // 마지막 접속한 페이지 저장
        viewModel.setLastUrl(binding.addressBar.text.toString())
    }

    override fun observeData() {
        viewModel.mainStateLiveData.observe(this) {
            when (it) {
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

    /** 뒤로가기 실행 시
     * 웹뷰에서 뒤로갈 수 있다면 이전 웹사이트로 이동
     * 없다면 1.5초 안에 2번 실행 시 종료
     * */
    override fun onBackPressed() {
        if (binding.cropImageView.isVisible) {
            binding.isCropImageViewVisible = false
        } else {
            if (binding.webView.canGoBack()) {
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

    /** 상단 툴바 초기화 */
    private fun initToolbar() {
        // 홈 버튼
        binding.goHomeButton.setOnDuplicatePreventionClickListener {
            binding.webView.loadUrl(viewModel.getSettingUrl())
        }

        // Keyboard search 클릭 시
        binding.addressBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val loadingUrl = v.text.toString()

                if (URLUtil.isNetworkUrl(loadingUrl)) {
                    binding.webView.loadUrl(loadingUrl)
                } else {
                    binding.webView.loadUrl("http://$loadingUrl")
                }

                // 키보드, 커서 숨기기
                currentFocus?.let { view ->
                    hideKeyboardAndCursor(this, view)
                }
            }
            true
        }

        // 뒤로가기 버튼
        binding.goBackButton.setOnDuplicatePreventionClickListener {
            binding.webView.goBack()
        }

        // 앞으로가기 버튼
        binding.goForwardButton.setOnDuplicatePreventionClickListener {
            binding.webView.goForward()
        }
    }

    /** 뷰 초기화 */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        // 아래로 스와이프 - 새로고침
        binding.refreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }

        // 웹뷰 초기화
        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true

            // 앱 실행 시 마지막 방문한 페이지로 이동
            loadUrl(viewModel.getLastUrl())

            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                binding.scrollValue = scrollY
            }
        }

        // 메인 플로팅 버튼
        binding.mainFloatingButton.setOnDuplicatePreventionClickListener {
            toggleFab()
        }

        // 웹뷰 스크롤 맨위로
        binding.moveTopFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            binding.webView.pageUp(true)
        }

        // 앱 설정 플로팅 버튼
        binding.appSettingFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            startForResult.launch(Intent(this, AppSettingActivity::class.java))
        }

        // 단어 노트 플로팅 버튼
        binding.noteFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            startActivity(Intent(this@MainActivity, NoteActivity::class.java))
        }

        // 스크린 캡처 플로팅 버튼
        binding.screenCaptureFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            showCaptureScreen()
        }

        // 스크린 캡처한 텍스트 추출
        binding.checkFloatingButton.setOnDuplicatePreventionClickListener {
            textExtractionFromCapture()
        }

    }

    /** 하단 배너광고 초기화 */
    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG, "광고 로드 성공 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e(TAG, "광고 로드 문제 발생 onAdFailedToLoad ${error.message}")
                }
            }
        }
    }

    /** 플로팅 Toggle(on/off) Animation */
    private fun toggleFab() {
        when (binding.isFabItemVisible) {
            true, null -> closeFloatingButtonWithAnimation()
            false -> openFloatingButtonWithAnimation()
        }
    }

    /** 플로팅 off Animation */
    private fun closeFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.moveTopTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.moveTopFloatingButton, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.appSettingTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.appSettingFloatingButton, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.noteTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.noteFloatingButton, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.screenCaptureTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.screenCaptureFloatingButton, TRANSLATION_Y, 0f).apply { start() }
        binding.mainFloatingButton.setImageResource(R.drawable.ic_add_24)
        binding.isFabItemVisible = false
    }

    /** 플로팅 on Animation */
    private fun openFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.moveTopTextView, TRANSLATION_Y, -680f).apply { start() }
        ObjectAnimator.ofFloat(binding.moveTopFloatingButton, TRANSLATION_Y, -680f).apply { start() }
        ObjectAnimator.ofFloat(binding.appSettingTextView, TRANSLATION_Y, -520f).apply { start() }
        ObjectAnimator.ofFloat(binding.appSettingFloatingButton, TRANSLATION_Y, -520f).apply { start() }
        ObjectAnimator.ofFloat(binding.noteTextView, TRANSLATION_Y, -360f).apply { start() }
        ObjectAnimator.ofFloat(binding.noteFloatingButton, TRANSLATION_Y, -360f).apply { start() }
        ObjectAnimator.ofFloat(binding.screenCaptureTextView, TRANSLATION_Y, -200f).apply { start() }
        ObjectAnimator.ofFloat(binding.screenCaptureFloatingButton, TRANSLATION_Y, -200f).apply { start() }
        binding.mainFloatingButton.setImageResource(R.drawable.ic_clear_24)
        binding.isFabItemVisible = true
    }

    /** 사용자 전체 화면 캡처 */
    private fun showCaptureScreen() {
        Handler(mainLooper).postDelayed({
            val rootView = window.decorView.rootView
            val bitmap = viewModel.getBitmapFromView(rootView)

            binding.cropImageView.setImageBitmap(bitmap)
            binding.isCropImageViewVisible = true
        }, CAPTURE_DELAY)
    }

    /** 화면 캡처 후 텍스트 추출 */
    private fun textExtractionFromCapture() {
        val selectedBitmap = binding.cropImageView.croppedImage
        selectedBitmap ?: return

        viewModel.readImageTextBitmap(selectedBitmap)
        binding.isCropImageViewVisible = false
    }

    /** 추출결과에 따른 분기 */
    private fun extractionComplete(extractionResult: String) {
        when (extractionResult) {
            // 너무 많은 글자 추출
            TEXT_LIMIT_EXCEEDED -> {
                Toast.makeText(this, getString(R.string.ocr_text_limit), Toast.LENGTH_SHORT).show()
            }

            // 텍스트 추출 에러
            EXTRACTION_ERROR -> {
                Toast.makeText(this, getString(R.string.ocr_error), Toast.LENGTH_SHORT).show()
            }

            // 정상
            else -> {
                val bottomDialogFragment = BottomDialogFragment(extractionResult)
                bottomDialogFragment.show(supportFragmentManager, bottomDialogFragment.tag)
            }
        }
    }

    inner class WebViewClient : android.webkit.WebViewClient() {

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
                goForwardButton.isEnabled = webView.canGoForward()
                addressBar.setText(url)
            }

            // 중복데이터 조회 후 없으면 삽입
            viewModel.insertAfterDuplicateDataLookup(url)
        }

    }

    inner class WebChromeClient : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            // 사이트 로드 상황에 따라 프로그레스바 업데이트
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