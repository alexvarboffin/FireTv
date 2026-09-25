# FireTv / Ultimate.TV — состояние проекта

Снимок на **2026-09-25**. Что известно о проекте по итогам работы 2026-09-21…25.
Подробный журнал изменений и их целей: [TASKS/history/2026-09-21_25_session.md](TASKS/history/2026-09-21_25_session.md).

---

## 1. Цель проекта

Заменить опубликованное в Google Play XML-приложение **Ultimate.TV** (`:app`) на новое Compose-приложение (`:app-compose`) **под тем же listing**: тот же `applicationId`, тот же ключ подписи, та же реклама.

Новое приложение — **универсальное 2-в-1** (один APK):

- **phone** — Compose Material3, визуально повторяет legacy `:app`;
- **TV** — адаптация того же phone-Compose под пульт (`androidx.tv.material3`, D-pad), с UX-паттернами из референса CinemaKMP.

Главная идея архитектуры (формулировка пользователя): **два независимых UI, но данные у них общие** — ViewModel и репозитории не дублируются.

---

## 2. Архитектура

```
LauncherActivity ── UI_MODE_TYPE_TELEVISION? ──► tv/MainActivity    → ui/tv/*     (androidx.tv.material3)
                 └─────────────────────────────► phone/MainActivity → ui/mobile/* (androidx.compose.material3)
                                                        │
                        :core:presentation (ViewModels, общие для phone и TV)
                        :core:domain (модели, интерфейсы репозиториев)
                        :core:data-bridge (реализации поверх :data / :xtream)
                        :data (Room, M3U), :xtream (Xtream API, Ktor)
```

Правила:

- проверка «это TV» — только в `LauncherActivity` (`Configuration.UI_MODE_TYPE_TELEVISION`); в рекламе используется та же проверка (`isTelevisionUi()`);
- `ui/tv` использует **только** `androidx.tv.material3` и не делегирует в `ui.mobile.*ScreenBody`;
- экраны делятся на `*Screen()` (без параметров, подключает VM) и `*ScreenBody(...)` (данные + callbacks), см. `guidelines/SCREEN_SCREENBODY.md`;
- legacy `:app` живёт параллельно до cutover и использует те же `:data` / `:core` репозитории.

### Модули (`settings.gradle.kts`)

| Модуль | Роль |
|--------|------|
| `:app` | legacy XML-приложение (сейчас в Play) |
| `:app-compose` | новое Compose-приложение (phone + TV) |
| `:common-resources` | **общие строки en + ru** для `:app` и `:app-compose` (создан 2026-09-24) |
| `:core:domain`, `:core:presentation`, `:core:data-bridge` | общий слой данных и ViewModel |
| `:data`, `:xtream` | Room / M3U; Xtream API (kapt) |
| `:simplesearchview`, `:mylibrary` | библиотеки legacy |
| `:shared`, `:features:ui` | внешний репозиторий `C:\Synced\WalhallaUI` (подключён через `projectDir`) |

### Пакеты `:app-compose`

`launcher/`, `phone/`, `tv/` (Activity, NavHost, CompositionLocal), `ui/mobile/*`, `ui/tv/*` (зеркальные экраны), `player/` (JiaoZi-плеер, Cast), `ads/` (AdMob), `navigation/` (routes, `PlayerBrowseScope`), `mockScreens/` (preview).

---

## 3. Сборка

| Что | Значение |
|-----|----------|
| Gradle wrapper | **9.7.1** (поднят пользователем 2026-09-25, коммит `3d7d045`) |
| AGP | **9.1.0** (там же) |
| Kotlin | 2.2.21 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 23 (`:app-compose` — 24: OkHttp 5.5 `Dns.newCall` + desugaring, см. чеклист I10) |
| Compose BOM | 2026.09.00 |
| Ktor | единая версия `ktor = "3.6.0"` для всех `ktor-*` |

Совместимость с AGP 9 держится на **opt-out** в `gradle.properties`:

```properties
android.builtInKotlin=false
android.newDsl=false
```

Opt-out нужен, пока модули используют плагин `org.jetbrains.kotlin.android`, `kapt` и `kotlinOptions`. В AGP 10 его уберут. Полная миграция затронет 11 модулей FireTv и 2 модуля WalhallaUI, и её надо согласовать отдельно.

Команда сборки (Windows):

```powershell
taskkill /F /IM java.exe
$env:GRADLE_OPTS='-Djava.net.preferIPv4Stack=true'
.\gradlew.bat :app-compose:assembleDebug :app:assembleDebug --no-daemon --console=plain `
  "-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true"
```

Без `preferIPv4Stack` Gradle на этой машине регулярно падает с `java.net.BindException: Address already in use: bind` (FileLockContentionHandler). В `gradle.properties` флаг **не записан**.

Артефакты (оба называются `iptv-*.apk`, так как `base.archivesName = "iptv"`):

- `app/build/outputs/apk/debug/iptv-debug.apk` — ~74 МБ;
- `app-compose/build/outputs/apk/debug/iptv-debug.apk` — ~150 МБ (вероятно, `libvlc-all` со всеми ABI).

---

## 4. Идентичность для Google Play

| Параметр | `:app` (legacy) | `:app-compose` |
|----------|-----------------|----------------|
| `applicationId` | `tv.hdonlinetv.besttvchannels.movies.watchfree` | **тот же** (было `….compose`) |
| `namespace` (R, исходники) | `tv.hdonlinetv.besttvchannels.movies.watchfree` | `tv.hdonlinetv.compose` — на Play не влияет |
| Подпись | `app/keystore/keystore.jks`, alias `release` | **тот же** keystore (`../app/keystore/keystore.jks`) для debug и release |
| `versionCode` | дата `yyMMdd` | та же схема |
| `versionName` | `1.4.$code` | `1.4.$code` |
| app name (release) | `Ultimate.TV` | `Ultimate.TV` |

`versionCode` загружаемого AAB должен быть **больше**, чем у текущей версии в Play Console.

---

## 5. Реклама (AdMob)

- ID те же, что в legacy (`app/src/main/res/values/const.xml`): App ID `ca-app-pub-5111357348858303~6069049164`, banner `…/6032819324`; в compose — `app-compose/src/main/res/values/ads.xml`.
- `MobileAds.initialize` — в `ComposeApp`, **кроме TV**.
- `AdMobBanner` (`ads/AdMobBanner.kt`) — anchored adaptive banner внизу phone `MainShellScreen`, контейнер скрыт до `onAdLoaded` (как legacy `include_banner_ad`). На TV не создаётся.
- DEBUG → тестовый unit Google, release → боевой.
- Логи: tag `AdMobBanner` (load / loaded / failed с code+message / opened / closed / clicked / impression), tag `ComposeAds` (инициализация).
- GDPR/UMP: `ads/AdsConsent.kt`, вызов в `phone/MainActivity` после `setContent` (на TV пропуск), tag `AdsConsent`. Форма появится только после создания GDPR-сообщения в AdMob → Privacy & messaging; сейчас SDK отвечает `Publisher misconfiguration: no form(s) configured` (тот же App ID, что у legacy).
- Interstitial **не перенесён намеренно**: в legacy он не работает (`admob_interstitial_unit_id` = `"0"`, интервал 0, `onAdLoaded(InterstitialAd)` объявлен без `override` и SDK его не вызывает). Нужен боевой unit ID и решение владельца.
- **Не перенесено** в compose: Firebase (analytics, messaging), OneSignal, плагин `google-services`.

---

## 6. Cast (phone)

Паттерн из CinemaKMP:

- `phone/MainActivity` — **`FragmentActivity`** (не `ComponentActivity`), тема `Theme.ComposePhone` (`Theme.AppCompat.DayNight.NoActionBar`);
- в Activity висит скрытый `CustomMediaRouteButton`, зарегистрированный в `MediaRouteButtonManager`;
- Compose-иконка `CastMediaRouteButton` вызывает `MediaRouteButtonManager.current?.performClick()`.

Без этого `MediaRouteButton` падает: `The activity must be a subclass of FragmentActivity`, затем `You need to use a Theme.AppCompat theme`.

---

## 7. Строки и локализация

- Общие строки (табы плейлиста / Xtream / сериала, playlist manage, settings, Cast, On/Off, tutorial/FAQ и т.д.) — `:common-resources`, `values` + `values-ru`.
- В `app-compose/res` — только compose/TV-специфичное: `menu_*`, onboarding, `layout_options*`, `compose_*_title`.
- Legacy `:app` может переопределять общий ключ своим значением (например `channels_format` = `Channels: %1$d`).
- В Kotlin UI не должно быть литералов EN/RU — только `stringResource` / `stringArrayResource`.

---

## 8. Ключевые TV-паттерны (кратко)

Полный перечень с проверками — `guidelines/USER_CORRECTED_CHECKLIST.md`, глобальный skill `~/.cursor/skills/compose-tv-from-mobile` (+ `USER_CORRECTIONS.md`).

- Shell: `NavigationDrawer` (скроллится, полный набор пунктов phone-drawer + FAB-действия) + `TvFocusTabRow` (переключение по фокусу, со счётчиками).
- Search / Playlist Management / Settings / Tutorial открываются **внутри shell**, drawer остаётся.
- Возврат фокуса в тот пункт drawer, откуда ушли: `focusRestorer` (не ручной save/restore — ломается на Lazy).
- `TvEditableField`: фокус = редактирование, Up/Down уходят, Left в начале строки → drawer.
- Диалоги — Compose `Dialog`, захват фокуса, Back закрывает; long-press — действие на KeyUp, arm-задержка, фокус на Cancel.
- Уведомления — `NotificationManager` + `NotificationOverlay`, без захвата фокуса; LOADING-overlay на долгие операции.
- TV-плеер — свой экран: Compose-chrome поверх JZ (в DEBUG видны оба), Canvas-шкала, таймер скрытия сбрасывается при действиях, штора каналов по Left с browse scope, CH±.

---

## 9. Референсы

- CinemaKMP (TV-паттерны, Cast): `Documentation/reference/cinemakmp-notKmp` — `git clone -b notKmp --depth 1`, 2026-09-22 ~16:22; также `C:\android\cinemakmp`. Папка `Documentation/reference/` в `.gitignore`.
- Демо-плейлисты для проверок — экран Tutorial & FAQ → Sample Playlist URL (для smoke лучше **UK**, ~50 КБ).

---

## 10. Правила работы, заданные пользователем

1. Делать задачу **в рамках запроса**. Любые побочные или общие правки (core, data, build, соседние модули) — сначала предупредить и дать список правок.
2. После каждого исправленного замечания — дописать пункт в `guidelines/USER_CORRECTED_CHECKLIST.md` (по нему другой ИИ проверяет проект), для TV-UX — ещё и в skill `compose-tv-from-mobile`.
3. Перед «готово» прогонять полный путь на устройстве/эмуляторе, а не только компиляцию.
4. Временные папки агента — в `.gitignore`.
5. Не менять смысл кода «заодно» (пример: `Key.Home` → нельзя просто удалить; в новом Compose это `Key.SystemHome`, код 3).

---

## 11. Открытые вопросы и риски

| # | Тема | Состояние |
|---|------|-----------|
| 1 | AGP 9 opt-out (`builtInKotlin`, `newDsl`) | временное решение; нужна согласованная миграция модулей и WalhallaUI |
| 2 | `preferIPv4Stack` | нужен для сборки на этой машине, в проект не записан |
| 3 | Release-сборка с R8 | `:app-compose` — 2026-09-25 собрана (APK 114,6 МБ, AAB 119,2 МБ), smoke на эмуляторе пройден (онбординг, URL-плейлист, воспроизведение, TV Settings). Правила — `app-compose/proguard-rules.pro`. APK и AAB собирать раздельно с `-Xmx6144m`. На реальном TV не проверялось. `:app` не проверялся |
| 4 | Interstitial, Firebase, OneSignal, `google-services` | не перенесены; interstitial в legacy мёртв (см. §5) |
| 5 | Размер compose (~115–120 МБ release) | вероятная причина — `libvlc-all` (все ABI); Play раздаст split по ABI из AAB || 6 | Папка `.tmp/` в корне | осталась после попытки сборки 2026-09-25 (логи Kotlin-демона); не в `.gitignore` (там только `.tmp-*/`) |
| 7 | Версии, пониженные 2026-09-21 из-за 404 Google Maven | `room 2.7.2`, `core 1.16.0`, `google-services 4.4.2` — до сих пор в каталоге |
| 8 | Roadmap, фаза 8 (cutover) | `applicationId`, подпись, ProGuard/R8, leanback/banner выровнены; legacy archive / flavor — не сделаны |
| 9 | Play Console | UMP-сообщение в AdMob; включить форм-фактор TV (иначе leanback-декларации не дадут TV-листинга); Data safety; раскатка через internal → staged production |
