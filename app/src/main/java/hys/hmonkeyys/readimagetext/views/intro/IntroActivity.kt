package hys.hmonkeyys.readimagetext.views.intro

import android.Manifest
import android.content.Intent
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityIntroBinding
import hys.hmonkeyys.readimagetext.fragment.dialog.CustomDialog
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList


internal class IntroActivity : BaseActivity<IntroViewModel>(

) {
    private lateinit var binding: ActivityIntroBinding

    override val viewModel: IntroViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.introLiveData.observe(this) {
            when(it) {
                is IntroState.Initialized -> {
                    initStatusBar()
                    initTextViews()
                }
                is IntroState.CheckPermission -> {
                    checkPermissions()
                }
                is IntroState.NeedUpdate -> {
                    if(it.isUpdate) {
                        showUpdateDialog()
                    } else {
                        goMainActivity(MAIN_MOVE_DELAY)
                    }
                }
            }
        }
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

        binding.appVersionTextView.text = resources.getString(R.string.app_version, Util().getAppVersionName(this@IntroActivity))
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
                ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array),
                    PERMISSIONS_REQUEST_CODE)
            } else {
                Log.i(TAG, "권한 모두 허용")
                viewModel.deleteData()

                val currentVersion = Util().getAppVersionCode(this)
                viewModel.checkUpdateVersion(currentVersion)
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
                        var isAllGranted = true
                        for(grant in grantResults) {
                            if(grant != PackageManager.PERMISSION_GRANTED) {
                                isAllGranted = false
                                break
                            }
                        }

                        if(isAllGranted) {
                            // 모든 권한 허용
                            goMainActivity(MAIN_MOVE_DELAY)
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

    private fun goMainActivity(time: Long) {
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, time)
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

    companion object {
        private const val TAG = "HYS_IntroActivity"

        private const val MAIN_MOVE_DELAY = 1500L

        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)

        private const val APP_URI = "market://details?id="

    }
}