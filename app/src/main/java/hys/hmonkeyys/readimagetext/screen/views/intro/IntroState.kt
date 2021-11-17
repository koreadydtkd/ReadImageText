package hys.hmonkeyys.readimagetext.screen.views.intro

sealed class IntroState {

    object CheckPermission : IntroState()

    object NeedUpdate : IntroState()

    object NoUpdateRequired : IntroState()
}