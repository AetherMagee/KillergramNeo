# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KillergramNeo is an **Xposed module** that enhances Telegram (and its forks) without modifying the original APK. It hooks into Telegram's internal classes at runtime via the LSPosed framework and provides a Jetpack Compose settings UI for toggling features.

## Build Commands

```bash
./gradlew :app:assembleDebug       # Debug build
./gradlew :app:assembleRelease     # Signed release (needs signing config)
./gradlew :app:testDebugUnitTest   # Unit tests
./gradlew :app:connectedDebugAndroidTest  # Instrumentation tests (needs device/emulator)
```

**Requirements:** JDK 17, Android SDK (compileSdk 35, minSdk 27, targetSdk 34)

## Architecture

### Two Runtime Contexts

The module runs in **two separate processes**:

1. **Settings app** (`MainActivity`) — standalone Compose UI where users toggle features. Writes to world-readable `SharedPreferences`.
2. **Xposed hooks** (`MainHook`) — injected into the target Telegram process by LSPosed. Reads preferences via `XSharedPreferences` and applies hooks conditionally.

Preferences bridge the two processes: the UI writes them, the hooks read them at injection time.

### Package Layout (`app/src/main/java/aether/killergram/neo/`)

- **`MainHook.kt`** — Entry point for Xposed. Implements `IXposedHookLoadPackage`, `IXposedHookZygoteInit`, `IXposedHookInitPackageResources`. Reads prefs and conditionally invokes each hook.
- **`hooks/`** — Each feature is a separate file defining an **extension function on `Hooks`** (e.g., `fun Hooks.killStories()`). The `Hooks` class wraps `LoadPackageParam` and provides `loadClass()` for safe class resolution.
- **`hooks/Hooks.kt`** — Thin wrapper around `LoadPackageParam` with logged class loading.
- **`ui/`** — Jetpack Compose UI: screens, components, theme, navigation. Uses Material3 with bottom nav (Features / Settings tabs).
- **`ui/model/ToggleCatalog.kt`** — Central registry of all feature toggles grouped into sections (Visuals, Premium, Misc, Testing).
- **`data/`** — `ModulePrefsStore` (SharedPreferences wrapper), `RootActions` (root detection/force-stop), `RestartTargetResolver` (detects installed Telegram variants), `TargetApps` (supported package list).
- **`core/PreferenceKeys.kt`** — All toggle keys as `SCREAMING_SNAKE_CASE` constants.

### Adding a New Hook

1. Create a new file in `hooks/` with an extension function: `fun Hooks.myNewHook() { ... }`
2. Add a preference key constant in `PreferenceKeys.kt`.
3. Register the hook in `MainHook.kt`'s `hooksMap` (maps pref key → hook invocation).
4. Add a `ModuleToggle` entry in `ToggleCatalog.kt` under the appropriate section.

### Resource Replacement (Solar Icons)

Icon injection works differently from code hooks — it uses `IXposedHookInitPackageResources` in `MainHook`. Drawable resources with a `_solar` suffix in the module replace matching drawables (minus the suffix) in the target app.

## Key Technical Details

- Xposed API 82 is a **compileOnly** dependency — it's provided by the framework at runtime.
- ProGuard keeps all classes under `aether.killergram.neo.**` (no obfuscation for the module).
- The module maintains a `packageBlocklist` to avoid hooking into itself or GMS.
- `Utils.kt` provides a `log()` helper that wraps `XposedBridge.log()` with level-based filtering (DEBUG output controlled by the debug logging preference).
- Version is in `app/build.gradle.kts` (`versionName` / `versionCode`).

## CI

- Every push/PR triggers `assembleDebug`.
- Pushing a `v*` tag builds a signed release and publishes a GitHub Release with the APK.
