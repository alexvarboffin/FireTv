package tv.hdonlinetv.compose.ui.tv.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * TV text field: D-pad focus activates editing (cursor + IME) immediately;
 * Up / Down move to the next focusable (or [upFocus]/[downFocus] via requestFocus);
 * Back clears focus and hides the IME.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvEditableField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    upFocus: FocusRequester? = null,
    downFocus: FocusRequester? = null,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }

    BackHandler(enabled = focused) {
        keyboard?.hide()
        focusManager.clearFocus()
    }

    val containerColor = when {
        isError && !focused -> colors.primary.copy(alpha = 0.25f)
        focused -> colors.primary
        else -> colors.surface
    }
    val contentColor = if (focused) colors.onPrimary else colors.onSurface

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(contentColor),
        textStyle = TextStyle(color = contentColor, fontSize = 16.sp),
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) {
                    keyboard?.show()
                } else {
                    keyboard?.hide()
                }
            }
            .onPreviewKeyEvent { event ->
                if (!focused || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionUp -> {
                        if (upFocus != null) {
                            upFocus.requestFocus()
                            true
                        } else {
                            focusManager.moveFocus(FocusDirection.Up)
                        }
                    }
                    Key.DirectionDown -> {
                        if (downFocus != null) {
                            downFocus.requestFocus()
                            true
                        } else {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
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
