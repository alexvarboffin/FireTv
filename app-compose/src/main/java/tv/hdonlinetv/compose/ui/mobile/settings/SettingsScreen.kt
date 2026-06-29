package tv.hdonlinetv.compose.ui.mobile.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.settings.SettingsViewModel
import tv.hdonlinetv.compose.core.presentation.settings.SettingsViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.components.LegacySettingsRow
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar
import tv.hdonlinetv.compose.ui.mobile.components.SettingsSingleChoiceBottomSheet
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier

@Composable
fun SettingsScreen() {
    val navController = LocalPhoneNavController.current
    val context = LocalContext.current
    val repository = LocalSettingsRepository.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    var showColumnsDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showNightModeDialog by remember { mutableStateOf(false) }
    var showMediaPlayerDialog by remember { mutableStateOf(false) }
    val layoutOptions = stringArrayResource(R.array.layout_options)
    val mediaPlayerOptions = listOf(
        stringResource(R.string.media_player_JZMediaSystem),
        stringResource(R.string.media_player_jz_aliyun),
        stringResource(R.string.media_player_jz_exo),
        stringResource(R.string.media_player_jz_ijk),
        stringResource(R.string.media_player_jz_vlc),
    )
    val sortOptions = listOf(
        stringResource(R.string.sort_by_name_asc),
        stringResource(R.string.sort_by_name_desc),
        stringResource(R.string.sort_by_id_asc),
        stringResource(R.string.sort_by_id_desc),
    )
    SettingsScreenBody(
        versionName = BuildConfig.VERSION_NAME,
        columnsLabel = if (state.gridColumns <= 1) layoutOptions[1] else layoutOptions[0],
        sortLabel = sortOptions[state.sortOption.coerceIn(0, 3)],
        modeLabel = if (state.detailsMode) {
            stringResource(R.string.option_details_activity)
        } else {
            stringResource(R.string.option_player_activity)
        },
        nightModeLabel = if (state.nightMode) {
            stringResource(R.string.settings_night_mode_on)
        } else {
            stringResource(R.string.settings_night_mode_off)
        },
        mediaPlayerLabel = mediaPlayerOptions[state.mediaPlayerOption.coerceIn(0, mediaPlayerOptions.lastIndex)],
        onBack = { navController.popBackStack() },
        onColumnsClick = { showColumnsDialog = true },
        onSortClick = { showSortDialog = true },
        onModeClick = { showModeDialog = true },
        onNightModeClick = { showNightModeDialog = true },
        onMediaPlayerClick = { showMediaPlayerDialog = true },
        onOpenTutorial = { navController.navigate(Routes.Tutorial.route) },
        onOpenPrivacy = {
            navController.navigate(
                Routes.InfoWeb.build(
                    url = context.getString(R.string.privacy_url),
                    title = context.getString(R.string.policy_privacy),
                ),
            )
        },
    )
    if (showColumnsDialog) {
        AlertDialog(
            onDismissRequest = { showColumnsDialog = false },
            title = { Text(stringResource(R.string.dialog_title_display_channels)) },
            text = {
                Column {
                    layoutOptions.forEachIndexed { index, label ->
                        TextButton(onClick = {
                            viewModel.setGridColumns(if (index == 0) 3 else 1)
                            showColumnsDialog = false
                        }) { Text(label) }
                    }
                }
            },
            confirmButton = {},
        )
    }
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text(stringResource(R.string.settings_sort)) },
            text = {
                Column {
                    sortOptions.forEachIndexed { index, label ->
                        TextButton(onClick = {
                            viewModel.setSortOption(index)
                            showSortDialog = false
                        }) { Text(label) }
                    }
                }
            },
            confirmButton = {},
        )
    }
    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = { Text(stringResource(R.string.settings_open_mode)) },
            text = {
                Column {
                    TextButton(onClick = {
                        viewModel.setDetailsMode(true)
                        showModeDialog = false
                    }) { Text(stringResource(R.string.option_details_activity)) }
                    TextButton(onClick = {
                        viewModel.setDetailsMode(false)
                        showModeDialog = false
                    }) { Text(stringResource(R.string.option_player_activity)) }
                }
            },
            confirmButton = {},
        )
    }
    if (showNightModeDialog) {
        val activity = context as? android.app.Activity
        AlertDialog(
            onDismissRequest = { showNightModeDialog = false },
            title = { Text(stringResource(R.string.settings_night_mode)) },
            text = {
                Column {
                    TextButton(onClick = {
                        viewModel.setNightMode(true)
                        NightModeApplier.apply(true)
                        showNightModeDialog = false
                        activity?.recreate()
                    }) { Text(stringResource(R.string.settings_night_mode_on)) }
                    TextButton(onClick = {
                        viewModel.setNightMode(false)
                        NightModeApplier.apply(false)
                        showNightModeDialog = false
                        activity?.recreate()
                    }) { Text(stringResource(R.string.settings_night_mode_off)) }
                }
            },
            confirmButton = {},
        )
    }
    if (showMediaPlayerDialog) {
        SettingsSingleChoiceBottomSheet(
            visible = showMediaPlayerDialog,
            title = stringResource(R.string.media_player_title),
            options = mediaPlayerOptions,
            selectedIndex = state.mediaPlayerOption.coerceIn(0, mediaPlayerOptions.lastIndex),
            onDismiss = { showMediaPlayerDialog = false },
            onSelect = { viewModel.setMediaPlayerOption(it) },
        )
    }
}

@Composable
fun SettingsScreenBody(
    versionName: String,
    columnsLabel: String,
    sortLabel: String,
    modeLabel: String,
    nightModeLabel: String,
    mediaPlayerLabel: String,
    onBack: () -> Unit,
    onColumnsClick: () -> Unit,
    onSortClick: () -> Unit,
    onModeClick: () -> Unit,
    onNightModeClick: () -> Unit,
    onMediaPlayerClick: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = stringResource(R.string.menu_settings),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colorResource(R.color.black))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorResource(R.color.bgMain)),
        ) {
            LegacySettingsRow(
                iconRes = R.drawable.ic_info,
                title = stringResource(R.string.version_label),
                trailing = versionName,
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_tv_icon,
                title = stringResource(R.string.display_channel),
                subtitle = columnsLabel,
                modifier = Modifier.clickable(onClick = onColumnsClick),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_tv_icon,
                title = stringResource(R.string.settings_sort),
                subtitle = sortLabel,
                modifier = Modifier.clickable(onClick = onSortClick),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_actions_settings,
                title = stringResource(R.string.settings_open_mode),
                subtitle = modeLabel,
                modifier = Modifier.clickable(onClick = onModeClick),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_nightmode,
                title = stringResource(R.string.settings_night_mode),
                subtitle = nightModeLabel,
                modifier = Modifier.clickable(onClick = onNightModeClick),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_tv_icon,
                title = stringResource(R.string.media_player_title),
                subtitle = mediaPlayerLabel,
                modifier = Modifier.clickable(onClick = onMediaPlayerClick),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_info,
                title = stringResource(R.string.menu_tutorial),
                modifier = Modifier.clickable(onClick = onOpenTutorial),
            )
            LegacySettingsRow(
                iconRes = R.drawable.ic_privacy,
                title = stringResource(R.string.menu_privacy),
                modifier = Modifier.clickable(onClick = onOpenPrivacy),
            )
        }
    }
}
