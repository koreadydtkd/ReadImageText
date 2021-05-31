package hys.hmonkeyys.readimagetext

import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.databinding.ActivityAppInfotmationBinding


class AppInformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppInfotmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInfotmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appVersionTextView.text = getVersionInfo()
    }

    fun getVersionInfo(): String {
        val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        return info.versionName
    }

}