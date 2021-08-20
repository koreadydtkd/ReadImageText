package hys.hmonkeyys.readimagetext.views.activity.guide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hys.hmonkeyys.readimagetext.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity() {

    private val binding: ActivityGuideBinding by lazy {
        ActivityGuideBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}