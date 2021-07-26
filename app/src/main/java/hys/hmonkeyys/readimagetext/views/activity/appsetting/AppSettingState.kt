package hys.hmonkeyys.readimagetext.views.activity.appsetting

sealed class AppSettingState {

    object Initialized : AppSettingState()

    object UrlChangeComplete : AppSettingState()

    object SpeedChangeComplete : AppSettingState()

}