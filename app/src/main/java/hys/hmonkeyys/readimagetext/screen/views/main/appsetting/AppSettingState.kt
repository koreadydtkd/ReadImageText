package hys.hmonkeyys.readimagetext.screen.views.main.appsetting

sealed class AppSettingState {

    object UrlChangeComplete : AppSettingState()

    object SpeedChangeComplete : AppSettingState()

}