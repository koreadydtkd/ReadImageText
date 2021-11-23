package hys.hmonkeyys.readimagetext.screen.views.main.appsetting

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isGone
import dagger.hilt.android.AndroidEntryPoint
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityAppSettingBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseActivity
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.HistoryActivity
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.licensedetail.LicenseDetailActivity
import hys.hmonkeyys.readimagetext.utils.Constant.MAIN_TO_HISTORY_DEFAULT
import hys.hmonkeyys.readimagetext.utils.Utility.hideKeyboardAndCursor
import hys.hmonkeyys.readimagetext.utils.Utility.isKorean
import hys.hmonkeyys.readimagetext.utils.Utility.toast
import java.util.*

@AndroidEntryPoint
internal class AppSettingActivity : BaseActivity<AppSettingViewModel, ActivityAppSettingBinding>() {

    override val viewModel: AppSettingViewModel by viewModels()
    override fun getViewBinding(): ActivityAppSettingBinding = ActivityAppSettingBinding.inflate(layoutInflater)

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == 200) {
            val data = activityResult.data
            data ?: return@registerForActivityResult

            val selectUrl = data.getStringExtra(MAIN_TO_HISTORY_DEFAULT).toString()

            val intent = Intent().apply {
                putExtra(MAIN_TO_HISTORY_DEFAULT, selectUrl)
            }
            setResult(200, intent)
            finish()
        }
    }

    /** 뷰 초기화 */
    override fun initViews() = with(binding) {
        // 설정된 주소 셋팅
        settingUrlEditText.setText(viewModel.getDefaultUrl())

        // 뒤로 가기 버튼
        backButton.setOnDuplicatePreventionClickListener {
            finish()
        }

        // 주소 설정 버튼
        urlEditButton.setOnDuplicatePreventionClickListener {
            viewModel.saveDefaultUrl(settingUrlEditText.text.toString())

            // 키보드, 커서 숨기기
            currentFocus?.let { view ->
                hideKeyboardAndCursor(this@AppSettingActivity, view)
            }
        }

        // 방문기록 버튼
        historyButton.setOnDuplicatePreventionClickListener {
            startForResult.launch(Intent(this@AppSettingActivity, HistoryActivity::class.java))
        }

        // 라이센스 이용약관 버튼
        licenseDetailButton.setOnDuplicatePreventionClickListener {
            startActivity(Intent(this@AppSettingActivity, LicenseDetailActivity::class.java))
        }

        // 읽기 속도 스피너 초기화
        if (isKorean()) {
            setKoreanViews()
        } else {
            setOtherLangViews()
        }
    }

    /** 한국어 설정인 경우 - 읽기 속도 변경 그대로 */
    private fun setKoreanViews() {
        val items = resources.getStringArray(R.array.spinner_items)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        binding.speakSpeedSpinner.apply {
            adapter = spinnerAdapter

            setSelection(viewModel.getTTsSpeed(), false)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.saveTTSSpeed(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /** 영어 설정인 경우 - 읽기를 사용하지 않기 때문에 속도조절 없애기 */
    private fun setOtherLangViews() {
        binding.speakSpeedTextView.isGone = true
        binding.speakSpeedView.isGone = true
        binding.speakSpeedCardView.isGone = true
    }

    override fun observeData() {
        viewModel.appSettingLiveData.observe(this) {
            when (it) {
                is AppSettingState.UrlChangeComplete -> toast(this, getString(R.string.change_default_url))

                is AppSettingState.SpeedChangeComplete -> toast(this, getString(R.string.change_speed))
            }
        }
    }

    companion object {
//        private const val TAG = "HYS_AppSettingActivity"
    }
}