# Aliyun Player (JZMediaAliyun) — лицензирование и откат версии

Дата конспекта: **2026-09-27**. Контекст: phone Compose, движок `JZMediaAliyun` через `AliPlayerFactory` / `UrlSource`.

## Симптом

При выборе медиаплеера **JZMediaAliyun** канал не играет: UI «video loading failed» / JZVD `onError`.

Logcat (пример на `7.15.0-full` и позже на `6.21.0-full`):

```
license authorization failed
NoDefaultLicense
LicenseKey Passed is invalid
Please provide correct license key before play
license check failed when prepare
ModuleCode: 4100  (на 7.x) / VodErrorCallback (на 6.21)
```

Это **не** NDK и не ProGuard: нативный `AliFrameWork` / `licenseManager` внутри AAR Aliyun Player SDK.

Другие движки (Exo / System / IJK / VLC) License Aliyun **не** требуют. Дефолт приложения — Exo (`mediaPlayerOption = 2`).

## Политика Aliyun (с 7.0.0)

С **2025-02-14 / SDK 7.0.0** мобильный Player SDK платный: Standard / Professional через License Key + сертификат (привязка к package name).  
Standard и Pro — **один и тот же** Maven-артефакт; отличие только в типе License в консоли.

- Standard (mobile): платно (~999 ¥/год/приложение по прайсу Aliyun).
- Trial: ~1 месяц Pro через консоль.
- Обойти проверку на 7.x «хаком» нельзя — SDK сам валит `prepare`.

Доки: [получить License](https://help.aliyun.com/zh/vod/developer-reference/obtain-the-player-sdk-license), [биллинг](https://help.aliyun.com/zh/vod/product-overview/billing-of-value-added-services).

## История в git (FireTv)

| Коммит | Что с Aliyun |
|--------|----------------|
| `8cddbf1` (root, 2025-09-13) | В `app/build.gradle.kts` **закомментировано**: `// implementation 'com.aliyun.sdk.android:AliyunPlayer:4.5.0-full'` — исходный pin проекта. |
| `836d6fe` | Включили зависимость через catalog: **`7.15.0-full`**. |

Итого: в самом начале в репо уже фигурировал **`4.5.0-full`**, не 7.x. Платная линейка попала позже.

## Что пробовали (2026-09-27)

| Версия | Результат |
|--------|-----------|
| `7.15.0-full` | Жёсткий License fail при play. |
| `6.21.0-full` (последняя до 7.0) | На устройстве всё равно `[6.21.0_46172822]` + `NoDefaultLicense` / `license check failed when prepare`. Откат «просто ниже 7» **не** спасает. |
| AAR scan Maven (`6.19`…`6.12`, `5.5.6.0-full`) | В бинарниках уже есть `licenseManager` / `NoDefaultLicense`. |
| **`4.5.0-full`** (как в первом коммите) | В AAR **нет** строк license-гейта. Выбрана как рабочий pin без Key. |

Текущий pin: `gradle/libs.versions.toml` → `aliyunPlayer = "4.5.0-full"`  
(`app` и `app-compose` через `libs.aliyun.player`).

## Рабочие варианты на будущее

1. **Оставить `4.5.0-full`** — без License Key; возможны старые баги / ABI / 16 KB page size; следить за Play и устройством.
2. **Купить Standard** + вшить License Key + `.crt` в Manifest/assets → можно снова поднять 7.x.
3. **Убрать Aliyun из UI** / мапить option `1` → Exo — если движок не нужен для IPTV/M3U.
4. Не рассчитывать на «тихий» откат к 6.x с текущего Maven: проверка лицензии уже в опубликованных 6.x сборках.

## Связанные файлы

- `gradle/libs.versions.toml` — `aliyunPlayer`
- `app-compose/.../cn/jzvd/demo/CustomMedia/JZMediaAliyun.java`
- `app-compose/.../player/LegacyJzPlayerSetup.kt` (option `1` → Aliyun)
- `core/data-bridge/.../LocalSettingsRepository.kt` — `DEFAULT_MEDIA_PLAYER = 2` (Exo)

## Не путать с

- **R8 / IJK** (`IjkMediaPlayer._setDataSourceFd`) — отдельная проблема JNI keep-rules при optimize; не License Aliyun.
- **Portrait crop** (`VIDEO_IMAGE_DISPLAY_TYPE`) — масштаб TextureView, не License.
