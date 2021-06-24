package hys.hmonkeyys.readimagetext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hys.hmonkeyys.readimagetext.databinding.ActivityLicenseDetailBinding

class LicenseDetailActivity : AppCompatActivity() {
    private val binding: ActivityLicenseDetailBinding by lazy {
        ActivityLicenseDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}