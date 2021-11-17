package hys.hmonkeyys.readimagetext.screen.views.main.note

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.di.TTS
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.data.repository.note.NoteRepository
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import hys.hmonkeyys.readimagetext.utils.Constant.TTS_PITCH
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class NoteViewModel(
    private val appPreferenceManager: AppPreferenceManager,
    private val noteRepository: NoteRepository,
    private val tts: TTS,
) : BaseViewModel() {

    private var _noteStateLiveData = MutableLiveData<NoteState>()
    val noteStateData: LiveData<NoteState> = _noteStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        getAllNote()
    }

    /** 번역노트(Room DB) 모두 가져오기 */
    private fun getAllNote() = viewModelScope.launch {
        _noteStateLiveData.postValue(NoteState.GetNoteData(noteRepository.getNotes()))
    }

    /** 번역노트(Room DB) 한개 삭제 */
    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteItem(note)
    }

    /** 텍스트 읽기 */
    fun speakOut(extractedResults: String) {
        try {
            val ttsSpeed = appPreferenceManager.getTTSSpeed(AppPreferenceManager.TTS_SPEED)
            tts.textToSpeech.apply {
                setSpeechRate(ttsSpeed)
                setPitch(TTS_PITCH)
                speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /** tts 실행여부 */
    fun isSpeaking(): Boolean = tts.textToSpeech.isSpeaking

    /** tts 정지 */
    fun ttsStop() {
        if(isSpeaking()) {
            tts.textToSpeech.stop()
        }
    }

    companion object {
        private const val TAG = "HYS_NoteViewModel"
    }
}