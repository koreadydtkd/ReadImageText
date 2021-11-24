package hys.hmonkeyys.readimagetext.screen.views.main.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.data.repository.note.NoteRepository
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NoteViewModel @Inject constructor(
    private val pref: AppPreferenceManager,
    private val noteRepository: NoteRepository,
) : BaseViewModel() {

    private var _noteStateLiveData = MutableLiveData<NoteState>()
    val noteStateData: LiveData<NoteState> = _noteStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        // 번역노트(Room DB) 모두 가져오기
        _noteStateLiveData.postValue(NoteState.GetNoteData(noteRepository.getNotes()))
    }

    /** 번역노트(Room DB) 한개 삭제 */
    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteItem(note)
    }

    /** 설정한 TTS 속도 가져오기 */
    fun getTTSSpeed(): Float = pref.getTTSSpeed(AppPreferenceManager.TTS_SPEED)

    companion object {
//        private const val TAG = "HYS_NoteViewModel"
    }
}