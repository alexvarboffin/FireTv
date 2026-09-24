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

- [ ] C5 — Пункт настройки cleanup **информативен**: title + summary + On/Off (не одно слово On)
  Verify: strings + Settings rows phone/TV

---

## D. Settings UI

- [ ] D1 — У пунктов Settings **тематические** иконки (не одна `ic_tv_icon` на всё)
  Verify: phone `LegacySettingsRow(icon = Icons.Outlined.*)`; TV `SettingsRow(icon = …)`
  Mapping: Version→Info, Display→GridView, Sort→SortByAlpha, Open→OpenInNew, Night→DarkMode, Cleanup→DeleteSweep, Player→VideoSettings, Tutorial→MenuBook, Privacy→PrivacyTip

- [ ] D2 — Phone `MainActivity` = **AppCompatActivity** + **Theme.AppCompat** (не platform Material theme): Cast `MediaRouteButton` требует FragmentActivity
  Verify: `phone/MainActivity.kt` extends AppCompatActivity; manifest `android:theme="@style/Theme.ComposePhone"`; Cast click must not crash

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

Дата последнего обновления чеклиста: **2026-09-24**.
