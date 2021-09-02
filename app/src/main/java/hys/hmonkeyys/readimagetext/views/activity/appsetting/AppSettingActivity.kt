package hys.hmonkeyys.readimagetext.views.activity.appsetting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityAppSettingBinding
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.utils.Utility.MAIN_TO_HISTORY_DEFAULT
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.activity.history.HistoryActivity
import hys.hmonkeyys.readimagetext.views.activity.licensedetail.LicenseDetailActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class AppSettingActivity : BaseActivity<AppSettingViewModel>() {

    private val binding: ActivityAppSettingBinding by lazy { ActivityAppSettingBinding.inflate(layoutInflater) }
    override val viewModel: AppSettingViewModel by viewModel()

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == 200) {
            val data = activityResult.data
            data ?: return@registerForActivityResult

            val selectUrl = data.getStringExtra(MAIN_TO_HISTORY_DEFAULT).toString()

            val intent = Intent()
            intent.putExtra(MAIN_TO_HISTORY_DEFAULT, selectUrl)
            setResult(200, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 설정된 주소 셋팅
        binding.settingUrlEditText.setText(viewModel.getDefaultUrl())
    }

    override fun observeData() {
        viewModel.appSettingLiveData.observe(this) {
            when (it) {
                is AppSettingState.Initialized -> {
                    initViews()
                    initSpinner()
                }
                is AppSettingState.UrlChangeComplete -> {
                    Toast.makeText(this, resources.getString(R.string.change_default_url), Toast.LENGTH_SHORT).show()
                }
                is AppSettingState.SpeedChangeComplete -> {
                    Toast.makeText(this, resources.getString(R.string.change_speed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 뷰 초기화 */
    private fun initViews() {
        // 뒤로 가기 버튼
        binding.backButton.setOnDuplicatePreventionClickListener {
            finish()
        }

        // 주소 설정 버튼
        binding.urlEditButton.setOnDuplicatePreventionClickListener {
            viewModel.saveDefaultUrl(binding.settingUrlEditText.text.toString())
        }

        // 방문기록 버튼
        binding.historyButton.setOnDuplicatePreventionClickListener {
            startForResult.launch(Intent(this, HistoryActivity::class.java))
        }

        // 라이센스 이용약관 버튼
        binding.licenseDetailButton.setOnDuplicatePreventionClickListener {
            startActivity(Intent(this, LicenseDetailActivity::class.java))
        }
    }

    /** 읽기 속도 스피너 초기화 */
    private fun initSpinner() {
        val items = resources.getStringArray(R.array.spinner_items)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        binding.speakSpeedSpinner.apply {
            adapter = spinnerAdapter

            setSelection(viewModel.getTTsSpeed(), false)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.saveTTSSpeed(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }

    }

    companion object {
        private const val TAG = "HYS_AppSettingActivity"
    }
}