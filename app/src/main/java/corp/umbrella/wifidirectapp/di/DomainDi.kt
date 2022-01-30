package corp.umbrella.wifidirectapp.di

import corp.umbrella.wifidirectapp.domain.usecases.DeleteNotesUseCase
import corp.umbrella.wifidirectapp.domain.usecases.GetNotesUseCase
import corp.umbrella.wifidirectapp.domain.usecases.SaveNoteUseCase
import org.koin.dsl.module

object DomainDi {

    val useCasesModule = module {
        factory {
            DeleteNotesUseCase(repository = get())
        }

        factory {
            GetNotesUseCase(repository = get())
        }

        factory {
            SaveNoteUseCase(repository = get())
        }
    }
}