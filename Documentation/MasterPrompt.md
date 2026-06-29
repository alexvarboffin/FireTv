# MasterPrompt (MP) — FireTv → Jetpack Compose

> **Роль агента:** техлид команды. Любой ИИ-программист должен по MP повторить работу без устных пояснений.
> **Папка документации:** `Documentation/`
> **Проект:** `C:\src\Synced\FireTv` — IPTV (M3U + Xtream), legacy UI = XML + ViewBinding + MVP.

---

## 0. Правила ведения MP

1. Если уточнение пользователя **противоречит** MP или первоначальному пониманию — **обновить MP** (раздел «История» + при необходимости Roadmap).
2. Если агент **ошибся**, но нашёл решение — записать **проблема → решение** в `TASKS/history/history.md` (кратко).
3. MP держать **компактным**; детали — в `Roadmap.md`, `ARCHITECTURE/`, `guidelines/`.
4. Skills: `.cursor/skills/` — читать перед сборкой и Compose-работой.

---

## 1. Цель проекта

Портировать `:app` в **универсальное Compose-приложение (2 в 1)**: один APK, два независимых UI-дерева.

| Ветка UI | Пакет / Activity | Библиотека |
|----------|------------------|------------|
| **Телефон** | `phone/MainActivity`, `ui/mobile/*` | `androidx.compose.material3` |
| **TV** | `tv/MainActivity`, `ui/tv/*` | `androidx.tv.material3` |

**Референс:** https://gitlab.lds.net.ru/reznichenko.i/cinemakmp.git — ветка **`notKmp`** (не `main`! `main` пустой). Clone: `git clone -b notKmp --depth 1 …/cinemakmp.git Documentation/reference/cinemakmp-notKmp`

**Фокус фазы 1:** `ui/mobile` + `phone/MainActivity`.  
**Acceptance:** `phone/*` визуально **100%** = legacy `:app` (Roadmap §6).

---

## 2. Целевая архитектура

### 2.1 Принцип: не смешивать UI в одном Composable

❌ **Запрещено:** один `MainActivity` + `when (isTv) { ... }` внутри `setContent`.  
✅ **Делаем:** детект при старте → запуск **отдельной** Activity; дальше у каждой ветки свой NavHost и свои composable.

### 2.2 Структура пакетов (зеркало mobile ↔ tv)

```
:app-compose/   (или новый :app после cutover)
├── launcher/
│   └── LauncherActivity.kt      # LAUNCHER: детект → startActivity → finish
├── phone/
│   └── MainActivity.kt          # setContent { PhoneTheme { PhoneNavHost() } }
├── tv/
│   └── MainActivity.kt          # setContent { TvTheme { TvNavHost() } }
└── ui/
    ├── mobile/
    │   ├── PrettyButton.kt      # material3
    │   ├── category/
    │   │   └── CategoryScreen.kt
    │   └── ...
    └── tv/
        ├── PrettyButton.kt      # tv.material3 — тот же контракт, другой import
        ├── category/
        │   └── CategoryScreen.kt
        └── ...

:core:presentation/              # ViewModels, UiState — общие для обеих веток
:core:domain/
:core:data-bridge/               # :data, :xtream
```

**Зеркало:** для каждого экрана/виджета в `ui/mobile/foo/X.kt` — пара `ui/tv/foo/X.kt`.  
Имена и сигнатуры совпадают; различаются только Material-импорты и TV-focus.

### 2.3 Старт приложения (2 в 1)

```kotlin
// launcher/LauncherActivity.kt — единственная точка с детектом
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTv = (resources.configuration.uiMode and UI_MODE_TYPE_MASK) == UI_MODE_TYPE_TELEVISION
        val target = if (isTv) tv.MainActivity::class.java else phone.MainActivity::class.java
        startActivity(Intent(this, target))
        finish()
    }
}
```

Дальше `phone/MainActivity` и `tv/MainActivity` **не знают** друг о друге.

### 2.4 Gradle-модули (минимум)

```
:core:presentation, :core:domain, :core:data-bridge
:app-compose                     # phone/, tv/, ui/mobile/, ui/tv/, launcher/
```

Отдельные `:compose-mobile` / `:compose-ui-common` **не создаём** — всё в понятной структуре пакетов внутри app.

**Legacy до cutover:** `:app`, `:data`, `:xtream`, WalhallaUI.

### 2.5 Общий код (legacy + compose)

**Принцип (уточнение пользователя):** слой БД (`:data`), парсеры (`:xtream`, M3U), репозитории плейлистов — **общие для всех** app-модулей. Из legacy выносить в `:core:data-bridge` / `:data`; при необходимости рефакторить для удобства обоих клиентов.

| Слой | Модуль | Кто использует |
|------|--------|----------------|
| Room / DAO / парсеры | `:data`, `:xtream` | data-bridge |
| Repositories (interface) | `:core:domain` | ViewModel, legacy Presenter |
| Repository impl, mappers | `:core:data-bridge` | `:app`, `:app-compose` |
| ViewModels | `:core:presentation` | `:app-compose` |
| Legacy Presenters | `:app` | делегаты → repository (постепенно убирать) |

Сейчас: `CategoryRepository` / `LocalCategoryRepository`. Далее: `ChannelRepository`, `PlaylistRepository`.

---

## 3. UI-конвенции (обязательно)

Полные правила Screen/ScreenBody: **[guidelines/SCREEN_SCREENBODY.md](guidelines/SCREEN_SCREENBODY.md)**

### Screen / ScreenBody (кратко)

| Composable | Параметры | Роль |
|------------|-----------|------|
| `*Screen()` | **нет** | Контейнер: `viewModel()` + вызов `*ScreenBody()` |
| `*ScreenBody()` | динамика + callbacks | Чистый UI, без ViewModel |

- **В ScreenBody передаём:** списки, состояния из БД/API, `isLoading`, callbacks.
- **Внутри ScreenBody:** статический текст (`stringResource`), плейсхолдеры, подписи кнопок, статические иконки — из ресурсов legacy `:app`.
- **Preview / mock:** пакет `mockScreens` в `app-compose` — MOCK-данные в `*Screen()`, `@Preview` на нём.

```kotlin
@Composable
fun HyperScreen() {
    val mockItems = listOf(/* demo */)
    HyperScreenBody(items = mockItems, onClick = {})
}

@Preview(showBackground = true)
@Composable fun HyperScreenPreview() = HyperScreen()
```

### Прочее

- UI-компонент **не знает** ViewModel; действия через callbacks.
- Side-effects — только `LaunchedEffect` / `DisposableEffect`.
- ViewModel: `StateFlow<UiState>`, `sealed interface UiError` — см. `guidelines/00_guidelines.md`.
- Material3 API — см. `guidelines/Compose_Refactoring.md`.

---

## 4. Текущее состояние legacy `:app` (baseline)

| Аспект | Состояние |
|--------|-----------|
| UI | 42 XML layouts, ViewBinding, 0 Compose |
| Архитектура | MVP Presenters; 1 ViewModel (`ChannelViewModel`) |
| Навигация | Intent + ViewPager2 + Drawer; Navigation Component не используется |
| Data | `:data` (Room), `:xtream` (API), presenters в `:app` |
| Сборка | `assembleDebug` OK после фиксов §5 |

**Экраны (приоритет миграции):** см. `Roadmap.md` §4.

---

## 5. История: проблема → решение (сессия 2026-06-18)

### 5.1 Сборка проекта
| Проблема | Решение |
|----------|---------|
| `:xtream:kaptGenerateStubsDebugKotlin` — JVM target Java 17 vs Kotlin 21 | В `xtream/build.gradle.kts` добавить `kotlinOptions { jvmTarget = "17" }` под `compileOptions` Java 17 |
| `:app:compileDebugKotlin` — та же ошибка JVM | В `app/build.gradle.kts` добавить `kotlinOptions { jvmTarget = "17" }` |
| `OnCategoryItemClickListener { ... }` — «Interface does not have constructors» | В `CategoryAdapter.kt`: `interface` → `fun interface` (SAM) |
| Gradle verbose log раздувается | `$env:ORG_GRADLE_PROJECT_org_gradle_console='plain'` при CI/агент-сборке |

**Команда сборки:** `.\gradlew.bat assembleDebug --no-daemon`  
**Артефакт:** `app/build/outputs/apk/debug/iptv-debug.apk`

### 5.2 Уточнение: fun interface
Kotlin `fun interface` = SAM с одним абстрактным методом; лямбда вместо `object : ...`.

### 5.3 Уточнение: архитектура 2-в-1 (не FireTvRoot)

| Было (неверно) | Стало |
|----------------|-------|
| Один `FireTvRoot` + `when(formFactor)` в Compose | `LauncherActivity` → `phone/MainActivity` **или** `tv/MainActivity` |
| Отдельные gradle-модули compose-mobile/phone | Пакеты `ui/mobile/*` и `ui/tv/*` внутри app |
| CompositionLocal / общий ScreenBody | Зеркальные файлы: `ui/mobile/PrettyButton`, `ui/tv/PrettyButton` |
| Общий NavHost с ветвлением | Свой `NavHost` в каждой Activity |

### 5.4 Уточнение: Screen / ScreenBody

| Правило | |
|---------|--|
| `*Screen()` | Без параметров; контейнер → вызывает `*ScreenBody` |
| `*ScreenBody()` | Только динамика + callbacks; статика (`stringResource`) внутри |
| Preview | `mockScreens` в app-compose, MOCK в `*Screen()` |

Детали: `guidelines/SCREEN_SCREENBODY.md`

---

## 6. Чеклист агента перед PR

- [ ] `./gradlew assembleDebug` зелёный
- [ ] Новые модули: `jvmTarget = "17"` везде, где Java 17
- [ ] Callback-интерфейсы для лямбд — `fun interface`
- [ ] Screen/Body split: `*Screen()` без параметров; статика внутри ScreenBody
- [ ] MP/Roadmap обновлены при смене архитектуры
- [ ] Визуальный паритет: screenshot diff или чеклист экрана (Roadmap §6)

---

## 7. Ссылки

| Документ | Содержание |
|----------|------------|
| [Roadmap.md](Roadmap.md) | Полное ТЗ, фазы, матрица экранов |
| [ARCHITECTURE/compose-architecture.md](ARCHITECTURE/compose-architecture.md) | Диаграммы, зависимости модулей |
| [TASKS/history/history.md](TASKS/history/history.md) | Лог сессий |
| [guidelines/SCREEN_SCREENBODY.md](guidelines/SCREEN_SCREENBODY.md) | Screen / ScreenBody / mockScreens |
| [reference/cinemakmp/](reference/cinemakmp/) | Референс (пока пустой) |

---

## 8. Git / артефакты агента

- Skills: `.cursor/skills/firetv-gradle-build/`, `.cursor/skills/firetv-compose-port/`
- Документация коммитится в основной репозиторий FireTv (уже git).
- Push на GitHub — **только по запросу пользователя**; агент коммитит MP/skills при существенных изменениях, если пользователь явно разрешил коммиты.
