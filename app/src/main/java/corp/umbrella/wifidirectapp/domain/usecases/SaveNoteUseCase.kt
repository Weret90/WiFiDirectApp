package corp.umbrella.wifidirectapp.domain.usecases

import corp.umbrella.wifidirectapp.domain.NotesRepository
import corp.umbrella.wifidirectapp.domain.entity.Note

class SaveNoteUseCase(private val repository: NotesRepository) {

    suspend operator fun invoke(note: Note) {
        repository.saveNote(note)
    }
}