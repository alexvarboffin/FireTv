package tv.hdonlinetv.compose.ui.tv.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * TV text field: D-pad navigates the [Surface]; OK enters edit mode (IME + cursor);
 * Back / Up / Down leave edit and return focus to the surface so list navigation continues.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvEditableField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    var editing by remember { mutableStateOf(false) }
    val surfaceFocus = remember { FocusRequester() }
    val fieldFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    fun exitEdit() {
        if (!editing) return
        editing = false
        keyboard?.hide()
        surfaceFocus.requestFocus()
    }

    BackHandler(enabled = editing, onBack = ::exitEdit)

    LaunchedEffect(editing) {
        if (editing) {
            fieldFocus.requestFocus()
            keyboard?.show()
        } else {
            keyboard?.hide()
        }
    }

    Surface(
        onClick = { editing = true },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(surfaceFocus),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isError) {
                colors.primary.copy(alpha = 0.25f)
            } else {
                colors.surface
            },
            contentColor = colors.onSurface,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
        ),
    ) {
        val contentColor = LocalContentColor.current
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            readOnly = !editing,
            cursorBrush = SolidColor(
                if (editing) colors.primary else Color.Transparent,
            ),
            textStyle = TextStyle(color = contentColor, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(fieldFocus)
                .focusProperties { canFocus = editing }
                .onPreviewKeyEvent { event ->
                    if (!editing || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionUp, Key.DirectionDown -> {
                            exitEdit()
                            true
                        }
                        else -> false
                    }
                },
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = contentColor.copy(alpha = 0.5f),
                    )
                }
                inner()
            },
        )
    }
}
