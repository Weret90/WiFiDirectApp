package corp.umbrella.wifidirectapp.di

import corp.umbrella.wifidirectapp.presentation.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object PresentationDi {

    val viewModelModule = module {
        viewModel {
            MainActivityViewModel(
                saveNoteUseCase = get(),
                deleteNotesUseCase = get(),
                getNotesUseCase = get()
            )
        }
    }
}