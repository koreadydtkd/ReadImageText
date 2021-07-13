package hys.hmonkeyys.readimagetext.views.licensedetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityLicenseDetailBinding

class LicenseDetailActivity : AppCompatActivity() {
    private val binding: ActivityLicenseDetailBinding by lazy {
        ActivityLicenseDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initStatusBar()

        binding.backButton.setOnClickListener {
            finish()
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
}