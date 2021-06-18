package hys.hmonkeyys.readimagetext.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomSheetDialogViewModel : ViewModel() {
    var translateCount = MutableLiveData<Int>()

    init {
        translateCount.value = 0
    }

    fun increaseCount() {
        translateCount.value = translateCount.value?.plus(1)
    }

    fun setDefaultValue() {
        translateCount.value = 0
    }
}