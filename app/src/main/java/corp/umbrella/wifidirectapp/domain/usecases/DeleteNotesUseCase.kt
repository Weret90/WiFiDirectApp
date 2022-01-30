package corp.umbrella.wifidirectapp.domain.usecases

import corp.umbrella.wifidirectapp.domain.NotesRepository

class DeleteNotesUseCase(private val repository: NotesRepository) {

    suspend operator fun invoke() {
        repository.deleteNotes()
    }
}