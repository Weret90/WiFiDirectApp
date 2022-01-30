package corp.umbrella.wifidirectapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import corp.umbrella.wifidirectapp.domain.entity.Note
import corp.umbrella.wifidirectapp.domain.usecases.DeleteNotesUseCase
import corp.umbrella.wifidirectapp.domain.usecases.GetNotesUseCase
import corp.umbrella.wifidirectapp.domain.usecases.SaveNoteUseCase
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val getNotesUseCase: GetNotesUseCase,
) : ViewModel() {

    fun saveNote(note: Note) {
        viewModelScope.launch {
            saveNoteUseCase(note)
        }
    }

    fun deleteNotes() {
        viewModelScope.launch {
            deleteNotesUseCase()
        }
    }

    fun getNotes(): LiveData<List<Note>> {
        return getNotesUseCase()
    }
}