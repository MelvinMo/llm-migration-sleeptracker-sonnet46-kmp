package com.sleeptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.presentation.ui.navigation.AppNavigation
import com.sleeptracker.presentation.ui.theme.SleepTrackerTheme
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import com.sleeptracker.presentation.viewmodel.SleepViewModel
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

// MIGRATION: React Native's `App.tsx` / `_layout.tsx` root → Android `MainActivity`.
// Koin DI and ResourceLoader initialization live in SleepTrackerApplication.onCreate(),
// which runs before this Activity is created, so ViewModels are safe to inject here.

class MainActivity : ComponentActivity() {

    // MIGRATION: Zustand stores at module scope → ViewModel instances via Koin delegate.
    // `by viewModel()` scopes the ViewModel to this Activity's ViewModelStore,
    // so the same instance is returned on configuration changes (screen rotations).
    private val authViewModel: AuthViewModel by viewModel()
    private val profileViewModel: UserProfileViewModel by viewModel()
    private val transparencyViewModel: TransparencyViewModel by viewModel()
    private val sleepViewModel: SleepViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val journalRepo: JournalRepository = get()

        setContent {
            SleepTrackerTheme {
                AppNavigation(
                    authViewModel         = authViewModel,
                    profileViewModel      = profileViewModel,
                    transparencyViewModel = transparencyViewModel,
                    sleepViewModel        = sleepViewModel,
                    journalRepository     = journalRepo
                )
            }
        }
    }
}
