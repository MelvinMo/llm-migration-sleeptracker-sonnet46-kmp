# Sleep Tracker — Claude Sonnet 4.6 → Kotlin Multiplatform (KMP) Migration

> Repo 2/7 from my M.Sc. thesis, *"Who Moved My Button?"* (McMaster, 2026) — Claude Sonnet 4.6's migration to Kotlin Multiplatform, the best-performing of the six. Other repos + baseline linked below.

Compose Multiplatform (Android) rewrite of the original React Native "Sleep Tracker" privacy-transparency app, produced by **Claude Sonnet 4.6** under a shared 15-rule migration prompt I wrote. It talks to the same Node.js/Express backend as the original app (see [llm-migration-sleeptracker-baseline](https://github.com/MelvinMo/llm-migration-sleeptracker-baseline)).

**UI fidelity target:** pixel-for-pixel match to the React Native source — layouts, text, font sizes, colors, padding, icons, and navigation flows were all checked against the original.

---

## Usability findings (from my thesis)

This migration was evaluated with Nielsen's ten usability heuristics across six standardized tasks by a single assessor (severity 0–4, lower is better). Full detail is in **Chapter 4** of my thesis (App 1).

| Metric | Value |
|---|---|
| Aggregate severity total | **14** |
| vs. baseline (React Native, total 16) | **−2 (improved)** |
| Rank among all 7 implementations | **1st (best)** |

This is the only one of the six migrations that scored *better* than the original app, driven by gains on H3 (User Control and Freedom) and H8 (Aesthetic and Minimalist Design) — the Material 3 time picker and native checkbox controls resolved some baseline ambiguity. See the thesis for the full per-heuristic breakdown and screenshots.

---

## Related repositories

| Repo | Description |
|---|---|
| [llm-migration-sleeptracker-baseline](https://github.com/MelvinMo/llm-migration-sleeptracker-baseline) | Original React Native app (unmodified snapshot) |
| **llm-migration-sleeptracker-sonnet46-kmp** | **This repo** — Claude Sonnet 4.6 → KMP |
| [llm-migration-sleeptracker-sonnet46-flutter](https://github.com/MelvinMo/llm-migration-sleeptracker-sonnet46-flutter) | Claude Sonnet 4.6 → Flutter |
| [llm-migration-sleeptracker-sonnet46-maui](https://github.com/MelvinMo/llm-migration-sleeptracker-sonnet46-maui) | Claude Sonnet 4.6 → .NET MAUI |
| [llm-migration-sleeptracker-gpt55-kmp](https://github.com/MelvinMo/llm-migration-sleeptracker-gpt55-kmp) | GPT-5.5 → KMP |
| [llm-migration-sleeptracker-gpt55-flutter](https://github.com/MelvinMo/llm-migration-sleeptracker-gpt55-flutter) | GPT-5.5 → Flutter |
| [llm-migration-sleeptracker-gpt55-maui](https://github.com/MelvinMo/llm-migration-sleeptracker-gpt55-maui) | GPT-5.5 → .NET MAUI |

---

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Android Studio | Hedgehog (2023.1) or later | https://developer.android.com/studio |
| JDK | **Android Studio's bundled JBR (OpenJDK 21)** — do not use a system JDK | — |
| Android SDK | API 35 (compile), API 26+ (run) | SDK Manager inside Android Studio |
| Kotlin plugin | 2.1+ (bundled with Android Studio) | — |

> Gradle handles everything — no separate SDK download needed beyond Android Studio.

---

## 1. Open the project

1. Launch Android Studio.
2. **File → Open** → select this repository's folder.
3. Wait for Gradle sync to finish (the first sync downloads ~500 MB of dependencies — this is normal).
4. If prompted about a missing JDK, point it to the JDK bundled with Android Studio (**File → Project Structure → SDK Location**).

---

## 2. Point the app at a backend

Copy the example environment file and edit it:

```bash
cp .env.example .env
```

The value isn't loaded automatically (Kotlin doesn't read `.env` files) — it's a reference for what to paste into [HttpApiClient.kt](composeApp/src/commonMain/kotlin/com/sleeptracker/network/HttpApiClient.kt), constant `DEFAULT_BACKEND_URL`:

| Target | Value to set |
|--------|---------------------------------------|
| Android emulator + local backend | `http://10.0.2.2:7000/api` |
| Physical device + local backend | `http://<your-computer's-LAN-IP>:7000/api` |
| Your own deployed backend | `https://<your-backend-host>/api` |

The checked-in default (`YOUR_LAN_IP`) is a placeholder — you must replace it with one of the values above before running the app.

`10.0.2.2` is the Android emulator's alias for the host machine's `localhost`. For a physical device, find your LAN IP with `ipconfig` (Windows) or `ifconfig` (Mac).

To run the backend locally, see the backend setup instructions in [llm-migration-sleeptracker-baseline](https://github.com/MelvinMo/llm-migration-sleeptracker-baseline).

---

## 3. Run on an Android emulator

### Option A — Android Studio (recommended)
1. **Device Manager → Create Device** → choose a Pixel profile → API 35 system image.
2. Click the green **Run** button (or `Shift+F10`).
3. Select the emulator when prompted.

### Option B — Command line
```bash
# Windows:
./gradlew.bat :composeApp:installDebug

# Mac / Linux:
./gradlew :composeApp:installDebug
```

---

## 4. Run on a physical Android device

Sensors (microphone, accelerometer, light sensor) only work on a real device.

### Step 1 — Enable Developer Options
1. **Settings → About Phone**
2. Tap **Build Number** 7 times until you see "You are now a developer"
3. **Settings → System → Developer Options → USB Debugging** → enable

### Step 2 — Connect the device

**Option A — USB cable**
1. Connect via USB and tap **Allow** on the device when prompted.
2. Verify:
```bash
adb devices
```
You should see your device listed. If it shows `unauthorized`, check the device screen for a new authorization dialog.

**Option B — Wireless (Android 11+)**
Both computer and device must be on the same Wi-Fi network.
1. On the device: **Developer Options → Wireless Debugging** → enable → **Pair device with pairing code** (note the IP, port, and code shown).
2. On the computer, pair once:
```bash
adb pair <ip>:<port>
# enter the 6-digit pairing code when prompted
```
3. Then connect (note: this uses a *different* port than pairing):
```bash
adb connect <ip>:<port>
```
4. Verify with `adb devices`.

### Step 3 — Update the backend URL for your device
Open [HttpApiClient.kt](composeApp/src/commonMain/kotlin/com/sleeptracker/network/HttpApiClient.kt) and set `DEFAULT_BACKEND_URL` to your machine's LAN IP, e.g.:
```kotlin
private const val DEFAULT_BACKEND_URL = "http://<your-lan-ip>:7000/api"
```

### Step 4 — Install and run
Via Android Studio: select the connected device from the dropdown and click **Run**.

Via command line (from this repo's folder):
```bash
# Windows PowerShell — forces Android Studio's bundled JDK 21:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew.bat :composeApp:installDebug
```

### Step 5 — Clear app data if reinstalling after an onboarding-related change
If you previously completed onboarding, old state can persist across reinstalls: **Device Settings → Apps → Sleep Tracker → Storage → Clear Data**.

### Step 6 — Grant permissions on first launch
Grant **Microphone**, **Body sensors / accelerometer**, and **Notifications** when prompted. If you denied one previously, re-enable it in **Device Settings → Apps → Sleep Tracker → Permissions**.

---

## 5. Release build (Android APK / AAB)

```bash
./gradlew.bat :composeApp:assembleRelease     # Signed APK
./gradlew.bat :composeApp:bundleRelease       # Android App Bundle (Play Store)
```

Output: `composeApp/build/outputs/apk/release/` or `composeApp/build/outputs/bundle/release/`.

---

## Privacy transparency UI

The app embeds a real-time privacy transparency layer visible on screens that collect sensor data:

- **Green icon** — sensor consented, local storage only (LOW risk)
- **Yellow icon** — sensor consented, cloud storage enabled (MEDIUM risk)
- **Red icon** — microphone + cloud storage both enabled (HIGH risk)

Tap any privacy icon to open the tooltip. Consent can be changed any time in **Profile → Consent Preferences**.

---

## Project structure

```
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/sleeptracker/
│       │   ├── constants/           # AppColors
│       │   ├── model/               # Shared data models
│       │   ├── network/             # Ktor HTTP client (DEFAULT_BACKEND_URL lives here)
│       │   ├── data/                # Repositories, local DB (SQLDelight)
│       │   ├── presentation/
│       │   │   ├── ui/navigation/   # AppNavigation.kt
│       │   │   ├── ui/screens/      # auth, onboarding, sleep, journal, statistics, profile
│       │   │   ├── ui/components/transparency/
│       │   │   └── viewmodel/
│       │   └── di/                  # Koin dependency injection
│       └── androidMain/             # Android-specific: MainActivity, sensors, WorkManager
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── .env.example                     # Backend URL reference values (see Section 2)
└── .gitignore
```

---

## Known limitations (from my thesis)

- Time entry and sleep-note selection use native Material 3 components rather than exact baseline replicas — this is the source of the H3/H8 *improvement* over baseline, but is a deliberate interaction change worth noting for anyone comparing pixel-for-pixel fidelity.
- See Chapter 4 of my thesis for the full task-by-task and heuristic-by-heuristic severity breakdown, including the two other Claude Sonnet 4.6 migrations (Flutter, MAUI).

---

## Environment variables & secrets

This repository contains **no real credentials**. `.env.example` holds placeholder/reference values only (a backend URL, not a secret). If you deploy your own backend, keep its real `.env` (JWT secret, database/Firebase keys, LLM API keys) out of version control — it's already covered by `.gitignore`.

---

## Citing my thesis

If you're referencing this repo, here's the full citation:

> Mokhtari, M. (2026). *"Who Moved My Button?": A Usability Evaluation of LLM-Assisted Cross-Platform Migration* [Master's thesis, McMaster University]. Department of Computing and Software. Supervisor: Richard F. Paige.

---

## License

MIT — see [LICENSE](LICENSE). Use it, fork it, build on it.
