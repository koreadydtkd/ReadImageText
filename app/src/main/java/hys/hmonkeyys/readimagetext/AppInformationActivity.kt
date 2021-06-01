package hys.hmonkeyys.readimagetext

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.databinding.ActivityAppInformationBinding


class AppInformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppInformationBinding

    private val spf: SharedPreferences by lazy {
        getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    // 중복 방지
    private var duplicateProtection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSpinner()
        initViews()
    }

    private fun initSpinner() {
        val items = resources.getStringArray(R.array.spinner_items)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        val ttsSpeed = spf.getFloat(SharedPreferencesConst.TTS_SPEED, 0.8f)

        binding.speakSpeedSpinner.apply {
            adapter = spinnerAdapter

            when(ttsSpeed) {
                1.2f -> setSelection(0)
                1.1f -> setSelection(1)
                1.0f -> setSelection(2)
                0.9f -> setSelection(3)
                0.8f -> setSelection(4)
                0.7f -> setSelection(5)
                0.6f -> setSelection(6)
                0.5f -> setSelection(7)
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> saveTTSSpeed(1.2f)
                        1 -> saveTTSSpeed(1.1f)
                        2 -> saveTTSSpeed(1.0f)
                        3 -> saveTTSSpeed(0.9f)
                        4 -> saveTTSSpeed(0.8f)
                        5 -> saveTTSSpeed(0.7f)
                        6 -> saveTTSSpeed(0.6f)
                        7 -> saveTTSSpeed(0.5f)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }

        }
    }

    private fun saveTTSSpeed(tts_speed: Float) {
        if(duplicateProtection) {
            Log.d(TAG, "스피치 속도 변경 $tts_speed")
            spf.edit().putFloat(SharedPreferencesConst.TTS_SPEED, tts_speed).apply()
            Toast.makeText(this, "속도가 변경되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            duplicateProtection = true
        }

    }

    private fun initViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.settingUrlEditText.setText(spf.getString(SharedPreferencesConst.SETTING_URL, DEFAULT_URL))

        binding.urlEditButton.setOnClickListener {
            val changeUrl = binding.settingUrlEditText.text.toString()
            spf.edit().putString(SharedPreferencesConst.SETTING_URL, changeUrl).apply()
            Toast.makeText(this, "기본 주소가 변경되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 라이센스 TextView
        binding.licenseDetailTextView.setOnClickListener {
            startActivity(Intent(this, LicenseDetailActivity::class.java))
        }

        // 앱 버전 TextView
        val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        binding.appVersionTextView.text = info.versionName

    }

    override fun onPause() {
        super.onPause()
        duplicateProtection = false
    }

    companion object {
        const val TAG = "AppInformationActivity"
        private const val DEFAULT_URL = "https://www.google.com"
    }
}