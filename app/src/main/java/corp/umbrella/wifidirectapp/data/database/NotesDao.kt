package corp.umbrella.wifidirectapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import corp.umbrella.wifidirectapp.domain.entity.Note

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes")
    fun getNotes(): LiveData<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteNotes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
}