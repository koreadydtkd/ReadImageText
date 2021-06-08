package hys.hmonkeyys.readimagetext

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import hys.hmonkeyys.readimagetext.databinding.ActivityMainBinding
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val spf: SharedPreferences by lazy {
        getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private var db: WebDatabase? = null

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == REQUEST_CODE) {
            activityResult.data?.let { data ->
                val selectUrl = data.getStringExtra("select_url").toString()
                binding.webView.loadUrl(selectUrl)
            } ?: run {
                Log.d(TAG, "data is null!!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = WebDatabase.getInstance(applicationContext)

        binding.isFabItemVisible = false
        binding.isCropImageViewVisible = false

        initToolbar()
        initWebView()
        initViews()
        initAdmob()

        checkPermissions()
    }

    // 상단 툴바 초기화
    private fun initToolbar() {
        binding.goHomeButton.setOnClickListener {
            binding.webView.loadUrl(getLoadUrl())
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
            loadUrl(getLoadUrl())

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

//            val captureCount = spf.getInt(SharedPreferencesConst.CAPTURE_COUNT, 0)
//            if(captureCount > 10) {
//                // todo 인앱결제 구현
//                Toast.makeText(this, "무료 횟수를 모두 이용하였습니다.", Toast.LENGTH_SHORT).show()
//            } else {
                Handler(mainLooper).postDelayed({
//                    spf.edit().putInt(SharedPreferencesConst.CAPTURE_COUNT, captureCount + 1).apply()

                    val rootView = this.window.decorView.rootView
                    val bitmap = getBitmapFromView(rootView)

                    binding.cropImageView.setImageBitmap(bitmap)
                    binding.isCropImageViewVisible = true
                }, 300)
//            }

        }

        binding.fabHistory.setOnClickListener {
            closeFloatingButtonWithAnimation()
            startForResult.launch(Intent(this, HistoryActivity::class.java))
        }

        binding.fabAppInfo.setOnClickListener {
            closeFloatingButtonWithAnimation()
            startActivity(Intent(this, AppInformationActivity::class.java))
        }

        binding.fabCheck.setOnClickListener {
            val selectedBitmap = binding.cropImageView.croppedImage
            selectedBitmap ?: return@setOnClickListener

            readImageTextBitmap(selectedBitmap)

            binding.isCropImageViewVisible = false
        }

    }

    // 번역에 필요한 데이터 다운
    private fun downloadGoogleTranslator() {
        // 다운로드 안한 경우에만 다운
        if(spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, false).not() ||
            spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, false).not() ||
            spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false).not()) {

            Toast.makeText(applicationContext, "번역에 필요한 파일을 다운받습니다.\n잠시만 기다려주세요.", Toast.LENGTH_LONG).show()

            val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
            val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
            val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()

            CoroutineScope(Dispatchers.IO).launch {
                downloadTranslateModel(englishModel)
                downloadTranslateModel(japaneseModel)
                downloadTranslateModel(koreanModel)
            }

        }

    }

    private fun downloadTranslateModel(translateModel: TranslateRemoteModel) {
        val modelManager = RemoteModelManager.getInstance()

        val conditions = DownloadConditions.Builder()
            .requireCharging()
            .build()

        modelManager.download(translateModel, conditions)
            .addOnSuccessListener {
                when(translateModel.language) {
                    TranslateLanguage.ENGLISH -> {
                        Log.e(TAG, "영어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, true).apply()
                    }
                    TranslateLanguage.JAPANESE -> {
                        Log.e(TAG, "일본어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, true).apply()
                    }
                    TranslateLanguage.KOREAN -> {
                        Log.e(TAG, "한국어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, true).apply()
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.toString())
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
        ObjectAnimator.ofFloat(binding.fabAppInfoTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfo, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabHistoryTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabHistory, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCaptureTextView, TRANSLATION_Y, 0f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabScreenCapture, TRANSLATION_Y, 0f).apply { start() }
        binding.fabMain.setImageResource(R.drawable.ic_add_24)
        binding.isFabItemVisible = false
    }

    private fun openFloatingButtonWithAnimation() {
        ObjectAnimator.ofFloat(binding.fabAppInfoTextView, TRANSLATION_Y, -600f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabAppInfo, TRANSLATION_Y, -600f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabHistoryTextView, TRANSLATION_Y, -400f).apply { start() }
        ObjectAnimator.ofFloat(binding.fabHistory, TRANSLATION_Y, -400f).apply { start() }
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

    // 7일 전 날짜 가져오기
    private fun getDateWeeksAgo(selectWeek: Int): String {
        val week = Calendar.getInstance()
        week.add(Calendar.DATE, (selectWeek * -7))
        return SimpleDateFormat("yyyy-MM-dd").format(week.time)
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
            } else {
                Log.e(TAG, "권한 모두 허용됨")
                deleteData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 지난 데이터 삭제
    private fun deleteData() {
        CoroutineScope(Dispatchers.IO).launch {
            db?.historyDao()?.deleteDataOneWeeksAgo(getDateWeeksAgo(1))
        }
    }

    // 퍼미션 권한 허용 요청에 대한 결과
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            when (requestCode) {
                PERMISSIONS_REQUEST_CODE -> {
                    if(grantResults.isNotEmpty()) {
                        var isAllGranted = true
                        for(grant in grantResults) {
                            if(grant != PackageManager.PERMISSION_GRANTED) {
                                isAllGranted = false
                                break
                            }
                        }

                        if(isAllGranted) {
                            // 모든 권한 허용
                            Util(applicationContext).downloadGoogleTranslator()
                            Toast.makeText(applicationContext, "번역에 필요한 파일을 다운받습니다.\n잠시만 기다려주세요.", Toast.LENGTH_LONG).show()
//                            downloadGoogleTranslator()
                        } else {
                            // 권한 불허
                            Log.e(TAG, "권한 미 허용")
                            Toast.makeText(this, resources.getString(R.string.decline_permissions), Toast.LENGTH_LONG).show()
                            finish()
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

            CoroutineScope(Dispatchers.IO).launch {
                // 중복 확인을 위한 쿼리 : 0 반환 시 데이터 없음
                val haveData = db?.historyDao()?.findByHistory(url ?: "", getCurrentDate()) ?: 0
                if(haveData < 1) {
                    db?.historyDao()?.insertHistory(WebHistoryModel(null, url, getCurrentDate()))
                }
            }

        }

        private fun getCurrentDate(): String {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            return sdf.format(date)
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

        private const val REQUEST_CODE = 1014
    }

}