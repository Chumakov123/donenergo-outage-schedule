package com.chumakov123.outageschedule.core

import android.app.Application
import com.chumakov123.outageschedule.di.appModule
import com.chumakov123.outageschedule.di.dataModule
import com.chumakov123.outageschedule.di.domainModule
import com.chumakov123.outageschedule.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)

            modules(
                appModule,
                dataModule,
                domainModule,
                presentationModule
            )
        }
    }
}