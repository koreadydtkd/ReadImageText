package hys.hmonkeyys.readimagetext.data.repository.note

import hys.hmonkeyys.readimagetext.data.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override suspend fun getNotes(): MutableList<Note> = withContext(ioDispatcher) {
        noteDao.getAll()
    }

    override suspend fun insertNote(note: Note) = withContext(ioDispatcher) {
        noteDao.insertNote(note)
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        noteDao.deleteAll()
    }

    override suspend fun deleteItem(note: Note) = withContext(ioDispatcher) {
        noteDao.delete(note)
    }
}