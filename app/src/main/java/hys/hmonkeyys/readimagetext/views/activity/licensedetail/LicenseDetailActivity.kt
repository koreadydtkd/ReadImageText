package hys.hmonkeyys.readimagetext.views.activity.licensedetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.utils.setOnDuplicatePreventionClickListener

class LicenseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license_detail)

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = getColor(R.color.teal_200)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        findViewById<Button>(R.id.backButton).setOnDuplicatePreventionClickListener {
            finish()
        }
    }
}