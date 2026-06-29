# Compose refactoring — Material 3 + Screen split

## Screen / ScreenBody

См. **[SCREEN_SCREENBODY.md](SCREEN_SCREENBODY.md)** — источник истины.

Кратко:
- `*Screen()` — без параметров, контейнер
- `*ScreenBody(...)` — динамика + callbacks; статика через `stringResource` / `painterResource` внутри
- `mockScreens/` — Preview с MOCK-данными

## Material 3 (актуальные API)

| ❌ Устаревшие | ✅ Актуальные |
|------------|------------|
| `Divider` | `HorizontalDivider` |
| `Icons.Default.ArrowBack` | `Icons.AutoMirrored.Filled.ArrowBack` |
| `BottomNavigation` | `NavigationBar` |
| `TabRow` | `PrimaryTabRow` / `SecondaryTabRow` |

## Компоненты

- Разбивка: **Screen** → **ScreenBody** → **components/** → **Dialogs** (на уровне Screen).
- Dialog не открывать внутри мелкого widget.
- Overlay — только в root `Scaffold` / `Box` ScreenBody.
- Если нет Preview — разбит неправильно (используй `mockScreens`).
- Экспериментальные API — в wrapper (`PrettyButton` и т.д.).

## ViewModel

См. `00_guidelines.md` — StateFlow, UiState, без UI-кода во ViewModel.
