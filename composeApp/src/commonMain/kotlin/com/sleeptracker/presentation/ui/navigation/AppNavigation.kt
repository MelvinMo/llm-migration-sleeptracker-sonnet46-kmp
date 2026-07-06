package com.sleeptracker.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.sleeptracker.constants.AppColors
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.presentation.ui.screens.auth.LoginScreen
import com.sleeptracker.presentation.ui.screens.auth.RegisterScreen
import com.sleeptracker.presentation.ui.screens.journal.JournalScreen
import com.sleeptracker.presentation.ui.screens.onboarding.AccelerometerConsentScreen
import com.sleeptracker.presentation.ui.screens.onboarding.CloudStorageScreen
import com.sleeptracker.presentation.ui.screens.onboarding.JournalDataScreen
import com.sleeptracker.presentation.ui.screens.onboarding.LightSensorConsentScreen
import com.sleeptracker.presentation.ui.screens.onboarding.MicrophoneConsentScreen
import com.sleeptracker.presentation.ui.screens.onboarding.PrivacyPolicyAgreementScreen
import com.sleeptracker.presentation.ui.screens.onboarding.QuestionsExplanationScreen
import com.sleeptracker.presentation.ui.screens.onboarding.QuestionsScreen
import com.sleeptracker.presentation.ui.screens.onboarding.TransparencyOnboardingScreen
import com.sleeptracker.presentation.ui.screens.privacy.PrivacyPolicyScreen
import com.sleeptracker.presentation.ui.screens.profile.ConsentPreferencesScreen
import com.sleeptracker.presentation.ui.screens.profile.ProfileScreen
import com.sleeptracker.presentation.ui.screens.sleep.SleepModeScreen
import com.sleeptracker.presentation.ui.screens.sleep.SleepScreen
import com.sleeptracker.presentation.ui.screens.statistics.StatisticsScreen
import com.sleeptracker.presentation.viewmodel.AuthUiState
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import com.sleeptracker.presentation.viewmodel.SleepViewModel
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileUiState
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel
// MIGRATION: Expo Router (file-based routing with (auth)/, (onboarding)/, (tabs)/ groups)
// → Navigation Compose with NavHost + nested navigation graphs.
//
// Route mapping:
//   (auth)/index   → "auth/login"
//   (auth)/register → "auth/register"
//   (onboarding)/* → nested "onboarding" graph
//   (tabs)/sleep   → "main/sleep"
//   (tabs)/sleep/sleep-mode → "main/sleep_mode"
//   (tabs)/journal → "main/journal"
//   (tabs)/statistics → "main/statistics"
//   (tabs)/profile → "main/profile"
//   privacy-policy → "privacy_policy"
//
// Conditional routing (from _layout.tsx):
//   Not authenticated                           → auth graph
//   Authenticated + not privacy onboarded       → onboarding graph
//   Authenticated + not app onboarded           → onboarding/questions_explanation
//   Fully onboarded                             → main tabs

// ─── Route constants ──────────────────────────────────────────────────────────
object Routes {
    const val AUTH_GRAPH    = "auth"
    const val LOGIN         = "auth/login"
    const val REGISTER      = "auth/register"

    const val ONBOARDING_GRAPH                 = "onboarding"
    const val ONBOARDING_MICROPHONE            = "onboarding/microphone"
    const val ONBOARDING_ACCELEROMETER         = "onboarding/accelerometer"
    const val ONBOARDING_LIGHT                 = "onboarding/light"
    const val ONBOARDING_JOURNAL               = "onboarding/journal"
    const val ONBOARDING_CLOUD                 = "onboarding/cloud"
    const val ONBOARDING_PRIVACY_POLICY        = "onboarding/privacy_policy_agreement"
    const val ONBOARDING_TRANSPARENCY          = "onboarding/transparency"
    const val ONBOARDING_QUESTIONS_EXPLANATION = "onboarding/questions_explanation"  // Added
    const val ONBOARDING_QUESTIONS             = "onboarding/questions"

    const val MAIN_GRAPH    = "main"
    const val SLEEP         = "main/sleep"
    const val SLEEP_MODE    = "main/sleep_mode"
    const val JOURNAL       = "main/journal"
    const val STATISTICS    = "main/statistics"
    const val PROFILE       = "main/profile"
    const val CONSENT_PREFS = "main/consent_preferences"
    const val PRIVACY_POLICY = "privacy_policy"
}

// ─── Bottom nav items ─────────────────────────────────────────────────────────
// MIGRATION: Expo `<Tabs>` with tabBarIcon config → NavigationBar + NavigationBarItem
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.SLEEP,      "Sleep",      Icons.Outlined.DarkMode),
    BottomNavItem(Routes.JOURNAL,    "Journal",    Icons.Outlined.Description),
    BottomNavItem(Routes.STATISTICS, "Statistics", Icons.Outlined.BarChart),
    BottomNavItem(Routes.PROFILE,    "Profile",    Icons.Outlined.Person)
)

// Routes where the bottom nav bar should be hidden
// MIGRATION: TypeScript `(tabs)/_layout.tsx` used `usePathname()` to hide tab bar on sleep-mode
private val routesWithHiddenNav = setOf(Routes.SLEEP_MODE)

// ─── Root navigation ──────────────────────────────────────────────────────────
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    profileViewModel: UserProfileViewModel,
    transparencyViewModel: TransparencyViewModel,
    sleepViewModel: SleepViewModel,
    journalRepository: JournalRepository
) {
    val navController = rememberNavController()
    val authState    by authViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    // Extract only the navigation-relevant flags so toggling consent prefs doesn't re-trigger routing
    val privacyOnboarded = (profileState as? UserProfileUiState.Loaded)?.hasCompletedPrivacyOnboarding
    val appOnboarded     = (profileState as? UserProfileUiState.Loaded)?.hasCompletedAppOnboarding
    val profileLoaded    = profileState is UserProfileUiState.Loaded

    LaunchedEffect(authState, profileLoaded, privacyOnboarded, appOnboarded) {
        when (authState) {
            is AuthUiState.CheckingAuth -> Unit
            is AuthUiState.Unauthenticated -> {
                navController.navigate(Routes.AUTH_GRAPH) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthUiState.Error -> Unit
            is AuthUiState.Authenticated -> {
                if (!profileLoaded) return@LaunchedEffect
                when {
                    privacyOnboarded == false ->
                        navController.navigate(Routes.ONBOARDING_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                    appOnboarded == false ->
                        navController.navigate(Routes.ONBOARDING_QUESTIONS_EXPLANATION) {
                            popUpTo(0) { inclusive = true }
                        }
                    else ->
                        navController.navigate(Routes.MAIN_GRAPH) {
                            popUpTo(0) { inclusive = true }
                        }
                }
            }
            else -> Unit
        }
    }

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute     = currentBackStack?.destination?.route
    val showBottomBar    = currentRoute != null &&
        bottomNavItems.any { currentRoute.startsWith(it.route) } &&
        currentRoute !in routesWithHiddenNav

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (showBottomBar) {
                // MIGRATION: Expo `<Tabs>` → Material3 `NavigationBar`
                NavigationBar(containerColor = AppColors.LightBlack) {
                    bottomNavItems.forEach { item ->
                        val selected = currentBackStack?.destination?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    // MIGRATION: Expo Tabs uses single-top behavior by default
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon     = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label    = { Text(item.label, fontSize = 11.sp) },
                            colors   = NavigationBarItemDefaults.colors(
                                selectedIconColor   = AppColors.Accent,
                                selectedTextColor   = AppColors.Accent,
                                unselectedIconColor = AppColors.Grey,
                                unselectedTextColor = AppColors.Grey,
                                indicatorColor      = AppColors.Accent.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = Routes.AUTH_GRAPH,
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Auth graph ────────────────────────────────────────────────────
            navigation(startDestination = Routes.LOGIN, route = Routes.AUTH_GRAPH) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        authViewModel        = authViewModel,
                        onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                        onLoginSuccess       = { /* LaunchedEffect handles redirect */ }
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        authViewModel      = authViewModel,
                        onNavigateToLogin  = { navController.popBackStack() },
                        onRegisterSuccess  = { /* LaunchedEffect handles redirect */ }
                    )
                }
            }

            // ── Onboarding graph ──────────────────────────────────────────────
            // MIGRATION: Expo Router `(onboarding)/` file group → nested navigation graph.
            // Each screen updates UserConsentPreferences via profileViewModel.setUserConsentPreferences.
            navigation(startDestination = Routes.ONBOARDING_MICROPHONE, route = Routes.ONBOARDING_GRAPH) {

                // Screen 1 of 9: Microphone consent
                // MIGRATION: `(onboarding)/index.tsx` → ONBOARDING_MICROPHONE
                composable(Routes.ONBOARDING_MICROPHONE) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    val prefs = (profileState2 as? UserProfileUiState.Loaded)
                        ?.userConsentPreferences ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                    MicrophoneConsentScreen(
                        consentPreferences         = prefs,
                        onUpdateConsent            = { newPrefs ->
                            profileViewModel.setUserConsentPreferences(newPrefs)
                            transparencyViewModel.recalculateRisksForConsent(newPrefs)
                        },
                        onNavigateBack             = { navController.popBackStack() },
                        onNavigateNext             = { navController.navigate(Routes.ONBOARDING_ACCELEROMETER) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=$sectionId")
                        }
                    )
                }

                // Screen 2 of 9: Accelerometer consent
                // MIGRATION: `(onboarding)/accelerometer-consent.tsx` → ONBOARDING_ACCELEROMETER
                composable(Routes.ONBOARDING_ACCELEROMETER) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    val prefs = (profileState2 as? UserProfileUiState.Loaded)
                        ?.userConsentPreferences ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                    AccelerometerConsentScreen(
                        consentPreferences         = prefs,
                        onUpdateConsent            = { newPrefs ->
                            profileViewModel.setUserConsentPreferences(newPrefs)
                            transparencyViewModel.recalculateRisksForConsent(newPrefs)
                        },
                        onNavigateBack             = { navController.popBackStack() },
                        onNavigateNext             = { navController.navigate(Routes.ONBOARDING_LIGHT) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=$sectionId")
                        }
                    )
                }

                // Screen 3 of 9: Light sensor consent
                // MIGRATION: `(onboarding)/light-sensor-consent.tsx` → ONBOARDING_LIGHT
                composable(Routes.ONBOARDING_LIGHT) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    val prefs = (profileState2 as? UserProfileUiState.Loaded)
                        ?.userConsentPreferences ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                    LightSensorConsentScreen(
                        consentPreferences         = prefs,
                        onUpdateConsent            = { newPrefs ->
                            profileViewModel.setUserConsentPreferences(newPrefs)
                            transparencyViewModel.recalculateRisksForConsent(newPrefs)
                        },
                        onNavigateBack             = { navController.popBackStack() },
                        onNavigateNext             = { navController.navigate(Routes.ONBOARDING_JOURNAL) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=$sectionId")
                        }
                    )
                }

                // Screen 4 of 9: Journal data info
                // MIGRATION: `(onboarding)/journal-data.tsx` → ONBOARDING_JOURNAL
                composable(Routes.ONBOARDING_JOURNAL) {
                    JournalDataScreen(
                        onNavigateBack         = { navController.popBackStack() },
                        onNavigateNext         = { navController.navigate(Routes.ONBOARDING_CLOUD) },
                        onNavigateToPrivacyPolicy = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=$sectionId")
                        }
                    )
                }

                // Screen 5 of 9: Cloud storage consent
                // MIGRATION: `(onboarding)/cloud-storage.tsx` → ONBOARDING_CLOUD
                composable(Routes.ONBOARDING_CLOUD) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    val prefs = (profileState2 as? UserProfileUiState.Loaded)
                        ?.userConsentPreferences ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                    CloudStorageScreen(
                        consentPreferences         = prefs,
                        onUpdateConsent            = { newPrefs ->
                            profileViewModel.setUserConsentPreferences(newPrefs)
                            transparencyViewModel.recalculateRisksForConsent(newPrefs)
                        },
                        onNavigateBack             = { navController.popBackStack() },
                        onNavigateNext             = { navController.navigate(Routes.ONBOARDING_PRIVACY_POLICY) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=$sectionId")
                        }
                    )
                }

                // Screen 6 of 9: Privacy policy agreement
                // MIGRATION: `(onboarding)/privacy-policy-agreement.tsx` → ONBOARDING_PRIVACY_POLICY
                composable(Routes.ONBOARDING_PRIVACY_POLICY) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    val prefs = (profileState2 as? UserProfileUiState.Loaded)
                        ?.userConsentPreferences ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                    PrivacyPolicyAgreementScreen(
                        consentPreferences      = prefs,
                        onUpdateConsent         = { newPrefs ->
                            profileViewModel.setUserConsentPreferences(newPrefs)
                            transparencyViewModel.recalculateRisksForConsent(newPrefs)
                        },
                        onNavigateBack          = { navController.popBackStack() },
                        onNavigateNext          = { navController.navigate(Routes.ONBOARDING_TRANSPARENCY) },
                        onNavigateToPrivacyPolicy = {
                            navController.navigate(Routes.PRIVACY_POLICY)
                        }
                    )
                }

                // Screen 7 of 9: Transparency feature introduction
                // MIGRATION: `(onboarding)/transparency.tsx` → ONBOARDING_TRANSPARENCY
                // TypeScript set `hasCompletedPrivacyOnboarding=true` here before routing.
                // Compose: onNavigateNext sets the flag → LaunchedEffect re-fires → navigates to
                // ONBOARDING_QUESTIONS_EXPLANATION (when hasCompletedPrivacyOnboarding=true, hasCompletedAppOnboarding=false).
                composable(Routes.ONBOARDING_TRANSPARENCY) {
                    TransparencyOnboardingScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateNext = {
                            profileViewModel.setHasCompletedPrivacyOnboarding(true)
                            // LaunchedEffect will navigate to ONBOARDING_QUESTIONS_EXPLANATION
                        }
                    )
                }

                // Screen 8 of 9: Questions explanation
                // MIGRATION: `(onboarding)/questions-explanation.tsx` → ONBOARDING_QUESTIONS_EXPLANATION
                // Reachable both from ONBOARDING_TRANSPARENCY (inline) and from LaunchedEffect
                // when hasCompletedPrivacyOnboarding=true and hasCompletedAppOnboarding=false.
                composable(Routes.ONBOARDING_QUESTIONS_EXPLANATION) {
                    QuestionsExplanationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateNext = { navController.navigate(Routes.ONBOARDING_QUESTIONS) }
                    )
                }

                // Screen 9 of 9: Sleep duration question
                // MIGRATION: `(onboarding)/questions.tsx` → ONBOARDING_QUESTIONS
                composable(Routes.ONBOARDING_QUESTIONS) {
                    val profileState2 by profileViewModel.uiState.collectAsState()
                    QuestionsScreen(
                        userId              = authViewModel.currentUser?.userId,
                        onNavigateBack      = { navController.popBackStack() },
                        onSaveGeneralSleepData = { data ->
                            sleepViewModel.saveGeneralSleepData(data)
                        },
                        onComplete          = {
                            val currentPrefs = (profileState2 as? UserProfileUiState.Loaded)
                                ?.userConsentPreferences
                                ?: com.sleeptracker.model.UserConsentPreferences.DEFAULT
                            transparencyViewModel.recalculateRisksForConsent(currentPrefs)
                            profileViewModel.setHasCompletedAppOnboarding(true)
                            // LaunchedEffect will navigate to MAIN_GRAPH
                        }
                    )
                }
            }

            // ── Main tabs graph ───────────────────────────────────────────────
            navigation(startDestination = Routes.SLEEP, route = Routes.MAIN_GRAPH) {
                composable(Routes.SLEEP) {
                    SleepScreen(
                        sleepViewModel             = sleepViewModel,
                        transparencyViewModel      = transparencyViewModel,
                        onNavigateToSleepMode      = { navController.navigate(Routes.SLEEP_MODE) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=${sectionId ?: ""}")
                        },
                        onNavigateToConsentPreferences = { navController.navigate(Routes.CONSENT_PREFS) }
                    )
                }

                // MIGRATION: sleep-mode hides the bottom nav — handled by showBottomBar condition above
                composable(Routes.SLEEP_MODE) {
                    SleepModeScreen(
                        transparencyViewModel      = transparencyViewModel,
                        onNavigateBack             = {
                            // MIGRATION: `router.replace('/(tabs)/sleep')` → popBackStack to sleep
                            navController.navigate(Routes.SLEEP) {
                                popUpTo(Routes.SLEEP) { inclusive = true }
                            }
                        },
                        onNavigateToStatistics     = { navController.navigate(Routes.STATISTICS) },
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=${sectionId ?: ""}")
                        },
                        onNavigateToConsentPreferences = { navController.navigate(Routes.CONSENT_PREFS) }
                    )
                }

                composable(Routes.JOURNAL) {
                    JournalScreen(
                        journalRepository          = journalRepository,
                        transparencyViewModel      = transparencyViewModel,
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=${sectionId ?: ""}")
                        },
                        onNavigateToConsentPreferences = { navController.navigate(Routes.CONSENT_PREFS) }
                    )
                }

                composable(Routes.STATISTICS) {
                    StatisticsScreen(
                        journalRepository          = journalRepository,
                        authViewModel              = authViewModel,
                        transparencyViewModel      = transparencyViewModel,
                        onNavigateToPrivacyPolicy  = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=${sectionId ?: ""}")
                        }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        authViewModel                  = authViewModel,
                        profileViewModel               = profileViewModel,
                        onNavigateToPrivacyPolicy      = { navController.navigate(Routes.PRIVACY_POLICY) },
                        onNavigateToConsentPreferences = { navController.navigate(Routes.CONSENT_PREFS) }
                    )
                }

                composable(Routes.CONSENT_PREFS) {
                    ConsentPreferencesScreen(
                        profileViewModel          = profileViewModel,
                        transparencyViewModel     = transparencyViewModel,
                        onNavigateBack            = { navController.popBackStack() },
                        onNavigateToPrivacyPolicy = { sectionId ->
                            navController.navigate("${Routes.PRIVACY_POLICY}?sectionId=${sectionId ?: ""}")
                        }
                    )
                }
            }

            // ── Privacy policy (modal-style, no bottom nav) ───────────────────
            // MIGRATION: TypeScript `privacy-policy.tsx` with `sectionId` param → navArgs
            composable("${Routes.PRIVACY_POLICY}?sectionId={sectionId}") { backStack ->
                val sectionId = backStack.arguments?.getString("sectionId")
                PrivacyPolicyScreen(
                    sectionId      = sectionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PRIVACY_POLICY) {
                PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

