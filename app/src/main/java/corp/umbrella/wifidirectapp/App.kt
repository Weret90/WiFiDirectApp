package corp.umbrella.wifidirectapp

import android.app.Application
import corp.umbrella.wifidirectapp.di.DataDi
import corp.umbrella.wifidirectapp.di.DomainDi
import corp.umbrella.wifidirectapp.di.PresentationDi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                DataDi.repositoryModule,
                DataDi.roomModule,
                DomainDi.useCasesModule,
                PresentationDi.viewModelModule
            )
        }
    }
}