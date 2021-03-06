package hys.hmonkeyys.readimagetext.screen.views.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseActivity
import hys.hmonkeyys.readimagetext.screen.dialog.UrlInputDialog
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.AppSettingActivity
import hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog.BottomDialogFragment
import hys.hmonkeyys.readimagetext.screen.views.main.note.NoteActivity
import hys.hmonkeyys.readimagetext.utils.Constant.EXTRACTION_ERROR
import hys.hmonkeyys.readimagetext.utils.Constant.MAIN_TO_HISTORY_DEFAULT
import hys.hmonkeyys.readimagetext.utils.Constant.TEXT_LIMIT_EXCEEDED
import hys.hmonkeyys.readimagetext.utils.Utility.hideKeyboardAndCursor
import hys.hmonkeyys.readimagetext.utils.Utility.toast
import java.util.*

@AndroidEntryPoint
internal class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val viewModel: MainViewModel by viewModels()
    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    // 방문했던 url 데이터 전달 시 webView load
    private val getSelectedUrlResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == 200) {
            val data = activityResult.data
            data ?: return@registerForActivityResult

            val selectUrl = data.getStringExtra(MAIN_TO_HISTORY_DEFAULT).toString()
            binding.webView.loadUrl(selectUrl)
        }
    }

    // 내장된 카메라로 사진찍고 text 추출
    private val getCameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            val imageBitmap = activityResult.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { bitmap ->
                viewModel.readImageTextBitmap(bitmap)
            }
        }
    }

    private val floatingButtons by lazy {
        listOf(binding.moveTopTextView, binding.moveTopFloatingButton, binding.appSettingTextView, binding.appSettingFloatingButton,
            binding.noteTextView, binding.noteFloatingButton, binding.cameraTextView, binding.cameraFloatingButton,
            binding.screenCaptureTextView, binding.screenCaptureFloatingButton)
    }

    private lateinit var dialog: UrlInputDialog

    private var backPressTime = 0L

    /** 뷰 초기화 */
    @SuppressLint("SetJavaScriptEnabled")
    override fun initViews() = with(binding) {
        // data binding 기본 값 설정
        isFabItemVisible = false
        isCropImageViewVisible = false

        // 홈 버튼
        goHomeButton.setOnDuplicatePreventionClickListener {
            webView.loadUrl(viewModel.getSettingUrl())
        }

        // Keyboard search 클릭 시
        addressBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val loadingUrl = v.text.toString()

                // https 주소 체크
                val url = if (URLUtil.isNetworkUrl(loadingUrl)) loadingUrl else "http://$loadingUrl"
                webView.loadUrl(url)

                // 키보드, 커서 숨기기
                currentFocus?.let { view -> hideKeyboardAndCursor(this@MainActivity, view) }
            }
            true
        }

        // 뒤로가기 버튼
        goBackButton.setOnDuplicatePreventionClickListener {
            webView.goBack()
        }

        // 앞으로가기 버튼
        goForwardButton.setOnDuplicatePreventionClickListener {
            webView.goForward()
        }

        // 아래로 스와이프 - 새로고침
        refreshLayout.setOnRefreshListener {
            webView.reload()
        }

        // 웹뷰 초기화
        webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true

            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                scrollValue = scrollY
            }
        }

        // 메인 플로팅 버튼
        mainFloatingButton.setOnDuplicatePreventionClickListener {
            toggleFab()
        }

        // 웹뷰 스크롤 맨위로
        moveTopFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            webView.pageUp(true)
        }

        // 앱 설정 플로팅 버튼
        appSettingFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            getSelectedUrlResult.launch(Intent(this@MainActivity, AppSettingActivity::class.java))
        }

        // 단어 노트 플로팅 버튼
        noteFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            startActivity(Intent(this@MainActivity, NoteActivity::class.java))
        }

        // 카메라 플로팅 버튼
        cameraFloatingButton.setOnClickListener {
            closeFloatingButtonWithAnimation()
            executeCamera()
        }

        // 스크린 캡처 플로팅 버튼
        screenCaptureFloatingButton.setOnDuplicatePreventionClickListener {
            closeFloatingButtonWithAnimation()
            showCaptureScreen()
        }

        // 스크린 캡처한 텍스트 추출
        checkFloatingButton.setOnDuplicatePreventionClickListener {
            textExtractionFromCapture()
        }

        initAdmob()
    }

    /** 하단 배너광고 초기화 */
    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
//                    Log.i(TAG, "광고 로드 성공 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
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
        floatingButtons.forEach {
            ObjectAnimator.ofFloat(it, TRANSLATION_Y, 0f).apply { start() }
        }
        binding.mainFloatingButton.setImageResource(R.drawable.ic_add)
        binding.isFabItemVisible = false
    }

    /** 플로팅 on Animation */
    private fun openFloatingButtonWithAnimation() {
        floatingButtons.forEach {
            ObjectAnimator.ofFloat(it, TRANSLATION_Y, it.tag.toString().toFloat()).apply { start() }
        }
        binding.mainFloatingButton.setImageResource(R.drawable.ic_clear)
        binding.isFabItemVisible = true
    }

    /** 카메라 사진 찍기 */
    private fun executeCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)
        }
        getCameraResult.launch(intent)
    }

    /** 사용자 전체 화면 캡처 */
    private fun showCaptureScreen() {
        Handler(mainLooper).postDelayed({
            val bitmap = viewModel.getBitmapFromView(window.decorView.rootView)

            binding.cropImageView.setImageBitmap(bitmap)
            binding.isCropImageViewVisible = true
        }, CAPTURE_DELAY)
    }

    /** 화면 캡처 후 텍스트 추출 */
    private fun textExtractionFromCapture() {
        binding.cropImageView.croppedImage?.let { selectedBitmap ->
            viewModel.readImageTextBitmap(selectedBitmap)
            binding.isCropImageViewVisible = false
        }
    }

    override fun observeData() {
        viewModel.mainStateLiveData.observe(this) {
            when (it) {
                is MainState.Initialized -> checkFirstInstall()

                is MainState.TextExtractionComplete -> extractionComplete(it.result)
            }
        }
    }

    /** 처음 실행 여부에 따른 분기처리 */
    private fun checkFirstInstall() {
        if (viewModel.isFirst()) {
            showUrlInputDialog()
        } else {
            // 앱 실행 시 마지막 방문한 페이지로 이동
            binding.webView.loadUrl(viewModel.getLastUrl())
        }
    }

    /** Url Input Dialog 띄우기 */
    private fun showUrlInputDialog() {
        dialog = UrlInputDialog(dialogClickedListener = {
            saveUrl(it)
        })
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, dialog.tag)
    }

    /** 예외 처리 후 저장 */
    private fun saveUrl(url: String) {
        val errorMessage = when {
            url.isEmpty() -> getString(R.string.input_address)
            url.contains(" ") -> getString(R.string.not_allow_empty)
            else -> ""
        }

        if (errorMessage.isEmpty()) {
            dialog.dismiss()
            viewModel.setUrl(url)
            viewModel.setFirst()
            binding.webView.loadUrl(url)
        } else {
            toast(this, errorMessage)
        }
    }

    /** 추출결과에 따른 분기 */
    private fun extractionComplete(extractionResult: String) {
        when (extractionResult) {
            // 너무 많은 글자 추출
            TEXT_LIMIT_EXCEEDED -> toast(this, getString(R.string.ocr_text_limit))

            // 텍스트 추출 에러
            EXTRACTION_ERROR -> toast(this, getString(R.string.ocr_error))

            // 정상
            else -> showBottomDialog(extractionResult)
        }
    }

    /** 하단 다이얼로그 띄우기 */
    private fun showBottomDialog(extractionResult: String) {
        val bottomDialogFragment = BottomDialogFragment(extractionResult)
        bottomDialogFragment.show(supportFragmentManager, bottomDialogFragment.tag)
    }

    /** WebViewClient 설정
     * 페이지 로드 시: progress 보여주기
     * 페이지 완료 시: refresh 및 progress 숨기기, toolbar 설정
     * */
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

    /** WebChromeClient progress 변경 감지하여 프로그레스바 보여주기 */
    inner class WebChromeClient : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            // 사이트 로드 상황에 따라 프로그레스바 업데이트
            binding.progressBar.progress = newProgress
        }
    }

    override fun onPause() {
        super.onPause()

        // 마지막 접속한 페이지 저장
        viewModel.setUrl(binding.addressBar.text.toString())
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
                    toast(this, getString(R.string.backward_finish))
                    backPressTime = time
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    companion object {
        //        private const val TAG = "HYS_MainActivity"
        private const val TRANSLATION_Y = "translationY"
        private const val ONE_POINT_FIVE_SECOND = 1500L
        private const val CAPTURE_DELAY = 400L
    }
}