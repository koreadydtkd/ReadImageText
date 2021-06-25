package hys.hmonkeyys.readimagetext.adapter

open class HistoryType(
    val type : Int
) {
    companion object{
        const val DATE = 0
        const val ADDRESS = 1
    }
}

class DateType(type: Int) : HistoryType(type) {
    var date : String? = ""
}
class AddressType(type: Int) : HistoryType(type) {
    var loadUrl : String? = ""
}