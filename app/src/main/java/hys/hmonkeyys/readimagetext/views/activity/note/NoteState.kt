package hys.hmonkeyys.readimagetext.views.activity.note

import hys.hmonkeyys.readimagetext.db.entity.Note

sealed class NoteState {
    object Initialized : NoteState()

    data class GetNoteData(
        val noteList: MutableList<Note>,
    ) : NoteState()
}