package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.player.tracks.NewClickableChipColor
import tv.hdonlinetv.compose.player.tracks.PlayerTracksController
import tv.hdonlinetv.compose.player.tracks.PlayerTracksUi

const val TRACKS_PAGE_MAIN = 0
const val TRACKS_PAGE_AUDIO = 1
const val TRACKS_PAGE_TEXT = 2
const val TRACKS_PAGE_ENGINE = 3

/** Gear entry in the TV player bottom row (new clickable → amber NewClickableChip colouring). */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTracksGearChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = stringResource(R.string.player_tracks_settings),
            modifier = Modifier.padding(12.dp).size(24.dp),
        )
    }
}

/**
 * YouTube-style "Audio & subtitles" right side panel built on [TvSidePanel].
 * Main page: rows "Audio — current" and "Subtitles — Off/lang"; each opens a nested list.
 * Back is handled by the caller (one level up, then close) via [page] / [onPageChange].
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTracksPanel(
    visible: Boolean,
    tracks: PlayerTracksUi,
    page: Int,
    onPageChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    engineOptions: List<String> = emptyList(),
    engineSelected: Int = 0,
    onEngineSelect: (Int) -> Unit = {},
) {
    val frEngineRow = remember { FocusRequester() }
    val frAudioRow = remember { FocusRequester() }
    val frTextRow = remember { FocusRequester() }
    val frFirstItem = remember { FocusRequester() }
    val lastPage = remember { intArrayOf(TRACKS_PAGE_MAIN) }
    val offLabel = stringResource(R.string.player_subtitles_off)

    LaunchedEffect(visible, page) {
        if (!visible) return@LaunchedEffect
        delay(60)
        val target = when (page) {
            TRACKS_PAGE_MAIN -> when (lastPage[0]) {
                TRACKS_PAGE_TEXT -> frTextRow
                TRACKS_PAGE_AUDIO -> frAudioRow
                else -> frEngineRow
            }
            else -> frFirstItem
        }
        runCatching { target.requestFocus() }
        lastPage[0] = page
    }

    TvSidePanel(visible = visible, side = TvSidePanelSide.End, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(440.dp)
                .background(Color.Black.copy(alpha = 0.78f))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                // Trap D-pad focus inside the panel (no leaking to player chrome behind it).
                .focusProperties { onExit = { if (visible) cancelFocusChange() } }
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val title = when (page) {
                TRACKS_PAGE_AUDIO -> stringResource(R.string.player_audio_track)
                TRACKS_PAGE_TEXT -> stringResource(R.string.player_subtitles)
                TRACKS_PAGE_ENGINE -> stringResource(R.string.tv_settings_player_row)
                else -> stringResource(R.string.tv_settings_panel_title) // was player_tracks_settings
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
            when (page) {
                TRACKS_PAGE_MAIN -> {
                    if (engineOptions.isNotEmpty()) {
                        TracksPanelRow(
                            title = stringResource(R.string.tv_settings_player_row), // was media_player_title
                            value = engineOptions.getOrNull(engineSelected) ?: "—",
                            chevron = true,
                            onClick = { onPageChange(TRACKS_PAGE_ENGINE) },
                            modifier = Modifier.focusRequester(frEngineRow),
                        )
                    }
                    if (tracks.showAudioButton) {
                        TracksPanelRow(
                            title = stringResource(R.string.player_audio_track),
                            value = tracks.audio.firstOrNull { it.selected }?.label ?: "—",
                            chevron = true,
                            onClick = { onPageChange(TRACKS_PAGE_AUDIO) },
                            modifier = Modifier.focusRequester(frAudioRow),
                        )
                    }
                    if (tracks.showSubtitlesButton) {
                        TracksPanelRow(
                            title = stringResource(R.string.player_subtitles),
                            value = tracks.text.firstOrNull { it.selected }?.label ?: offLabel,
                            chevron = true,
                            onClick = { onPageChange(TRACKS_PAGE_TEXT) },
                            modifier = Modifier.focusRequester(frTextRow),
                        )
                    }
                }
                TRACKS_PAGE_ENGINE -> {
                    engineOptions.forEachIndexed { i, name ->
                        TracksPanelRow(
                            title = name,
                            checked = i == engineSelected,
                            onClick = {
                                onEngineSelect(i)
                                onPageChange(TRACKS_PAGE_MAIN)
                            },
                            modifier = if (i == engineSelected) Modifier.focusRequester(frFirstItem) else Modifier,
                        )
                    }
                }
                TRACKS_PAGE_AUDIO -> {
                    val sel = tracks.audio.indexOfFirst { it.selected }.coerceAtLeast(0)
                    tracks.audio.forEachIndexed { i, opt ->
                        TracksPanelRow(
                            title = opt.label,
                            checked = opt.selected,
                            onClick = {
                                PlayerTracksController.selectAudio(opt)
                                onPageChange(TRACKS_PAGE_MAIN)
                            },
                            modifier = if (i == sel) Modifier.focusRequester(frFirstItem) else Modifier,
                        )
                    }
                }
                TRACKS_PAGE_TEXT -> {
                    val sel = tracks.text.indexOfFirst { it.selected } + 1
                    TracksPanelRow(
                        title = offLabel,
                        checked = tracks.subtitlesOff,
                        onClick = {
                            PlayerTracksController.disableSubtitles()
                            onPageChange(TRACKS_PAGE_MAIN)
                        },
                        modifier = if (sel == 0) Modifier.focusRequester(frFirstItem) else Modifier,
                    )
                    tracks.text.forEachIndexed { i, opt ->
                        TracksPanelRow(
                            title = opt.label,
                            checked = opt.selected,
                            onClick = {
                                PlayerTracksController.selectText(opt)
                                onPageChange(TRACKS_PAGE_MAIN)
                            },
                            modifier = if (i + 1 == sel) Modifier.focusRequester(frFirstItem) else Modifier,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TracksPanelRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    chevron: Boolean = false,
    checked: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        // Old amber "new clickable" marker styling:
        // colors = ClickableSurfaceDefaults.colors(
        //     containerColor = Color.White.copy(alpha = 0.08f),
        //     contentColor = Color.White,
        //     focusedContainerColor = NewClickableChipColor,
        //     focusedContentColor = Color.Black,
        // ),
        // Same focus styling as the EPG / channel list rows (PlayerChannelSheet).
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (checked) {
                androidx.tv.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            } else {
                Color.White.copy(alpha = 0.10f)
            },
            contentColor = Color.White,
            focusedContainerColor = androidx.tv.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
            focusedContentColor = Color.White,
            pressedContainerColor = androidx.tv.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!chevron) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (checked) androidx.tv.material3.LocalContentColor.current else Color.Transparent,
                )
            }
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(text = value, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            if (chevron) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
