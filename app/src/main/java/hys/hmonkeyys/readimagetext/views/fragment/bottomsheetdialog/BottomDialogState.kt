package hys.hmonkeyys.readimagetext.views.fragment.bottomsheetdialog

sealed class BottomDialogState {

    data class TranslateComplete(
        val isSuccess: Boolean,
        val translateText: String = "hys",
    ) : BottomDialogState()

}