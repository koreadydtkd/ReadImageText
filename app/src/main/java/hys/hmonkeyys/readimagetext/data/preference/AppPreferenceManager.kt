package hys.hmonkeyys.readimagetext.data.preference

import android.content.Context

class AppPreferenceManager(
    context: Context,
) {
    private val prefs by lazy { context.getSharedPreferences(APP_DEFAULT_KEY, Context.MODE_PRIVATE) }
    private val editor by lazy { prefs.edit() }

    /** @set String */
    fun setString(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    /** @return String */
    fun getString(key: String?): String? = prefs.getString(key, DEFAULT_VALUE_STRING)


    /** @set boolean */
    fun setBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** @return boolean */
    fun getBoolean(key: String?): Boolean = prefs.getBoolean(key, DEFAULT_VALUE_BOOLEAN)


    /** @set int */
    fun setInt(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    /** @return int */
    fun getInt(key: String?): Int = prefs.getInt(key, DEFAULT_VALUE_INT)


    /** @set long */
    fun setLong(key: String?, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    /** @return long */
    fun getLong(key: String?): Long = prefs.getLong(key, DEFAULT_VALUE_LONG)


    /** @set float */
    fun setFloat(key: String?, value: Float) {
        editor.putFloat(key, value)
        editor.apply()
    }

    /** @return float */
    fun getFloat(key: String?): Float = prefs.getFloat(key, DEFAULT_VALUE_FLOAT)


    /** 주소 Url 값 저장 */
    fun setUrl(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    /** 주소 Url 값 가져오기 */
    fun getUrl(key: String?): String? = prefs.getString(key, null)


    /** TTS 속도 저장 */
    fun setTTSSpeed(key: String?, value: Float) {
        editor.putFloat(key, value)
        editor.apply()
    }

    /** TTS 속도 가져오기 */
    fun getTTSSpeed(key: String?): Float = prefs.getFloat(key, DEFAULT_TTS_SPEED)


    companion object {
        const val APP_DEFAULT_KEY = "default_key" // Pref 이름

        private const val DEFAULT_VALUE_STRING = ""
        private const val DEFAULT_VALUE_BOOLEAN = false
        private const val DEFAULT_VALUE_INT = -1
        private const val DEFAULT_VALUE_LONG = -1L
        private const val DEFAULT_VALUE_FLOAT = -1f

        private const val DEFAULT_TTS_SPEED = 0.8f

        const val TTS_SPEED = "tts_speed"
        const val SETTING_URL = "setting_url"
        const val LAST_URL = "last_url"
    }
}