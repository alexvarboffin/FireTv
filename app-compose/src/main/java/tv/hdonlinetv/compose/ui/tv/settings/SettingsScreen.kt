package tv.hdonlinetv.compose.ui.tv.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.settings.SettingsViewModel
import tv.hdonlinetv.compose.core.presentation.settings.SettingsViewModelFactory
import tv.hdonlinetv.compose.phone.LocalCategoryRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
) {
    val navController = LocalTvNavController.current
    val context = LocalContext.current
    val repository = LocalSettingsRepository.current
    val categoryRepository = LocalCategoryRepository.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository, categoryRepository),
    )
    val state by viewModel.uiState.collectAsState()
    var choice by remember { mutableStateOf<SettingsChoice?>(null) }
    val layoutOptions = stringArrayResource(R.array.layout_options_tv)
    val mediaPlayerOptions = listOf(
        stringResource(R.string.media_player_JZMediaSystem),
        stringResource(R.string.media_player_jz_aliyun),
        stringResource(R.string.media_player_jz_exo),
        stringResource(R.string.media_player_jz_ijk),
        stringResource(R.string.media_player_jz_vlc),
    )
    val nightLabels = listOf(
        stringResource(R.string.settings_night_mode_on),
        stringResource(R.string.settings_night_mode_off),
    )
    val exit: () -> Unit = onBack ?: {
        navController.popBackStack()
        Unit
    }

    SettingsScreenBody(
        columnsLabel = if (state.gridColumns <= 1) layoutOptions[1] else layoutOptions[0],
        nightModeLabel = if (state.nightMode) nightLabels[0] else nightLabels[1],
        cleanupEmptyCategoriesLabel = if (state.cleanupEmptyCategories) nightLabels[0] else nightLabels[1],
        mediaPlayerLabel = mediaPlayerOptions[state.mediaPlayerOption.coerceIn(0, mediaPlayerOptions.lastIndex)],
        onBack = exit,
        onColumnsClick = { choice = SettingsChoice.Columns },
        onNightModeClick = { choice = SettingsChoice.NightMode },
        onCleanupEmptyCategoriesClick = { choice = SettingsChoice.CleanupEmptyCategories },
        onMediaPlayerClick = { choice = SettingsChoice.MediaPlayer },
    )

    when (choice) {
        SettingsChoice.Columns -> {
            TvChoiceOverlay(
                title = stringResource(R.string.dialog_title_display_channels),
                options = layoutOptions.toList(),
                onDismiss = { choice = null },
                onSelect = { index ->
                    viewModel.setGridColumns(if (index == 0) 4 else 1)
                    choice = null
                },
            )
        }
        SettingsChoice.NightMode -> {
            TvChoiceOverlay(
                title = stringResource(R.string.settings_night_mode),
                options = nightLabels,
                onDismiss = { choice = null },
                onSelect = { index ->
                    val enabled = index == 0
                    viewModel.setNightMode(enabled)
                    AppCompatDelegate.setDefaultNightMode(
                        if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO,
                    )
                    choice = null
                    (context as? Activity)?.recreate()
                },
            )
        }
        SettingsChoice.CleanupEmptyCategories -> {
            TvChoiceOverlay(
                title = stringResource(R.string.settings_cleanup_empty_categories),
                options = nightLabels,
                onDismiss = { choice = null },
                onSelect = { index ->
                    viewModel.setCleanupEmptyCategories(index == 0)
                    choice = null
                },
            )
        }
        SettingsChoice.MediaPlayer -> {
            TvChoiceOverlay(
                title = stringResource(R.string.media_player_title),
                options = mediaPlayerOptions,
                onDismiss = { choice = null },
                onSelect = { index ->
                    viewModel.setMediaPlayerOption(index)
                    choice = null
                },
            )
        }
        null -> Unit
    }
}

private enum class SettingsChoice {
    Columns,
    NightMode,
    CleanupEmptyCategories,
    MediaPlayer,
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreenBody(
    columnsLabel: String,
    nightModeLabel: String,
    cleanupEmptyCategoriesLabel: String,
    mediaPlayerLabel: String,
    onBack: () -> Unit,
    onColumnsClick: () -> Unit,
    onNightModeClick: () -> Unit,
    onCleanupEmptyCategoriesClick: () -> Unit,
    onMediaPlayerClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val drawerFocus = LocalTvDrawerFocusRequester.current
    val leftToDrawer = if (drawerFocus != null) {
        Modifier.focusProperties { left = drawerFocus }
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = leftToDrawer,
        ) {
            Text(text = stringResource(R.string.ok))
        }
        Text(text = stringResource(R.string.menu_settings))
        SettingsRow(
            title = stringResource(R.string.display_channel),
            subtitle = columnsLabel,
            onClick = onColumnsClick,
            modifier = leftToDrawer,
        )
        SettingsRow(
            title = stringResource(R.string.settings_night_mode),
            subtitle = nightModeLabel,
            onClick = onNightModeClick,
            modifier = leftToDrawer,
        )
        SettingsRow(
            title = stringResource(R.string.settings_cleanup_empty_categories),
            subtitle = cleanupEmptyCategoriesLabel,
            onClick = onCleanupEmptyCategoriesClick,
            modifier = leftToDrawer,
        )
        SettingsRow(
            title = stringResource(R.string.media_player_title),
            subtitle = mediaPlayerLabel,
            onClick = onMediaPlayerClick,
            modifier = leftToDrawer,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title)
            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvChoiceOverlay(
    title: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val firstFocus = remember { FocusRequester() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            firstFocus.requestFocus()
        }
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title)
            options.forEachIndexed { index, label ->
                Button(
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
                ) {
                    Text(text = label)
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.ok))
            }
        }
    }
}
