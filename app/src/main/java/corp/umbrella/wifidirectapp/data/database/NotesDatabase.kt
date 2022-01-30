package corp.umbrella.wifidirectapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import corp.umbrella.wifidirectapp.domain.entity.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao
}