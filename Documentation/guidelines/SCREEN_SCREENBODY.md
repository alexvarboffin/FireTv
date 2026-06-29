# Screen / ScreenBody — правила

Обязательно для `ui/mobile/*` и `ui/tv/*` (зеркальные пары).

---

## 1. `*Screen()` — контейнер экрана

| Правило | |
|---------|--|
| Сигнатура | **Без параметров** — `@Composable fun FooScreen()` |
| Роль | Сборка визуального слоя: данные → `*ScreenBody()` |
| ViewModel | Допустим `viewModel()` **внутри** Screen (не в параметрах) |
| Preview | `@Preview` вешается на `*Screen()` в пакете `mockScreens` |

```kotlin
// production: ui/mobile/category/CategoryScreen.kt
@Composable
fun CategoryScreen() {
    val vm: CategoryViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    CategoryScreenBody(
        categories = state.categories,
        isLoading = state.isLoading,
        onCategoryClick = vm::onCategoryClick,
    )
}
```

---

## 2. `*ScreenBody()` — чистый UI

| Правило | |
|---------|--|
| Параметры | **Динамические данные** + **callbacks** |
| ViewModel | ❌ Запрещён |
| Статика | ❌ Не передавать аргументами |

### В параметры ScreenBody — ТОЛЬКО:

- контент из БД / API (списки, id, флаги)
- состояния загрузки / ошибки (`isLoading`, `error`, `isEmpty`)
- callbacks (`onClick`, `onNavigate`, …)

### Внутри ScreenBody (не в аргументах):

- статический текст → `stringResource(R.string.*)` (ресурсы из legacy `:app`)
- плейсхолдеры полей ввода
- подписи кнопок
- статические иконки → `painterResource(R.drawable.*)` / `Icons.*`

```kotlin
@Composable
fun CategoryScreenBody(
    categories: List<CategoryUi>,
    isLoading: Boolean,
    onCategoryClick: (String) -> Unit,
) {
    Text(text = stringResource(R.string.categories_title))  // статика внутри
    // ...
}
```

---

## 3. Mock / Preview — пакет `mockScreens`

Модуль `app-compose`, пакет `mockScreens` — демо-данные для Preview и тестирования UI без VM/БД.

```kotlin
// app-compose/.../mockScreens/HyperScreen.kt
@Composable
fun HyperScreen() {
    val mockItems = listOf(
        CategoryUi(id = "1", name = "News", thumb = null),
        CategoryUi(id = "2", name = "Sports", thumb = null),
    )
    HyperScreenBody(
        categories = mockItems,
        isLoading = false,
        onCategoryClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun HyperScreenPreview() {
    HyperScreen()
}
```

**Именование:** mock-обёртка может называться как экран (`HyperScreen`) или `CategoryScreen` в `mockScreens` — дублирует production-имя для Preview.

---

## 4. Иерархия файлов (на один экран)

```
ui/mobile/category/
  CategoryScreen.kt       # viewModel + ScreenBody
  CategoryScreenBody.kt   # UI + stringResource
  components/
    CategoryCard.kt       # мелкие виджеты

mockScreens/
  CategoryScreen.kt       # MOCK + Preview (опционально отдельный файл)

ui/tv/category/           # зеркало mobile
  CategoryScreen.kt
  CategoryScreenBody.kt
```

---

## 5. Чеклист

- [ ] `*Screen()` без параметров
- [ ] `*ScreenBody()` без ViewModel
- [ ] Статический текст/иконки — внутри ScreenBody через resources
- [ ] Preview через `mockScreens` с mock-данными
- [ ] Пара `ui/mobile` ↔ `ui/tv` для каждого экрана

---

## 6. Антипаттерны

```kotlin
// ❌ Статика в параметрах
fun FooScreenBody(title: String, buttonLabel: String, ...)

// ❌ ViewModel в ScreenBody
fun FooScreenBody(vm: FooViewModel)

// ❌ Параметры у Screen
fun FooScreen(navController: NavController)

// ❌ Preview на ScreenBody с фейковыми строками в параметрах вместо mockScreens
@Preview
fun FooScreenBodyPreview() {
    FooScreenBody(title = "Hello", ...)  // title должен быть stringResource внутри
}
```
