package hys.hmonkeyys.readimagetext.views.activity.intro

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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityIntroBinding
import hys.hmonkeyys.readimagetext.utils.Utility.getAppVersionCode
import hys.hmonkeyys.readimagetext.utils.Utility.getAppVersionName
import hys.hmonkeyys.readimagetext.views.dialog.CustomDialog
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.activity.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList

internal class IntroActivity : BaseActivity<IntroViewModel>(

) {
    private val binding: ActivityIntroBinding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    override val viewModel: IntroViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.introLiveData.observe(this) {
            when (it) {
                is IntroState.Initialized -> {
                    initViews()
                }
                is IntroState.CheckPermission -> {
                    checkPermissions()
                }
                is IntroState.NeedUpdate -> {
                    if (it.isNeedUpdate) {
                        showUpdateDialog()
                    } else {
                        goMainActivity()
                    }
                }
            }
        }
    }

    /** 각 뷰들 초기화 */
    private fun initViews() {
        binding.mainTextView.apply {
            val str = getString(R.string.intro_main_contents)
            val ssb = SpannableStringBuilder(str)

            // '번역' 글자 파란색으로 변경
            ssb.setSpan(ForegroundColorSpan(getColor(R.color.intro_text_blue)), 7, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // '듣기' 글자 노란색으로 변경
            ssb.setSpan(ForegroundColorSpan(getColor(R.color.intro_text_yellow)), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = ssb
        }

        binding.appVersionTextView.text = getString(R.string.app_version, getAppVersionName(this@IntroActivity))
    }

    /** 권한 체크 */
    private fun checkPermissions() {
        try {
            val rejectedPermissionList = ArrayList<String>()

            // 필요한 퍼미션들을 하나씩 권한을 받았는지 확인
            for (permission in REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없으면 추가
                    rejectedPermissionList.add(permission)
                }
            }

            // 거절한 퍼미션이 있으면 권한 요청
            if (rejectedPermissionList.isNotEmpty()) {
                val array = arrayOfNulls<String>(rejectedPermissionList.size)
                ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), PERMISSIONS_REQUEST_CODE)
            } else {
                // 권한 모두 허용

                viewModel.deleteData()

                val currentVersion = getAppVersionCode(this)
                viewModel.checkUpdateVersion(currentVersion)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 퍼미션 권한 허용 요청에 대한 결과 */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            when (requestCode) {
                PERMISSIONS_REQUEST_CODE -> {
                    if (grantResults.isNotEmpty()) {
                        var isAllGranted = true
                        for (grant in grantResults) {
                            if (grant != PackageManager.PERMISSION_GRANTED) {
                                isAllGranted = false
                                break
                            }
                        }

                        if (isAllGranted) {
                            // 모든 권한 허용
                            goMainActivity()
                        } else {
                            // 권한 불허
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

    /** 메인 화면으로 이동 */
    private fun goMainActivity() {
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, MAIN_MOVE_DELAY)
    }

    /** 취소 못하는 업데이트 다이얼로그 띄우기 */
    private fun showUpdateDialog() {
        val customDialog = CustomDialog(dialogClickedListener = {
            goPlayStore()
        })
        customDialog.isCancelable = false
        customDialog.show(supportFragmentManager, customDialog.tag)
    }

    /** 플레이스토어로 이동 */
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
        private const val APP_URI = "market://details?id="
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    }
}