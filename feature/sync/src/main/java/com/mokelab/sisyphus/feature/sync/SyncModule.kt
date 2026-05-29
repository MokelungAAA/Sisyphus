package com.mokelab.sisyphus.feature.sync

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val syncModule = module {
    single { TokenStorage(androidContext()) }
    single { AuthManager(androidContext(), get()) }
    single { SyncService(get(), get()) }
    single { ConflictResolver() }
    single { SyncManager(androidContext(), get(), get(), get(), get()) }
    single { SyncLifecycleObserver(get(), get()) }
    viewModel { SyncViewModel(get(), get(), get(), get(), get()) }
}
