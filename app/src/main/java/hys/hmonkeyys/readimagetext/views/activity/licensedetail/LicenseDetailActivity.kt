package hys.hmonkeyys.readimagetext.views.activity.licensedetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener

class LicenseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license_detail)

        findViewById<Button>(R.id.backButton).setOnDuplicatePreventionClickListener {
            finish()
        }
    }
}