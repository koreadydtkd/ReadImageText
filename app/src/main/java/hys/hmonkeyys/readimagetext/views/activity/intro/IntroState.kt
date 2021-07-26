package hys.hmonkeyys.readimagetext.views.activity.intro

sealed class IntroState {

    object Initialized : IntroState()

    object CheckPermission : IntroState()

    data class NeedUpdate(
        val isUpdate: Boolean,
    ) : IntroState()
}