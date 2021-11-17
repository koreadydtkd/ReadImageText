package hys.hmonkeyys.readimagetext.data.repository.note

import hys.hmonkeyys.readimagetext.data.db.entity.Note

interface NoteRepository {

    suspend fun getNotes(): MutableList<Note>

    suspend fun insertNote(note: Note)

    suspend fun deleteAll()

    suspend fun deleteItem(note: Note)
}