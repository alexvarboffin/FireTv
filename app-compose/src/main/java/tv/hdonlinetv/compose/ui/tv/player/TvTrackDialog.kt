package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.player.tracks.NewClickableChipColor

/** TV chip button in the player bottom row (NewClickableChip colouring). */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvNewClickableChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(24.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = NewClickableChipColor.copy(alpha = 0.85f),
            contentColor = Color.Black,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black,
        ),
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    }
}

/**
 * D-pad list of tracks. Back (dialog dismissOnBackPress) closes it; caller restores focus
 * to the opening chip in [onDismiss].
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTrackDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
        Column(
            modifier = Modifier
                .width(520.dp)
                .heightIn(max = 420.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            val focusIndex = selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))
            options.forEachIndexed { index, label ->
                Button(
                    onClick = { onSelect(index) },
                    colors = ButtonDefaults.colors(
                        containerColor = NewClickableChipColor.copy(alpha = 0.25f),
                        focusedContainerColor = NewClickableChipColor,
                        focusedContentColor = Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (index == focusIndex) Modifier.focusRequester(firstFocus) else Modifier),
                ) {
                    Text(text = if (index == selectedIndex) "✓ $label" else label)
                }
            }
        }
    }
}
