package hys.hmonkeyys.readimagetext.views.activity.note

import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.di.TTS
import hys.hmonkeyys.readimagetext.model.entity.Note
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.views.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class NoteViewModel(
    private val sharedPreferences: SharedPreferences,
    private val noteDao: NoteDao,
    private val tts: TTS,
) : BaseViewModel() {

    private var _noteStateLiveData = MutableLiveData<NoteState>()
    val noteStateData: LiveData<NoteState> = _noteStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _noteStateLiveData.postValue(NoteState.Initialized)
    }

    fun getAllNote() = viewModelScope.launch {
        _noteStateLiveData.postValue(NoteState.GetNoteData(noteDao.getAll()))
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteDao.delete(note)
    }

    // 텍스트 읽기
    fun speakOut(extractedResults: String) {
        try {
            tts.textToSpeech.apply {
                setSpeechRate(sharedPreferences.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_SPEECH_RATE))
                setPitch(TTS_PITCH)
                speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    // 텍스트 읽기 실행중인지
    fun isSpeaking(): Boolean = tts.textToSpeech.isSpeaking

    fun ttsStop() {
        if(isSpeaking()) {
            tts.textToSpeech.stop()
        }
    }

    companion object {
        private const val TAG = "HYS_NoteViewModel"

        private const val TTS_PITCH = 1.0F
        private const val TTS_SPEECH_RATE = 0.8F
    }
}