package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R

/**
 * Full-screen loading placeholder (legacy [LoadingDialogFragment]):
 * non-cancelable, holds focus so the form underneath is not usable while a long
 * playlist download/parse runs.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvLoadingOverlay(
    visible: Boolean,
    message: String = stringResource(R.string.loading),
) {
    if (!visible) return

    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            focus.requestFocus()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 40.dp, vertical = 32.dp)
                    .focusRequester(focus)
                    .focusable(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = colors.primary,
                    trackColor = colors.onSurface.copy(alpha = 0.2f),
                )
                Text(text = message)
            }
        }
    }
}
