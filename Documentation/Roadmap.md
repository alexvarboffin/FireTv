# Roadmap — FireTv → Jetpack Compose (Mobile + Phone/TV)

**Версия:** 2026-06-18  
**MP:** [MasterPrompt.md](MasterPrompt.md)  
**Acceptance:** runnable app + ветка **phone** (`ui/mobile`) = **100%** визуала legacy `:app`

---

## 1. Scope

### In scope
- `:core:*` + `:app-compose` с пакетами `phone/`, `tv/`, `ui/mobile/`, `ui/tv/`
- `LauncherActivity` — единственный детект; старт `phone/MainActivity` или `tv/MainActivity`
- Зеркальная структура UI: `ui/mobile/X` ↔ `ui/tv/X`
- ViewModels в `:core:presentation` — общие
- Reuse `:data`, `:xtream`

### Out of scope (v1)
- KMP/iOS
- Один `setContent { when(isTv)... }` — **запрещено**
- Отдельные gradle-модули на каждый виджет
- Полный отказ от legacy `:app` (до фазы 8)
- Переписывание WalhallaUI `:features:ui` (оборачивать или дублировать Compose-диалоги)
- Player rewrite (оставить `AndroidView` + Media3/JiaoZi до фазы 7)

---

## 2. Целевая структура репозитория

```
FireTv/
├── app/                          # LEGACY
├── app-compose/                  # универсальное приложение 2-в-1
│   └── src/main/java/.../
│       ├── launcher/LauncherActivity.kt
│       ├── phone/MainActivity.kt
│       ├── tv/MainActivity.kt
│       └── ui/
│           ├── mobile/           # material3 (PrettyButton, CategoryScreen, …)
│           └── tv/               # tv.material3 (те же имена файлов)
├── core/
│   ├── domain/
│   ├── presentation/             # ViewModels
│   └── data-bridge/
├── data/
├── xtream/
└── Documentation/
```

### Зависимости
```
app-compose → core:presentation, core:domain, core:data-bridge
core:presentation → core:domain, core:data-bridge
core:data-bridge → :data, :xtream
```

`ui/mobile` и `ui/tv` — **внутри** `app-compose`, не отдельные gradle-модули.

---

## 3. Form-factor strategy

### 3.1 Launcher → отдельная Activity (единственный способ)

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".launcher.LauncherActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".phone.MainActivity" />
<activity android:name=".tv.MainActivity" />
```

```kotlin
// launcher/LauncherActivity.kt
val isTv = (configuration.uiMode and UI_MODE_TYPE_MASK) == UI_MODE_TYPE_TELEVISION
startActivity(Intent(this, if (isTv) tv.MainActivity::class.java else phone.MainActivity::class.java))
finish()
```

❌ Не делать: `when(isTv)` внутри одного `setContent`.

### 3.2 Зеркало UI

| mobile (phone) | tv |
|----------------|-----|
| `ui/mobile/PrettyButton.kt` | `ui/tv/PrettyButton.kt` |
| `ui/mobile/category/CategoryScreen.kt` | `ui/tv/category/CategoryScreen.kt` |
| `phone/MainActivity` + `PhoneNavHost` | `tv/MainActivity` + `TvNavHost` |

Общее только в `:core:presentation` (ViewModel + UiState).

### 3.3 Product flavors (опционально, фаза 8+)
Два APK вместо одного — только если понадобится уменьшить размер; по умолчанию **один universal APK**.

---

## 4. Матрица экранов (legacy → Compose)

| # | Legacy | Путь | Compose (mobile / tv) | VM | Приоритет |
|---|--------|------|----------------------|-----|-----------|
| 1 | SplashActivity | `activity/SplashActivity.kt` | `SplashScreenBody` | `SplashViewModel` | P0 |
| 2 | OnboardingActivity | `com/walhalla/onboarding/` | `OnboardingScreenBody` | `OnboardingViewModel` | P1 |
| 3 | MainActivity | `activity/MainActivity.kt` | `MainShellBody` (drawer+tabs) | `MainViewModel` | P0 |
| 4 | CategoryFragment | `fragment/CategoryFragment.kt` | `CategoryGridBody` | `CategoryViewModel` | P0 |
| 5 | AllChannelFragment | `fragment/AllChannelFragment.kt` | `ChannelListBody` | `AllChannelViewModel` | P0 |
| 6 | ChannelActivity | `activity/ChannelActivity.kt` | reuse `ChannelListBody` | `ChannelListViewModel` | P0 |
| 7 | DetailsActivity | `activity/DetailsActivity.kt` | `DetailsScreenBody` | `DetailsViewModel` | P1 |
| 8 | FavoriteActivity / FavoritesFragment | | `FavoritesBody` | `FavoritesViewModel` | P1 |
| 9 | SearchActivity | `activity/SearchActivity.kt` | `SearchScreenBody` | `SearchViewModel` | P1 |
| 10 | SettingsActivity | `activity/SettingsActivity.kt` | `SettingsScreenBody` | `SettingsViewModel` | P1 |
| 11 | PlaylistManagementActivity | | `PlaylistManageBody` | `PlaylistManageViewModel` | P2 |
| 12 | PlaylistActivity | `activity/playlist/PlaylistActivity.kt` | `PlaylistBrowserBody` | `PlaylistViewModel` | P2 |
| 13 | PlaylistXtreamActivity | | `XtreamBrowserBody` | `XtreamBrowserViewModel` | P2 |
| 14 | XstreamsLive/VOD/Series fragments | `fragment/xtream/` | `Xtream*Body` | `Xtream*ViewModel` | P2 |
| 15 | SerialInfoActivity + nested | `activity/serial/` | `SerialDetailBody` | `SerialViewModel` | P3 |
| 16 | PlrActivity | `activity/player/PlrActivity.kt` | `PlayerScreen` (AndroidView) | `PlayerViewModel` | P3 |
| 17 | Tutorial/FAQ/Privacy | | `InfoScreenBody` | minimal | P4 |

**Порядок (`ui/mobile` + `phone/MainActivity`):** 1 → 4 → 5 → 3 → 6 → 7 → 8 → 9 → 10 → …

**Зеркало `ui/tv`:** после каждого экрана в mobile — копия в `ui/tv/` + TV focus.

---

## 5. Фазы работ

### Фаза 0 — Подготовка (1–2 дня)
- [ ] Зелёная сборка legacy — **DONE**
- [ ] `kotlin-compose` plugin + `tv-material` в catalog
- [ ] `app-compose`: `launcher/`, `phone/MainActivity`, `tv/MainActivity`, `ui/mobile/PrettyButton`, `ui/tv/PrettyButton`
- [ ] `LauncherActivity` в manifest как LAUNCHER

### Фаза 1 — Core skeleton (3–5 дней)
- [ ] `:core:domain`, `:core:presentation`, `:core:data-bridge`
- [ ] Routes в `core` или в `phone/navigation`, `tv/navigation` (одинаковые sealed class)

### Фаза 2 — Design system (3–4 дня)
- [ ] `ui/mobile/theme/PhoneTheme.kt` (material3)
- [ ] `ui/tv/theme/TvTheme.kt` (tv.material3)
- [ ] Пары виджетов: `PrettyButton`, `PrettyScaffold`, `Loading`, `Error`

### Фаза 3 — phone: первые экраны (1–2 недели)
- [ ] `phone/MainActivity` + `PhoneNavHost`
- [ ] Splash → Category → Channels (ViewModels из core)
- [ ] Coil3, unit-тесты VM

### Фаза 4 — phone: основной функционал (2–3 недели)
- [ ] Main shell, Details, Favorites, Search, Settings

### Фаза 5 — tv: зеркало + D-pad (2–3 недели)
- [ ] `tv/MainActivity` + `TvNavHost`
- [ ] Зеркальные экраны в `ui/tv/`
- [ ] Acceptance §6 для **phone** vs legacy

### Фаза 6 — Playlist + Xtream (2 недели)
- [ ] Playlist management, M3U browser, Xtream live/VOD/series
- [ ] `AddProfileFragment` → Compose dialog

### Фаза 7 — Player (1–2 недели)
- [ ] `PlrActivity` → `PlayerScreen` с `AndroidView(JZVideoPlayer)` или Media3 `PlayerView`
- [ ] Cast: оставить Cast SDK через `AndroidView` / Activity delegate

### Фаза 8 — Cutover
- [ ] `app-compose` → production `applicationId`
- [ ] Legacy `:app` archived или flavor `legacy`
- [ ] ProGuard/R8 правила перенесены
- [ ] Release signing verified

---

## 6. Acceptance: 100% визуальный паритет (phone / ui/mobile)

Для **каждого** экрана из матрицы §4 — ветка **phone** должна совпадать с legacy `:app`.

**Артефакты:**
- `Documentation/screenshots/legacy/`
- `Documentation/screenshots/phone-compose/`
- Diff в PR или ручной чеклист в `TASKS/acceptance/<screen>.md`

---

## 7. Presenter → ViewModel mapping

| Legacy Presenter | New ViewModel | Источник данных |
|------------------|---------------|-----------------|
| `CategoryPresenter` | `CategoryViewModel` | `:data` CategoryDao |
| `AllChannelPresenter` | `AllChannelViewModel` | `:data` |
| `FavoritePresenter` | `FavoritesViewModel` | Room favorites |
| `XtreamPresenter` | `Xtream*ViewModel` | `:xtream` |
| `PlaylistPresenterImpl` | `PlaylistViewModel` | `:data` |
| `PlaylistManagementPresenterImpl` | `PlaylistManageViewModel` | M3U parse |

**Паттерн адаптера (переходный):**
```kotlin
class CategoryViewModel(
    private val legacy: CategoryPresenterBridge
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState = _uiState.asStateFlow()
    fun load() { viewModelScope.launch { /* bridge → update state */ } }
}
```

---

## 8. Gradle: app-compose (один модуль, обе библиотеки)

```kotlin
// app-compose/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "tv.hdonlinetv.compose"
    compileSdk = 36
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)      // ui/mobile
    implementation("androidx.tv:tv-material:1.0.1")       // ui/tv
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(project(":core:presentation"))
    implementation(project(":core:data-bridge"))
}
```

---

## 9. Риски

| Риск | Митигация |
|------|-----------|
| cinemakmp пустой | Целевая архитектура в MP; обновить при появлении кода |
| 607-line Settings XML | Разбить на секции Compose; отдельный `SettingsViewModel` |
| Player + Cast сложность | Последняя фаза; AndroidView bridge |
| Java legacy (30+ files) | Конвертировать при касании экрана |
| WalhallaUI XML dialogs | Compose replacements или `AndroidViewBinding` временно |
| JVM target drift | Skill `firetv-gradle-build`; CI check |

---

## 10. Definition of Done (проект)

1. `app-compose` на phone и Fire TV (через `LauncherActivity`)
2. P0–P2 экраны в `ui/mobile` + `phone/MainActivity`
3. Зеркало в `ui/tv` + `tv/MainActivity`
4. Phone UI: §6 acceptance vs legacy
4. Нет регрессий: playlists, favorites, playback
5. `assembleRelease` успешен
6. MP и Roadmap актуальны
