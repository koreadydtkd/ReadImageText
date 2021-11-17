package hys.hmonkeyys.readimagetext.extensions

/** . ! ? ~ 포함여부 확인 확장함수 */
fun Char.isSpecialSymbols(): Boolean {
    if (this.toString() == "." || this.toString() == "!" || this.toString() == "?" || this.toString() == "~") {
        return true
    }
    return false
}