package com.example.di

import android.content.Context
import com.example.data.local.MercuryDatabase
import com.example.data.preferences.LocalePreferencesManager
import com.example.data.repository.MercuryRepository
import com.example.domain.usecase.GetDashboardSummaryUseCase
import com.example.domain.usecase.ProcessPurchaseUseCase
import com.example.domain.usecase.ProcessSaleUseCase

/**
 * Dependency Injection container providing application-scoped singletons.
 * Enables clean inversion of control, testing doubles, and modular instantiation.
 */
interface AppContainer {
    val database: MercuryDatabase
    val repository: MercuryRepository
    val localePreferencesManager: LocalePreferencesManager
    val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
    val processSaleUseCase: ProcessSaleUseCase
    val processPurchaseUseCase: ProcessPurchaseUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: MercuryDatabase by lazy {
        MercuryDatabase.getDatabase(context)
    }

    override val repository: MercuryRepository by lazy {
        MercuryRepository(database)
    }

    override val localePreferencesManager: LocalePreferencesManager by lazy {
        LocalePreferencesManager(context)
    }

    override val getDashboardSummaryUseCase: GetDashboardSummaryUseCase by lazy {
        GetDashboardSummaryUseCase(repository)
    }

    override val processSaleUseCase: ProcessSaleUseCase by lazy {
        ProcessSaleUseCase(repository)
    }

    override val processPurchaseUseCase: ProcessPurchaseUseCase by lazy {
        ProcessPurchaseUseCase(repository)
    }
}

