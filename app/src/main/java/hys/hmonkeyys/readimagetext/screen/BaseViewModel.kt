package hys.hmonkeyys.readimagetext.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal abstract class BaseViewModel : ViewModel() {

    open fun fetchData(): Job = viewModelScope.launch { }

}