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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import hys.hmonkeyys.readimagetext.customdialog.CustomDialog
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
                val selectUrl = data.getStringExtra(Util.MAIN_TO_HISTORY_DEFAULT).toString()
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

        val customDialog = CustomDialog()
        customDialog.show(supportFragmentManager, customDialog.tag)
    }

    override fun onStop() {
        super.onStop()

        // 마지막 접속한 페이지 저장
        spf.edit().putString(SharedPreferencesConst.LAST_URL, binding.addressBar.text.toString()).apply()
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
                Log.d(TAG, "권한 모두 허용됨")
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
//                            Util(applicationContext).downloadGoogleTranslator()
//                            if(!spf.getBoolean(SharedPreferencesConst.IS_FIRST, false)) {
//                                spf.edit().putBoolean(SharedPreferencesConst.IS_FIRST, true).apply()
                                // todo 커스텀 다이얼로그 띄우기 - 간단한 앱 안내
//                                val customDialog = CustomDialog()
//                                customDialog.show(supportFragmentManager, customDialog.tag)
//                            }
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
        private const val TAG = "HYS_MainActivity"

        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

        private const val DEFAULT_URL = "https://www.google.com"
        private const val TRANSLATION_Y = "translationY"

        private const val REQUEST_CODE = 1014

        private const val OCR_TEXT_LIMIT = 350
    }

}