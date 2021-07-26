package hys.hmonkeyys.readimagetext.views.activity.main


sealed class MainState {
    object Initialized : MainState()

    data class TextExtractionComplete(
        val result: String,
    ) : MainState()
}