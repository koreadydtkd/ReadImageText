package hys.hmonkeyys.readimagetext.views.main


sealed class MainState {
    object Initialized: MainState()

    data class TextExtractionComplete(
        val result: String
    ): MainState()
}