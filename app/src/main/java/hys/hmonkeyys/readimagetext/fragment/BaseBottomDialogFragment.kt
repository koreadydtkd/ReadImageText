package hys.hmonkeyys.readimagetext.fragment

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job

internal abstract class BaseBottomDialogFragment<VM: BaseFragmentViewModel>: BottomSheetDialogFragment() {

    abstract val viewModel: VM

    private lateinit var fetchJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchJob = viewModel.fetchData()
        observeData()
    }

    abstract fun observeData()

    override fun onDestroy() {
        if(fetchJob.isActive) {
            fetchJob.cancel()
        }
        super.onDestroy()
    }
}