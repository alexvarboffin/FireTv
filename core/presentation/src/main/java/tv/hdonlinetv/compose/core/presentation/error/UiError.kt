package tv.hdonlinetv.compose.core.presentation.error

sealed interface UiError {
    data object Network : UiError
    data object Unknown : UiError
    data object Validation : UiError
}
