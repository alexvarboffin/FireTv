# История задач (problem → solution)

Формат: дата | проблема | решение | файлы

---

## 2026-06-18 — Сборка legacy `:app`

| # | Проблема | Решение |
|---|----------|---------|
| 1 | `assembleDebug` падает: `:xtream` JVM Java 17 vs Kotlin 21 | `kotlinOptions { jvmTarget = "17" }` в `xtream/build.gradle.kts` |
| 2 | `:app:compileDebugKotlin` — та же JVM ошибка | `kotlinOptions { jvmTarget = "17" }` в `app/build.gradle.kts` |
| 3 | `CategoryFragment.kt:63` — Interface does not have constructors | `fun interface OnCategoryItemClickListener` в `CategoryAdapter.kt` |
| 4 | Огромный Gradle log | `ORG_GRADLE_PROJECT_org_gradle_console=plain` |

**Результат:** `BUILD SUCCESSFUL`, APK `app/build/outputs/apk/debug/iptv-debug.apk`

---

## 2026-06-18 — Архитектура 2-в-1 (уточнение пользователя)

| Было | Стало |
|------|-------|
| `FireTvRoot` + `when(formFactor)` в одном Composable | `LauncherActivity` → `phone/MainActivity` или `tv/MainActivity` |
| Gradle-модули compose-mobile/phone/common | Пакеты `ui/mobile/*`, `ui/tv/*` в `app-compose` |
| CompositionLocal wrappers | Зеркальные файлы: `ui/mobile/PrettyButton`, `ui/tv/PrettyButton` |

**Суть:** universal APK, два независимых UI-дерева; общий только `:core` (ViewModels).

## 2026-06-18 — Screen / ScreenBody

| Правило | |
|---------|--|
| `*Screen()` | Без параметров; MOCK в `mockScreens` для Preview |
| `*ScreenBody()` | Динамика + callbacks; статика — `stringResource` внутри |

Файл: `guidelines/SCREEN_SCREENBODY.md`

---

| # | Запрос пользователя | Действие |
|---|---------------------|----------|
| 1 | MP + роадмап портирования app → Compose Mobile/Phone | Создан `MasterPrompt.md`, `Roadmap.md`, `ARCHITECTURE/compose-architecture.md` |
| 2 | Референс cinemakmp | Clone в `Documentation/reference/cinemakmp` — **только README**, код отсутствует |
| 3 | Фокус Mobile, acceptance Phone = 100% legacy visual | Зафиксировано в MP §1 и Roadmap §6 |
| 4 | Skills для агентов | `.cursor/skills/firetv-gradle-build`, `firetv-compose-port` |

---

## 2026-06-18 — Фаза 1: общий CategoryRepository

| Задача | Статус |
|--------|--------|
| `CategoryRepository` interface в `:core:domain` | done |
| `LocalCategoryRepository` + `CategoryMapper` в `:core:data-bridge` | done |
| `CategoryViewModel` → repository | done |
| Legacy `CategoryPresenter` → тот же repository | done |
| `:app` + `:app-compose` assembleDebug | BUILD SUCCESSFUL |

**Уточнение:** БД, парсеры, playlist repos — общий слой для legacy и compose; выносить из legacy в `:core` / `:data`.

---

## 2026-06-18 — Фаза 0: скелет app-compose

| Задача | Статус |
|--------|--------|
| `kotlin-compose` plugin, `tv-material` в catalog | done |
| `:core:domain`, `:core:presentation`, `:core:data-bridge` | done |
| `:app-compose` Launcher → phone/tv MainActivity | done |
| `ui/mobile` ↔ `ui/tv` PrettyButton, CategoryScreen | done |
| `mockScreens/CategoryScreen` + Preview | done |
| `:app-compose:assembleDebug` | BUILD SUCCESSFUL |

APK: `app-compose/build/outputs/apk/debug/iptv-compose-debug.apk`  
applicationId: `tv.hdonlinetv.besttvchannels.movies.watchfree.compose`

---

```markdown
## YYYY-MM-DD — Краткое название

| # | Проблема | Решение |
|---|----------|---------|
| 1 | ... | ... |
```

Агент **обязан** дописывать строку при исправлении ошибки или смене понимания ТЗ.
