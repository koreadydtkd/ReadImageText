package hys.hmonkeyys.readimagetext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import hys.hmonkeyys.readimagetext.databinding.ActivityIntroBinding
import hys.hmonkeyys.readimagetext.dialog.CustomDialog
import hys.hmonkeyys.readimagetext.room.WebDatabase
import hys.hmonkeyys.readimagetext.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private var db: WebDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = WebDatabase.getInstance(applicationContext)

        initStatusBar()
        initTextViews()

        checkPermissions()
    }

    private fun initStatusBar() {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.teal_200, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initTextViews() {
        binding.mainTextView.apply {
            val str = resources.getString(R.string.intro_main_contents)
            val ssb = SpannableStringBuilder(str)
            ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.intro_text_blue, null)), 7, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.intro_text_yellow, null)), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = ssb
        }

        binding.appVersionTextView.text = resources.getString(R.string.app_version, getAppVersionName())
    }

    private fun getAppVersionName(): String {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            ""
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
                Log.i(TAG, "권한 모두 허용됨")
                deleteData()

                checkUpdateVersion()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 7일 지난 데이터 삭제
    private fun deleteData() {
        CoroutineScope(Dispatchers.IO).launch {
            db?.historyDao()?.deleteDataOneWeeksAgo(Util().getDateWeeksAgo(1))
        }
    }

    private fun checkUpdateVersion() {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val updateVersion = remoteConfig.getLong(REMOTE_CONFIG_KEY)
                    Log.i(TAG, "$updateVersion")
                    if(updateVersion > Util().getAppVersion(applicationContext)) {
                        showUpdateDialog()
                    } else {
                        goMainActivity(1500L)
                    }
                } else {
                    goMainActivity(1500L)
                }
            }.addOnFailureListener {
                goMainActivity(1500L)
            }
    }

    private fun showUpdateDialog() {
        val customDialog = CustomDialog(dialogClickedListener = {
            goPlayStore()
        })
        customDialog.isCancelable = false
        customDialog.show(supportFragmentManager, customDialog.tag)
    }

    private fun goPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(APP_URI + packageName)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun goMainActivity(time: Long) {
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, time)
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
                            goMainActivity(750L)
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

    companion object {
        private const val TAG = "HYS_IntroActivity"

        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

        private const val REMOTE_CONFIG_KEY = "app_version"

        private const val APP_URI = "market://details?id="

    }
}