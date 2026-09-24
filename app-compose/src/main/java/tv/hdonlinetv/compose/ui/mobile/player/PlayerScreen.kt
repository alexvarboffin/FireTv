package tv.hdonlinetv.compose.ui.mobile.player

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.jzvd.Jzvd
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModel
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModelFactory
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.toWire
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.player.CastMediaRouteButton
import tv.hdonlinetv.compose.player.HandleJzPlayerFullscreen
import tv.hdonlinetv.compose.player.HandlePlayerScreenSystemUi
import tv.hdonlinetv.compose.player.JZVideoPlayerNew
import tv.hdonlinetv.compose.player.LegacyCastController
import tv.hdonlinetv.compose.player.LegacyJzPlayerRelease
import tv.hdonlinetv.compose.player.LegacyJzPlayerSetup
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar
import tv.hdonlinetv.compose.ui.mobile.components.SettingsSingleChoiceBottomSheet

private data class PlayerPlaybackKey(
    val id: Long,
    val link: String?,
    val name: String,
    val cover: String?,
    val extUserAgent: String?,
    val extReferer: String?,
    val ua: String?,
)

private fun ChannelUi.playbackKey() = PlayerPlaybackKey(
    id = id,
    link = link,
    name = name,
    cover = cover,
    extUserAgent = extUserAgent,
    extReferer = extReferer,
    ua = ua,
)

@Composable
fun PlayerScreen() {
    val navController = LocalPhoneNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val channelId = args?.getLong(Routes.Player.ARG_CHANNEL_ID) ?: 0L
    val streamUrl = args?.getString(Routes.Player.ARG_STREAM_URL).orEmpty()
    val streamTitle = args?.getString(Routes.Player.ARG_STREAM_TITLE).orEmpty()
    val browseScope = PlayerBrowseScope.parse(
        type = args?.getString(Routes.Player.ARG_SCOPE),
        key = args?.getString(Routes.Player.ARG_SCOPE_KEY),
    )
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    var mediaPlayerOption by remember {
        mutableIntStateOf(settingsRepository.getSettings().mediaPlayerOption)
    }
    val viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            repository = repository,
            channelId = channelId,
            browseScope = browseScope.toWire(),
            directStreamUrl = streamUrl.takeIf { it.isNotBlank() },
            directStreamTitle = streamTitle.takeIf { it.isNotBlank() },
        ),
    )
    val state by viewModel.uiState.collectAsState()
    PlayerScreenBody(
        channel = state.channel,
        siblings = state.siblings,
        isLoading = state.isLoading,
        mediaPlayerOption = mediaPlayerOption,
        onBack = { navController.popBackStack() },
        onToggleFavorite = { viewModel.toggleFavorite() },
        onSelectChannel = viewModel::selectChannel,
        onPreviousChannel = viewModel::previousChannel,
        onNextChannel = viewModel::nextChannel,
        onMediaPlayerOptionChange = { option ->
            settingsRepository.setMediaPlayerOption(option)
            mediaPlayerOption = option
        },
    )
}

@Composable
fun PlayerScreenBody(
    channel: ChannelUi?,
    siblings: List<ChannelUi>,
    isLoading: Boolean,
    mediaPlayerOption: Int,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectChannel: (ChannelUi) -> Unit,
    onPreviousChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onMediaPlayerOptionChange: (Int) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showMediaPlayerSheet by remember { mutableStateOf(false) }
    var showChannelSheet by remember { mutableStateOf(false) }
    val canZap = siblings.size > 1
    val mediaPlayerOptions = listOf(
        stringResource(R.string.media_player_JZMediaSystem),
        stringResource(R.string.media_player_jz_aliyun),
        stringResource(R.string.media_player_jz_exo),
        stringResource(R.string.media_player_jz_ijk),
        stringResource(R.string.media_player_jz_vlc),
    )
    val playbackKey = channel?.playbackKey()
    val setup = remember(playbackKey) { channel?.let { LegacyJzPlayerSetup.prepare(it) } }
    var playerRef by remember { mutableStateOf<JZVideoPlayerNew?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }
    val castController = remember(playbackKey, setup) {
        if (channel != null && setup != null) {
            LegacyCastController(context.applicationContext, channel, setup)
        } else {
            null
        }
    }

    val exitPlayer = {
        if (!isExiting) {
            isExiting = true
            castController?.onPause()
            LegacyJzPlayerRelease.stopAndRelease(playerRef)
            onBack()
        }
    }

    BackHandler {
        if (showChannelSheet) {
            showChannelSheet = false
        } else if (!Jzvd.backPress()) {
            exitPlayer()
        }
    }

    HandlePlayerScreenSystemUi()

    HandleJzPlayerFullscreen(
        isFullscreen = isFullscreen,
        onFullscreenChange = { isFullscreen = it },
    )

    DisposableEffect(lifecycleOwner, castController, isExiting) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (!isExiting) {
                        Jzvd.goOnPlayOnPause()
                    }
                    castController?.onPause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isExiting) {
                        castController?.onResume()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isExiting) {
                castController?.onPause()
                LegacyJzPlayerRelease.stopAndRelease(playerRef)
            }
        }
    }

    LaunchedEffect(playbackKey, mediaPlayerOption, setup, playerRef, isExiting) {
        if (isExiting) return@LaunchedEffect
        val player = playerRef ?: return@LaunchedEffect
        val ch = channel ?: return@LaunchedEffect
        val playerSetup = setup ?: return@LaunchedEffect
        LegacyJzPlayerSetup.apply(player, ch, playerSetup, mediaPlayerOption)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(colorResource(R.color.black)),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colorResource(R.color.black),
            // Keep bottom controls (seek / buttons) above gesture/nav bar.
            // Top insets come from TopAppBar; do not zero all insets.
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                if (!isFullscreen) {
                    // Insets from Material3 TopAppBar — no manual status spacer (avoids double gap).
                    LegacyTopAppBar(
                        title = channel?.name.orEmpty(),
                        backgroundColor = Color.Transparent,
                        titleColor = colorResource(R.color.white),
                        navigationIcon = {
                            IconButton(onClick = exitPlayer) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_baseline_arrow_white),
                                    contentDescription = null,
                                    tint = colorResource(R.color.white),
                                )
                            }
                        },
                        actions = {
                            if (canZap) {
                                IconButton(onClick = { showChannelSheet = true }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = stringResource(R.string.menu_home),
                                        tint = colorResource(R.color.white),
                                    )
                                }
                                IconButton(onClick = onPreviousChannel) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipPrevious,
                                        contentDescription = null,
                                        tint = colorResource(R.color.white),
                                    )
                                }
                                IconButton(onClick = onNextChannel) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = null,
                                        tint = colorResource(R.color.white),
                                    )
                                }
                            }
                            IconButton(onClick = { showMediaPlayerSheet = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_actions_settings),
                                    contentDescription = stringResource(R.string.media_player_title),
                                    tint = colorResource(R.color.white),
                                )
                            }
                            if (channel != null && channel.id > 0) {
                                IconButton(onClick = onToggleFavorite) {
                                    Icon(
                                        painter = painterResource(
                                            if (channel.isFavorite) {
                                                R.drawable.ic_favorite_fill_red
                                            } else {
                                                R.drawable.ic_favorite_border_red
                                            },
                                        ),
                                        contentDescription = null,
                                        tint = colorResource(R.color.white),
                                    )
                                }
                            }
                            CastMediaRouteButton()
                        },
                    )
                } else null
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .then(
                        if (isFullscreen) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.padding(padding).fillMaxSize()
                        },
                    )
                    .background(colorResource(R.color.black)),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = colorResource(R.color.white),
                        )
                    }
                    channel == null || channel.link.isNullOrBlank() || setup == null -> {
                        Text(
                            text = stringResource(R.string.no_item),
                            color = colorResource(R.color.white),
                        )
                    }
                    else -> {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                JZVideoPlayerNew(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                    )
                                }
                            },
                            update = { player ->
                                playerRef = player
                                player.onFullscreenChange = { fullscreen ->
                                    isFullscreen = fullscreen
                                }
                            },
                            onRelease = { player ->
                                if (!isExiting) {
                                    LegacyJzPlayerRelease.stopAndRelease(player)
                                }
                            },
                        )
                    }
                }

                // Fullscreen: touch CH± on the sides (top bar is hidden).
                if (canZap && isFullscreen && channel != null && !channel.link.isNullOrBlank()) {
                    IconButton(
                        onClick = onPreviousChannel,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.35f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    IconButton(
                        onClick = onNextChannel,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.35f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    IconButton(
                        onClick = { showChannelSheet = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.35f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = stringResource(R.string.menu_home),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }

    SettingsSingleChoiceBottomSheet(
        visible = showMediaPlayerSheet,
        title = stringResource(R.string.media_player_title),
        options = mediaPlayerOptions,
        selectedIndex = mediaPlayerOption.coerceIn(0, mediaPlayerOptions.lastIndex),
        onDismiss = { showMediaPlayerSheet = false },
        onSelect = onMediaPlayerOptionChange,
    )

    PlayerChannelSheet(
        visible = showChannelSheet,
        channels = siblings,
        currentChannelId = channel?.id ?: 0L,
        onChannelSelect = onSelectChannel,
        onDismiss = { showChannelSheet = false },
    )
}
