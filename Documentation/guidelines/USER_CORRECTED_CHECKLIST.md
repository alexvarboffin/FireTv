# User-corrected checklist (verification)

> **Для ИИ-ревьюера:** проверь проект по пунктам ниже. Каждый кейс — реальное замечание пользователя, которое уже должно быть реализовано.
> **Для агента-разработчика:** после нового замечания пользователя и фикса — **добавь пункт сюда в том же ходе** (и зеркало в skill `compose-tv-from-mobile` / `USER_CORRECTIONS.md` при TV UX).

Формат пункта:

```
- [ ] ID — кратко что проверить
  Verify: где смотреть / ожидаемое поведение
```

Отмечай `[x]` только после проверки в коде/на устройстве.

---

## A. Архитектура TV / shell

- [ ] A1 — `ui/tv` только `androidx.tv.material3`, без делегирования в `ui.mobile.*ScreenBody`
  Verify: imports в `ui/tv/**`; нет `import …ui.mobile`

- [ ] A2 — Нет FAB на TV; действия FAB/top-bar phone → scrollable `NavigationDrawer`
  Verify: `ui/tv/main/MainShellScreen.kt` drawer items vs phone shell

- [ ] A3 — TabRow: фокус на таб **переключает** контент (`onFocus`), не только click
  Verify: `Tab(onFocus = { … })` в MainShell / Tutorial

- [ ] A4 — Search / PlaylistManage / Settings / Tutorial в shell (drawer selected), не «уход» с потерей drawer где так задумано
  Verify: `ShellPanel` / in-shell content; Left из полей → drawer

- [ ] A6 — Xtream browser / Serial detail / Onboarding на TV — native tv.material3 (TvFocusTabRow, Surface-блоки, TvLoadingOverlay); `TvNavHost` без material3 `Scaffold` (Box + `windowInsetsPadding(systemBars)`)
  Verify: `ui/tv/playlist/XtreamBrowserScreen.kt`, `SerialDetailScreen.kt`, `ui/tv/onboarding/OnboardingScreen.kt`, `tv/TvNavHost.kt`

- [ ] A5 — Left из контента восстанавливает **исходный** пункт drawer (`focusRestorer` + `LocalTvDrawerFocusRequester`)
  Verify: drawer LazyColumn + leftmost `focusProperties { left = drawerGroupFocus }`

---

## B. Списки, карточки, иконки

- [ ] B1 — Прозрачные иконки каналов: placeholder **по форме иконки**, не заливка всего айтема
  Verify: `RemoteImage` + `contentBackground` / `channel_icon_placeholder`; clip совпадает с image bounds

- [ ] B2 — При 1 колонке сетки — **list row**, не grid-карточка
  Verify: `ChannelGridBody` `listMode` → `ChannelListRow` (tv + mobile)

- [ ] B3 — List row показывает meta как legacy (категория, desc, favorite, geo и т.д.)
  Verify: `ChannelListRow` + `ChannelGeoLockBadge`

- [ ] B4 — Айтем плейлиста: иконка **источника** (cloud / local / buffer / xtream)
  Verify: TV `PlaylistCardTv` + `PlaylistTypeIcons`; phone `PlaylistCard`

- [ ] B5 — Refresh / Delete на плейлисте — **иконки**, не голый текст (где есть кнопки)
  Verify: PlaylistChannels / actions dialog labels

- [ ] B6 — Категории: бейдж количества каналов (`N ch`), если count > 0
  Verify: `CategoryUi.count`, `CategoryCard` (tv + mobile), DAO counts

---

## C. Плейлисты: delete / refresh / категории

- [ ] C1 — Delete **не сразу**: диалог подтверждения (Cancel в фокусе на TV + arm delay)
  Verify: `TvConfirmDeletePlaylistDialog`; mobile `AlertDialog` на delete плейлиста

- [ ] C2 — Refresh при пустом/ошибочном ответе **не стирает** текущий UI (`no item`); toast/notification со статистикой
  Verify: `PlaylistRefreshResult`; UI не переключается на empty из‑за `isLoading` wipe

- [ ] C3 — Refresh сохраняет `_id` плейлиста (не delete+новый id → orphan экрана каналов)
  Verify: `LocalPlaylistRepository.refreshFromUrl` / `clearPlaylistChannelLinks`

- [ ] C4 — Опциональная очистка пустых категорий при delete плейлиста (настройка, default Off)
  Verify: `AppSettingsUi.cleanupEmptyCategories`; до delete — cats листа; после — удалить empty; при включении On — сразу `deleteEmptyCategories`

- [ ] C6 — TV «Управление» (in-shell панель, ViewModel живёт с shell): после успешного сохранения повторное открытие **не** показывает toast успеха и не закрывается само; форма пустая
  Verify: `PlaylistManageViewModel.consumeSaved()` (сброс в `PlaylistManageUiState()`), вызов в TV `PlaylistManageScreen` после `notifications.show(SUCCESS)` перед `exit()`; на устройстве: сохранить → открыть «Управление» снова

- [ ] C5 — Пункт настройки cleanup **информативен**: title + summary + On/Off (не одно слово On)
  Verify: strings + Settings rows phone/TV

---

## D. Settings UI

- [ ] D1 — У пунктов Settings **тематические** иконки (не одна `ic_tv_icon` на всё)
  Verify: phone `LegacySettingsRow(icon = Icons.Outlined.*)`; TV `SettingsRow(icon = …)`
  Mapping: Version→Info, Display→GridView, Sort→SortByAlpha, Open→OpenInNew, Night→DarkMode, Cleanup→DeleteSweep, Player→VideoSettings, Tutorial→MenuBook, Privacy→PrivacyTip

- [ ] D2 — Phone Cast = Cinema livehack: `FragmentActivity` + `Theme.AppCompat` + hidden `CustomMediaRouteButton` + Compose `performClick` (не `ComponentActivity`, не platform Material theme)
  Verify: `phone/MainActivity.kt` FragmentActivity + host AndroidView; `MediaRouteButtonManager`; `CastMediaRouteButton` Icon + performClick; theme `Theme.ComposePhone`

- [ ] D3 — TV Settings имеет паритет с phone: Version, Sort (4 варианта), Open mode (details / player)
  Verify: `ui/tv/settings/SettingsScreen.kt` `SettingsChoice.Sort/OpenMode`; диалог выбора с фокусом на текущем значении

---

## H. Строки / i18n

- [ ] H1 — Общие UI-строки (табы плейлиста/Xtream/serial, playlist manage, settings cleanup, Cast CD, On/Off…) в **`:common-resources`** (`values` + `values-ru`), зависят `:app` и `:app-compose`
  Verify: `common-resources/src/main/res/values{,-ru}/strings.xml`; нет хардкода табов в Kotlin; `tab_titles_xtream*` arrays в common

- [ ] H2 — Compose-only / TV-only строки остаются в `app-compose` (`compose_*_title`, `menu_*`, `layout_options_tv`, onboarding…)
  Verify: `app-compose/src/main/res/values{,-ru}/strings.xml` — только form-factor; legacy overrides (`channels_format` полный текст) могут жить в `:app`

- [ ] H3 — Нет программного EN/RU в UI (`": ON"`, `"Cast"`, tab titles) — только `stringResource` / `stringArrayResource`
  Verify: grep UI на литералы; PlaylistManage local storage; CastMediaRouteButton; TV player `R.string.player_live` (LIVE / ЭФИР)

- [ ] H4 — Phone Tutorial&FAQ: табы **по центру/fill** (как legacy TabLayout fixed+fill); иконки табов **24dp**; copy у sample playlists **32dp** (не intrinsic 42 + IconButton 48)
  Verify: `ui/mobile/info/TutorialScreen.kt` TutorialIconTabRow weight(1f)+size(24); `TutorialFaqContent` FaqPlaylistRow size(32)

- [ ] H5 — Phone Category tab: **3** колонки на tablet (`sw≥600`) или landscape, иначе **2**
  Verify: `ui/mobile/category/CategoryScreen.kt` `columns` from orientation / screenWidthDp

- [ ] H6 — «Parse clipboard» парсит только **сырой текст M3U** (`M3UParser`, нужен `#EXTINF`), ссылку из буфера не скачивает → подпись это явно говорит: «Распознать буфер обмена (текст плейлиста M3U)» / «Parse clipboard (M3U playlist text)»; автоопределения текст/ссылка нет (решение пользователя)
  Verify: `parse_clipboard` одинаковый в `common-resources` и `:app` (`values`, `values-ru`)

---

## I. Play cutover (замена listing)

- [ ] I1 — `applicationId` = legacy `tv.hdonlinetv.besttvchannels.movies.watchfree` (не `.compose`)
  Verify: `app-compose/build.gradle.kts` defaultConfig.applicationId

- [ ] I2 — Release/debug signing = тот же `app/keystore/keystore.jks` (alias `release`)
  Verify: `signingConfigs.x` + `signingConfig` на debug/release

- [ ] I3 — AdMob: `APPLICATION_ID` в manifest + phone banner на MainShell (debug=test unit; release=`b1` / admob_banner_unit_id); **TV: no MobileAds.init / no banner**; listener → Log
  Verify: `ads.xml`, meta-data, `AdMobBanner` + `isTelevisionUi()`; `ComposeApp` skip TV; logcat tag `AdMobBanner` / `ComposeAds`

- [ ] I4 — `versionCode` > текущего в Play Console; `versionName` линия `1.4.*`
  Verify: перед upload сравнить с Console; date-based code

- [ ] I5 — Release собирается с R8 и работает: правила legacy перенесены (`-dontoptimize`, aliyun, JZ-движки по конструктору, Jzvd, VLC, Glide, Cast OptionsProvider, Parcelable/Serializable)
  Verify: `app-compose/proguard-rules.pro`; `assembleRelease` + `bundleRelease` (раздельно, `-Xmx6144m`); release smoke: онбординг → добавить URL-плейлист → канал играет → TV Settings

- [ ] I6 — GDPR/UMP на phone: `AdsConsent.gather()` в `phone/MainActivity` (на TV пропуск); форма показывается только после настройки GDPR-сообщения в AdMob → Privacy & messaging
  Verify: `ads/AdsConsent.kt`; logcat tag `AdsConsent` (без формы в консоли — `Publisher misconfiguration`)

- [ ] I9 — GDPR/UMP **не запускается на TV**: ни `tv/MainActivity`, ни TV-экраны не вызывают `AdsConsent` / `UserMessagingPlatform`; phone-путь на TV-устройстве тоже отсекается `isTelevisionUi()`
  Verify: grep `AdsConsent|UserMessagingPlatform` — вызов только в `phone/MainActivity.kt`; guard в `ads/AdsConsent.kt`; на TV в logcat нет запросов consent (tag `AdsConsent` → только `skip on television`, если вообще есть)

- [ ] I10 — `:app-compose` `minSdk = 24` (`android-minSdkCompose`), без костылей в `FilteredDns`: при minSdk 23 D8 не добавлял forwarder default-метода `Dns.newCall` (OkHttp 5.5.0 через Ktor 3.6.0) → `AbstractMethodError` в плеере (Error в потоке OkHttp, try/catch не ловит)
  Verify: `app-compose/build.gradle.kts` minSdk; `FilteredDns` — только `lookup`; debug: открыть канал → играет, в `logcat -b crash` пусто

- [ ] I7 — TV-листинг: `leanback` + `touchscreen` `required=false`, `LEANBACK_LAUNCHER` у `LauncherActivity`, `android:banner` 320×180
  Verify: `app-compose/src/main/AndroidManifest.xml`; `drawable-xhdpi/tv_banner.png`

- [ ] I8 — Interstitial **не переносится**: в legacy он мёртв (unit id `"0"`, interval 0, `onAdLoaded` без `override`); включать только с боевым ID по решению пользователя
  Verify: в app-compose нет InterstitialAd

---

## E. TV Player

- [ ] E1 — Пока spinner / play скрыт: фокус на Favorite/Close, **не** на отсутствующий `frPlay` (Right не в пустоту)
  Verify: `PlayerScreen.kt` `chromePrimaryFocus` / `isPreparing`

- [ ] E2 — Нет redundant иконки «список каналов» справа: шторка открывается **Left** / Menu
  Verify: нет `Icons.Filled.List` / frChannels button в top bar; `Key.DirectionLeft` → `openChannelSheet`

- [ ] E3 — Player — TV экран (`ui/tv/player`), не mobile Scaffold/Cast
  Verify: package path + tv.material3 chrome

- [ ] E4 — Overlay auto-hide: таймер сбрасывается на focus/click/D-pad
  Verify: `resetHideTimer` на focus/click

---

## F. Диалоги / long-press / поля / loading

- [ ] F1 — Модалки = Compose `Dialog`, Back dismiss, фокус внутри
  Verify: About / settings choices / confirm delete

- [ ] F2 — Long-press: act on **KeyUp**; dialog arm ~300ms; initial focus **Cancel**
  Verify: playlist cards + actions/confirm dialogs

- [ ] F3 — `TvEditableField`: focus = edit; Up/Down leave; Left at caret start → drawer/leftFocus
  Verify: `TvEditableField.kt`; Search/Manage leftFocus

- [ ] F4 — Paste URL: кнопка под полем (D-pad), не только сбоку if that was corrected
  Verify: `PlaylistManageScreen` TV layout

- [ ] F5 — Long download/parse: non-cancelable LOADING overlay
  Verify: `TvLoadingOverlay` / `isSaving`

- [ ] F6 — Notifications не крадут фокус
  Verify: `NotificationOverlay` `canFocus = false`

---

## G. Процесс / репозиторий

- [ ] G1 — `Documentation/reference/` в `.gitignore`; clone cinemakmp не в индексе
  Verify: `.gitignore` + `git ls-files Documentation/reference`

- [ ] G2 — Не трогать shared/core «заодно» без предупреждения и списка правок
  Verify: процесс агента (rule); не код

---

## Как добавлять новый кейс

1. Исправить код.
2. Добавить пункт в **этот файл** (секция + Verify).
3. Добавить строку Wrong→Right в  
   `~/.cursor/skills/compose-tv-from-mobile/USER_CORRECTIONS.md` (если TV/UX).
4. При необходимости — hard rule в skill `SKILL.md`.

Дата последнего обновления чеклиста: **2026-09-25**.
