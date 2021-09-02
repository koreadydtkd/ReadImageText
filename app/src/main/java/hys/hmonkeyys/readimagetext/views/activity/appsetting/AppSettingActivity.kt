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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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

    private fun initViews() {
        binding.backButton.setOnDuplicatePreventionClickListener {
            finish()
        }

        binding.settingUrlEditText.setText(viewModel.getDefaultUrl())

        binding.urlEditButton.setOnDuplicatePreventionClickListener {
            viewModel.saveDefaultUrl(binding.settingUrlEditText.text.toString())
        }

        binding.historyButton.setOnDuplicatePreventionClickListener {
            startForResult.launch(Intent(this, HistoryActivity::class.java))
        }

        binding.licenseDetailButton.setOnDuplicatePreventionClickListener {
            startActivity(Intent(this, LicenseDetailActivity::class.java))
        }
    }

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

    companion object {
        private const val TAG = "HYS_AppSettingActivity"
    }
}