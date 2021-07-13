package hys.hmonkeyys.readimagetext.fragment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

internal abstract class BaseFragmentViewModel: ViewModel() {

    abstract fun fetchData(): Job

}