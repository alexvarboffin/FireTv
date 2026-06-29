---
name: firetv-gradle-build
description: Build FireTv Android Gradle project and fix common JVM/Kotlin compile errors. Use when assembling the project, fixing gradle build failures, kapt JVM target mismatches, or SAM interface errors in FireTv.
---

# FireTv Gradle Build

## Build command

```powershell
cd C:\src\Synced\FireTv
$env:ORG_GRADLE_PROJECT_org_gradle_console='plain'
.\gradlew.bat assembleDebug --no-daemon
```

APK: `app/build/outputs/apk/debug/iptv-debug.apk`

## Known fixes

### JVM target mismatch (Java 17 vs Kotlin 21)

**Symptom:** `Inconsistent JVM-target compatibility detected for tasks 'compileDebugJavaWithJavac' (17) and 'kaptGenerateStubsDebugKotlin' (21)`

**Fix:** In every Android module with `compileOptions` Java 17, add:

```kotlin
kotlinOptions {
    jvmTarget = "17"
}
```

Modules already fixed: `:app`, `:xtream`. Check any new module.

### SAM interface lambda error

**Symptom:** `Interface 'X' does not have constructors` when passing lambda to custom listener.

**Fix:** Change to `fun interface X` (single abstract method).

### Modules in project

`:app`, `:data`, `:xtream`, `:simplesearchview`, `:mylibrary`, `:shared` (WalhallaUI), `:features:ui` (WalhallaUI)

## After fix

1. Re-run `assembleDebug`
2. Log problem/solution in `Documentation/TASKS/history/history.md`
3. Update `Documentation/MasterPrompt.md` §5 if new pattern
