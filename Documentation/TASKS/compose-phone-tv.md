# Задача: Compose phone + TV (2 в 1)

> Статус: описание зафиксировано 2026-10-19.  
> Референс реализации: этот же репозиторий **FireTv** (`:app-compose`, `Documentation/MasterPrompt.md` §1–2, skill `.cursor/skills/firetv-compose-port`).

## Цель

Один Compose-проект / один APK с **двумя независимыми UI-деревьями**:

| Ветка | Пакеты / вход | UI-библиотека |
|-------|---------------|---------------|
| Phone | `…phone.MainActivity`, `ui/mobile/*` | `androidx.compose.material3` |
| TV | `…tv.MainActivity`, `ui/tv/*` | `androidx.tv.material3` |

Общие: ViewModel, domain, data/repositories. UI не смешивать.

## Детект устройства (как в FireTv)

Единственная точка с детектом — **Launcher** (LAUNCHER в манифесте).  
Не `when (isTv)` внутри одного `setContent`.

Референс: `app-compose/.../launcher/LauncherActivity.kt`

```kotlin
val isTv =
    (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) ==
        Configuration.UI_MODE_TYPE_TELEVISION
// опционально DEBUG-исключения для эмуляторов / тестовых устройств
val target = if (isTv) TvMainActivity::class.java else PhoneMainActivity::class.java
startActivity(Intent(this, target))
finish()
```

Дальше каждая Activity поднимает свой theme + NavHost.

## Раскладка пакетов (зеркало имён)

```
app-compose/src/main/java/…/compose/
├── launcher/LauncherActivity.kt
├── phone/MainActivity.kt          (+ CompositionLocal для nav/repos)
├── tv/MainActivity.kt
└── ui/
    ├── mobile/…/FooScreen.kt      # material3
    └── tv/…/FooScreen.kt          # tv.material3, тот же смысл/имя

core/presentation/                 # общие ViewModels / UiState
core/domain/
core/data-bridge/                  # (или аналог data-слоя)
```

- Компоненты phone и tv **могут совпадать по имени** (`CategoryScreen`, `ChannelCard`, …) — различаются пакет и Material-импорты / фокус TV.
- Для каждого экрана/виджета в `ui/mobile` — пара в `ui/tv` (по мере порта).
- Порядок работ (референс skill): сначала phone + legacy parity, затем зеркало TV.

## Общий слой (не дублировать)

- ViewModels и UiState — только в `core/presentation` (или общем presentation-модуле).
- Репозитории / domain — общие; UI ветки только подписываются и вызывают те же API.
- Строки/ресурсы — общие где возможно; TV-only строки — допустимы отдельно.

## Запреты

- Один `MainActivity` с ветвлением всего UI по `isTv` в Composable.
- TV-экран, который просто зовёт `ui.mobile.*ScreenBody` «как есть» (TV = свой UI на `tv.material3`).
- Тянуть phone-only зависимости (FAB, touch-only жесты без D-pad пути) в TV без адаптации.

## Чеклист приёмки (коротко)

- [ ] LAUNCHER = Launcher → phone **или** tv Activity, затем `finish()`
- [ ] Детект через `UI_MODE_TYPE_TELEVISION` (как в FireTv)
- [ ] Пакеты `ui/mobile` и `ui/tv` с зеркальными именами экранов
- [ ] Общие ViewModel; нет дубля бизнес-логики в UI
- [ ] Manifest: leanback / TV banner / `LEANBACK_LAUNCHER` по необходимости (см. FireTv cutover)

## Куда смотреть в FireTv

| Тема | Файл |
|------|------|
| Архитектура целиком | `Documentation/MasterPrompt.md` §1–2 |
| Skill порта | `.cursor/skills/firetv-compose-port/SKILL.md` |
| Launcher | `app-compose/.../launcher/LauncherActivity.kt` |
| Phone / TV entry | `…/phone/MainActivity.kt`, `…/tv/MainActivity.kt` |
| Screen / ScreenBody | `Documentation/guidelines/SCREEN_SCREENBODY.md` |
| TV UX / фокус | skill `compose-tv-from-mobile` |
| Проверка после правок UX | `Documentation/guidelines/USER_CORRECTED_CHECKLIST.md` |

## Следующий шаг

После утверждения этого описания — переходить к конкретным экранам/модулям целевого Compose-проекта, опираясь на структуру FireTv выше.
