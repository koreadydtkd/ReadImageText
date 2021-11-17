package hys.hmonkeyys.readimagetext.screen.views.main.note

import hys.hmonkeyys.readimagetext.data.db.entity.Note

sealed class NoteState {

    data class GetNoteData(
        val noteList: MutableList<Note>,
    ) : NoteState()

}