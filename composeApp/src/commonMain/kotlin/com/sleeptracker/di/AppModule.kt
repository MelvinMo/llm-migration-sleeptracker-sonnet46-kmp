package com.sleeptracker.di

import com.sleeptracker.data.datasource.CloudJournalDataSource
import com.sleeptracker.data.datasource.LocalJournalDataSource
import com.sleeptracker.data.local.LocalDatabaseManager
import com.sleeptracker.data.repository.JournalDataSource
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.data.repository.JournalRepositoryImpl
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.network.HttpApiClient
import com.sleeptracker.network.TransparencyApiService
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import com.sleeptracker.presentation.viewmodel.SleepViewModel
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel
import com.sleeptracker.resources.ResourceLoader
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// MIGRATION: Replaces manual DI wiring in MainActivity.onCreate().
// Services/index.ts singletons (`export const journalRepository = new JournalRepositoryImpl()`)
// → Koin `single` / `viewModel` definitions.
//
// Circular dependency solution:
// HttpApiClient needs a token provider → AuthViewModelHolder holds a weak ref to AuthViewModel.
// AuthViewModel is defined after HttpApiClient and registers itself with AuthViewModelHolder.
// This matches the original TS pattern where httpClient captured authStore.getState().token lazily.

// Holds a reference to AuthViewModel so HttpApiClient can lazily retrieve the current token.
// Populated when AuthViewModel is first created by Koin.
class AuthViewModelHolder {
    var viewModel: AuthViewModel? = null
    val token: String? get() = viewModel?.currentToken
}

// ── Network module ────────────────────────────────────────────────────────────
// MIGRATION: `HttpApiClient` construction with lazy token access from AuthViewModel.
val networkModule = module {
    single { AuthViewModelHolder() }

    // MIGRATION: `new HttpApiClient(() => authStore.getState().authToken)`
    // → `HttpApiClient(getToken = { get<AuthViewModelHolder>().token })`
    single {
        HttpApiClient(getToken = { get<AuthViewModelHolder>().token })
    }

    // MIGRATION: TransparencyService → TransparencyApiService.
    // privacyPolicyJson / pipedaRegulationsJson are loaded from composeResources by ResourceLoader
    // before Koin starts, so they're accessible synchronously here.
    single {
        TransparencyApiService(
            httpClient                = get(),
            secureStorage             = get(),
            getUserConsentPreferences = {
                get<UserProfileViewModel>().consentPreferences
            },
            privacyPolicyJson         = ResourceLoader.privacyPolicyJson,
            pipedaRegulationsJson     = ResourceLoader.pipedaRegulationsJson
        )
    }
}

// ── Data module ───────────────────────────────────────────────────────────────
// MIGRATION: `LocalDatabaseManager.getInstance()` singleton → Koin `single`
val dataModule = module {
    single { LocalDatabaseManager.getInstance() }

    single<JournalDataSource>(named("local")) { LocalJournalDataSource(get()) }

    // MIGRATION: CloudJournalDataSource needs a token lambda for Bearer auth header
    single<JournalDataSource>(named("cloud")) {
        CloudJournalDataSource(get()) {
            get<AuthViewModelHolder>().token
        }
    }

    // MIGRATION: JournalRepositoryImpl wires cloud + local datasources with encryption
    single<JournalRepository> {
        JournalRepositoryImpl(
            cloudDataSource       = get(named("cloud")),
            localDataSource       = get(named("local")),
            encryptionService     = get(),
            userProfileViewModel  = get(),
            getUserId             = { get<AuthViewModelHolder>().viewModel?.currentUser?.userId },
            transparencyViewModel = get()
        )
    }
}

// ── ViewModel module ──────────────────────────────────────────────────────────
// MIGRATION: Zustand stores → Lifecycle-scoped ViewModels via Koin `viewModel`.
// `viewModel { }` integrates with Android ViewModel lifecycle (Activity/Fragment scope).
// Compose equivalent: `koinViewModel()` in composables, or `get()` in AppNavigation-level code.
val viewModelModule = module {
    // MIGRATION: `useTransparencyStore` → TransparencyViewModel
    viewModel { TransparencyViewModel(get()) }

    // MIGRATION: `useAuthStore` → AuthViewModel.
    // After construction, registers itself with AuthViewModelHolder for token access.
    viewModel {
        AuthViewModel(get(), get(), get()).also { vm ->
            get<AuthViewModelHolder>().viewModel = vm
        }
    }

    // MIGRATION: `useProfileStore` → UserProfileViewModel
    viewModel { UserProfileViewModel(get()) }

    // MIGRATION: Sleep screen business logic → SleepViewModel
    viewModel { SleepViewModel(get(), get(), get()) }
}
