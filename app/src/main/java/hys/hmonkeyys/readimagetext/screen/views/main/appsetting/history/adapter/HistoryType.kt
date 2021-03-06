package hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter

open class HistoryType(
    val type: Int,
) {
    companion object {
        const val DATE = 0
        const val URL = 1
    }
}

class DateType(type: Int) : HistoryType(type) {
    var date: String? = ""
}

class UrlType(type: Int) : HistoryType(type) {
    var uid: Int = 0
    var loadUrl: String? = ""
}