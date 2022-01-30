package corp.umbrella.wifidirectapp.di

import androidx.room.Room
import corp.umbrella.wifidirectapp.data.NotesRepositoryImpl
import corp.umbrella.wifidirectapp.data.database.NotesDatabase
import corp.umbrella.wifidirectapp.domain.NotesRepository
import org.koin.dsl.module

object DataDi {

    private const val DB_NAME = "notes.db"

    val roomModule = module {
        single {
            Room.databaseBuilder(get(), NotesDatabase::class.java, DB_NAME)
                .build()
                .notesDao()
        }
    }

    val repositoryModule = module {
        single<NotesRepository> {
            NotesRepositoryImpl(dao = get())
        }
    }
}