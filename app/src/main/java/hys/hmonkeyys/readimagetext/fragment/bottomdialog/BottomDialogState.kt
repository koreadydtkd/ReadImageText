package hys.hmonkeyys.readimagetext.fragment.bottomdialog

sealed class BottomDialogState {

    data class TranslateComplete(
        val isSuccess: Boolean,
        val translateText: String = "hys",
    ) : BottomDialogState()

}