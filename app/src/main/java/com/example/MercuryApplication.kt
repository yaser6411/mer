package com.example

import android.app.Application
import com.example.data.local.MercuryDatabase
import com.example.data.preferences.LocalePreferencesManager
import com.example.data.repository.MercuryRepository
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

class MercuryApplication : Application() {

    lateinit var container: AppContainer
        private set

    val database: MercuryDatabase get() = container.database
    val repository: MercuryRepository get() = container.repository
    val localePreferencesManager: LocalePreferencesManager get() = container.localePreferencesManager

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // Pre-warm database
        database
    }
}

