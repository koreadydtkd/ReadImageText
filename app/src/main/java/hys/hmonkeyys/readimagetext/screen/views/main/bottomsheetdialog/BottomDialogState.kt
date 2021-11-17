package hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog

sealed class BottomDialogState {

    data class TranslationComplete(
        val translateText: String = "",
    ) : BottomDialogState()

    object TranslationFailed : BottomDialogState()

    object InsertComplete : BottomDialogState()
}