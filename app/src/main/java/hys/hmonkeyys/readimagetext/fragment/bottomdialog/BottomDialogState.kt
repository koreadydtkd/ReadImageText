package hys.hmonkeyys.readimagetext.fragment.bottomdialog

sealed class BottomDialogState {

//    object Initialized: BottomDialogState()

    data class TranslateComplete(
        val isSuccess: Boolean,
        val translateText: String = "hys"
    ): BottomDialogState()

}