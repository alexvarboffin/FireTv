---
name: firetv-compose-port
description: Port FireTv to universal Compose app (2-in-1): LauncherActivity routes to phone/MainActivity or tv/MainActivity; mirror ui/mobile and ui/tv packages. Use for FireTv Compose migration, form-factor setup, or creating PrettyButton-style mirrored components.
---

# FireTv Compose Migration

## Read first

1. `Documentation/MasterPrompt.md` §2
2. `Documentation/Roadmap.md`

## Architecture (mandatory)

❌ **Never:** single `setContent { when(isTv) { ... } }`  
✅ **Always:** `LauncherActivity` → `phone/MainActivity` OR `tv/MainActivity`

```
ui/mobile/PrettyButton.kt   → material3
ui/tv/PrettyButton.kt      → tv.material3 (mirror)
```

Shared: `:core:presentation` ViewModels only.

## Screen / ScreenBody

Read `Documentation/guidelines/SCREEN_SCREENBODY.md`.

- `*Screen()` — **no parameters**; wires VM → `*ScreenBody()`
- `*ScreenBody(dynamic, callbacks)` — static strings/icons via `stringResource` inside
- `mockScreens/` package — MOCK data + `@Preview`

## Launcher pattern

```kotlin
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTv = (resources.configuration.uiMode and UI_MODE_TYPE_MASK) == UI_MODE_TYPE_TELEVISION
        startActivity(Intent(this, if (isTv) tv.MainActivity::class.java else phone.MainActivity::class.java))
        finish()
    }
}
```

## Focus order

1. `ui/mobile` + `phone/MainActivity` (matches legacy `:app` visual)
2. Mirror to `ui/tv` + `tv/MainActivity`

## ViewModel rules

See `Documentation/guidelines/00_guidelines.md` — StateFlow, sealed UiError, no Context in VM.

## After changes

Update `TASKS/history/history.md` if architecture clarifications from user.
