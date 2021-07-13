package hys.hmonkeyys.readimagetext.views.appsetting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityAppSettingBinding
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.history.HistoryActivity
import hys.hmonkeyys.readimagetext.views.licensedetail.LicenseDetailActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class AppSettingActivity : BaseActivity<AppSettingViewModel>() {
    private val binding: ActivityAppSettingBinding by lazy {
        ActivityAppSettingBinding.inflate(layoutInflater)
    }

    override val viewModel: AppSettingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.appSettingLiveData.observe(this) {
            when(it) {
                is AppSettingState.Initialized -> {
                    initStatusBar()
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

    private fun initStatusBar() {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.teal_200, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.settingUrlEditText.setText(viewModel.getDefaultUrl())

        binding.urlEditButton.setOnClickListener {
            viewModel.saveDefaultUrl(binding.settingUrlEditText.text.toString())
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.licenseDetailButton.setOnClickListener {
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

    companion object {
        private const val TAG = "HYS_AppSettingActivity"
    }
}