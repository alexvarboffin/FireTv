package tv.hdonlinetv.compose.ui.tv.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import cn.jzvd.Jzvd
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModel
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModelFactory
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.toWire
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.player.JZVideoPlayerNew
import tv.hdonlinetv.compose.player.LegacyJzPlayerRelease
import tv.hdonlinetv.compose.player.LegacyJzPlayerSetup
import tv.hdonlinetv.compose.tv.LocalTvNavController

private const val CONTROL_VISIBILITY_TIMEOUT_MS = 5_000L

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
    val navController = LocalTvNavController.current
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
        onNextChannel = viewModel::nextChannel,
        onPreviousChannel = viewModel::previousChannel,
        onMediaPlayerOptionChange = { option ->
            settingsRepository.setMediaPlayerOption(option)
            mediaPlayerOption = option
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreenBody(
    channel: ChannelUi?,
    siblings: List<ChannelUi>,
    isLoading: Boolean,
    mediaPlayerOption: Int,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectChannel: (ChannelUi) -> Unit,
    onNextChannel: () -> Unit,
    onPreviousChannel: () -> Unit,
    onMediaPlayerOptionChange: (Int) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showMediaPlayerDialog by remember { mutableStateOf(false) }
    var channelSheetVisible by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var hideControlsJob by remember { mutableStateOf<Job?>(null) }
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
    var isExiting by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var scrubProgress by remember { mutableStateOf<Float?>(null) }
    var jzState by remember { mutableIntStateOf(Jzvd.STATE_IDLE) }
    var isJzFullscreen by remember { mutableStateOf(false) }
    val rootFocus = remember { FocusRequester() }
    val (frFavorite, frClose, frPlay, frProgress, frSettings, frFullscreen) = remember {
        FocusRequester.createRefs()
    }
    // DEBUG: keep JZ XML chrome visible together with Compose overlay (parity check).
    val showJzXmlChrome = BuildConfig.DEBUG
    val hideJzChrome = !showJzXmlChrome

    val displayedProgress = scrubProgress
        ?: if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    // XML @id/loading — spinner while preparing.
    val isPreparing = jzState == Jzvd.STATE_PREPARING ||
        jzState == Jzvd.STATE_PREPARING_PLAYING ||
        jzState == Jzvd.STATE_PREPARING_CHANGE_URL ||
        isLoading

    // Play/pause is hidden while preparing — never leave focus pointing at missing frPlay.
    val canFavorite = channel != null && channel.id > 0
    val chromePrimaryFocus = when {
        isPreparing -> if (canFavorite) frFavorite else frClose
        else -> frPlay
    }
    val chromeDownFromTop = if (isPreparing) {
        if (durationMs > 0L) frProgress else frSettings
    } else {
        frPlay
    }
    val chromeUpFromBottom = if (isPreparing) chromePrimaryFocus else frPlay

    // Cinema PlayerOverlayPhone: PlayCircleOutline / PauseCircleOutline (64dp).
    // Replay uses Refresh for complete/error (JZ jz_click_replay).
    val centerControl: Pair<ImageVector, String> = when (jzState) {
        Jzvd.STATE_PLAYING -> Icons.Rounded.PauseCircleOutline to "Pause"
        Jzvd.STATE_AUTO_COMPLETE, Jzvd.STATE_ERROR -> Icons.Filled.Refresh to stringResource(R.string.refresh)
        else -> Icons.Rounded.PlayCircleOutline to "Play"
    }

    // Cinema TvUI_VideoLayout: any interaction restarts the auto-hide countdown.
    fun resetHideTimer() {
        lastInteractionTime = System.currentTimeMillis()
        hideControlsJob?.cancel()
        if (!controlsVisible || showMediaPlayerDialog || channelSheetVisible) return
        hideControlsJob = scope.launch {
            delay(CONTROL_VISIBILITY_TIMEOUT_MS)
            if (System.currentTimeMillis() - lastInteractionTime >= CONTROL_VISIBILITY_TIMEOUT_MS) {
                controlsVisible = false
            }
        }
    }

    fun onCenterControlClick() {
        resetHideTimer()
        playerRef?.performStartButtonAction()
        playerRef?.let { jzState = it.state }
    }

    fun toggleFullscreen() {
        resetHideTimer()
        val player = playerRef ?: return
        if (player.screen == Jzvd.SCREEN_FULLSCREEN) {
            player.gotoNormalScreen()
        } else {
            player.gotoFullscreen()
        }
        isJzFullscreen = player.screen == Jzvd.SCREEN_FULLSCREEN
    }

    val exitPlayer = {
        if (!isExiting) {
            isExiting = true
            hideControlsJob?.cancel()
            LegacyJzPlayerRelease.stopAndRelease(playerRef)
            onBack()
        }
    }

    BackHandler {
        when {
            showMediaPlayerDialog -> showMediaPlayerDialog = false
            channelSheetVisible -> channelSheetVisible = false
            else -> exitPlayer()
        }
    }

    HandleTvPlayerImmersiveUi()

    DisposableEffect(lifecycleOwner, isExiting) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && !isExiting) {
                Jzvd.goOnPlayOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            hideControlsJob?.cancel()
            if (!isExiting) {
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

    // Only steal focus to chrome when it becomes visible — not on every
    // playerRef/state churn (retry would yank focus off Close/Settings).
    // While preparing, play is not composed → focus Favorite / Channels / Close.
    LaunchedEffect(
        controlsVisible,
        showMediaPlayerDialog,
        channelSheetVisible,
        isPreparing,
        canFavorite,
    ) {
        if (channelSheetVisible) return@LaunchedEffect
        if (controlsVisible && !showMediaPlayerDialog) {
            resetHideTimer()
            chromePrimaryFocus.requestFocus()
        } else {
            hideControlsJob?.cancel()
            if (!controlsVisible) {
                rootFocus.requestFocus()
            }
        }
    }

    LaunchedEffect(showJzXmlChrome, playerRef) {
        if (showJzXmlChrome && playerRef != null) {
            playerRef?.forceShowNativeChromeForDebug()
        }
    }

    fun openChannelSheet() {
        if (siblings.isEmpty()) return
        channelSheetVisible = true
        controlsVisible = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocus)
            .focusProperties {
                // Cinema root: when chrome visible, D-pad enters primary control.
                left = FocusRequester.Cancel
                up = FocusRequester.Cancel
                right = if (controlsVisible && !channelSheetVisible) {
                    chromePrimaryFocus
                } else {
                    FocusRequester.Cancel
                }
                down = if (controlsVisible && !channelSheetVisible) {
                    chromePrimaryFocus
                } else {
                    FocusRequester.Cancel
                }
            }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                if (keyEvent.key == Key.Back) return@onKeyEvent false

                // Sheet open: Home / Left / Right close it (click does not).
                if (channelSheetVisible) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.DirectionRight,
                        Key.Home, Key.MoveHome, Key.Escape,
                        -> {
                            channelSheetVisible = false
                            return@onKeyEvent true
                        }
                        else -> Unit
                    }
                }

                // Hardware CH+/CH− (and PageUp/PageDown fallbacks on some remotes).
                when (keyEvent.key) {
                    Key.ChannelUp, Key.PageUp -> {
                        onNextChannel()
                        controlsVisible = true
                        resetHideTimer()
                        return@onKeyEvent true
                    }
                    Key.ChannelDown, Key.PageDown -> {
                        onPreviousChannel()
                        controlsVisible = true
                        resetHideTimer()
                        return@onKeyEvent true
                    }
                    Key.DirectionLeft, Key.Menu -> {
                        if (!channelSheetVisible && !showMediaPlayerDialog && siblings.isNotEmpty()) {
                            openChannelSheet()
                            return@onKeyEvent true
                        }
                    }
                    else -> Unit
                }

                if (channelSheetVisible) return@onKeyEvent false

                resetHideTimer()
                if (!controlsVisible && !showMediaPlayerDialog) {
                    controlsVisible = true
                    return@onKeyEvent true
                }
                false
            },
    ) {
        when {
            isLoading -> {
                // Spinner drawn below via isPreparing/isLoading overlay.
            }
            channel == null || channel.link.isNullOrBlank() || setup == null -> {
                Text(
                    text = stringResource(R.string.no_item),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
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
                            suppressNativeChrome = hideJzChrome
                        }
                    },
                    update = { player ->
                        playerRef = player
                        player.suppressNativeChrome = hideJzChrome
                        if (showJzXmlChrome) {
                            player.forceShowNativeChromeForDebug()
                        }
                        player.onPlaybackProgress = { position, duration ->
                            if (scrubProgress == null) {
                                positionMs = position
                            }
                            durationMs = duration
                        }
                        player.onPlaybackStateChanged = { jzState = it }
                        player.onFullscreenChange = { fullscreen ->
                            isJzFullscreen = fullscreen
                        }
                        jzState = player.state
                        isJzFullscreen = player.screen == Jzvd.SCREEN_FULLSCREEN
                    },
                    onRelease = { player ->
                        player.onPlaybackProgress = null
                        player.onPlaybackStateChanged = null
                        player.onFullscreenChange = null
                        if (!isExiting) {
                            LegacyJzPlayerRelease.stopAndRelease(player)
                        }
                    },
                )
            }
        }

        // XML @id/loading — always on top of video while buffering/preparing.
        if (isPreparing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp),
                color = Color.White,
                strokeWidth = 4.dp,
            )
        }

        if (controlsVisible && !showMediaPlayerDialog) {
            // Cinema layout: top title+fav+close | center refresh | bottom seek+settings
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = channel?.name.orEmpty(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Channel sheet opens via Left / Menu — no redundant list icon.
                        if (canFavorite) {
                            val favChannel = channel!!
                            TvPlayerIconButton(
                                onClick = {
                                    resetHideTimer()
                                    onToggleFavorite()
                                },
                                modifier = Modifier
                                    .focusRequester(frFavorite)
                                    .focusProperties {
                                        left = frClose
                                        right = frClose
                                        down = chromeDownFromTop
                                        up = frSettings
                                    }
                                    .onFocusChanged { if (it.isFocused) resetHideTimer() },
                            ) {
                                Icon(
                                    imageVector = if (favChannel.isFavorite) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = stringResource(
                                        if (favChannel.isFavorite) R.string.remove_fav else R.string.add_fav,
                                    ),
                                    tint = Color.White,
                                )
                            }
                        }
                        TvPlayerIconButton(
                            onClick = {
                                resetHideTimer()
                                exitPlayer()
                            },
                            modifier = Modifier
                                .focusRequester(frClose)
                                .focusProperties {
                                    left = if (canFavorite) frFavorite else frClose
                                    right = if (canFavorite) frFavorite else frClose
                                    down = chromeDownFromTop
                                    up = frSettings
                                }
                                .onFocusChanged { if (it.isFocused) resetHideTimer() },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cancel),
                                tint = Color.White,
                            )
                        }
                    }
                }

                // XML @id/start — hide while loading spinner is shown.
                if (!isPreparing) {
                    TvPlayerIconButton(
                        onClick = { onCenterControlClick() },
                        size = 64.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .focusRequester(frPlay)
                            .focusProperties {
                                up = if (channel != null && channel.id > 0) frFavorite else frClose
                                down = if (durationMs > 0L) frProgress else frSettings
                                left = FocusRequester.Cancel
                                right = FocusRequester.Cancel
                            }
                            .onFocusChanged { if (it.isFocused) resetHideTimer() },
                    ) {
                        Icon(
                            imageVector = centerControl.first,
                            contentDescription = centerControl.second,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        // Leave room for JZ XML bottom seek bar when DEBUG shows both.
                        .padding(bottom = if (showJzXmlChrome) 52.dp else 0.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(top = 8.dp, bottom = 16.dp),
                ) {
                    TvPlayerProgressBar(
                        progress = displayedProgress,
                        durationMs = durationMs,
                        onProgressChange = { fraction ->
                            scrubProgress = fraction
                            resetHideTimer()
                            val player = playerRef
                            if (player != null && durationMs > 0L) {
                                player.seekToMs((fraction * durationMs).toLong())
                            }
                            scope.launch {
                                delay(500)
                                scrubProgress = null
                            }
                        },
                        onUserInteraction = { resetHideTimer() },
                        focusRequester = frProgress,
                        upFocus = chromeUpFromBottom,
                        downFocus = frSettings,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TvPlayerIconButton(
                            onClick = {
                                resetHideTimer()
                                showMediaPlayerDialog = true
                            },
                            modifier = Modifier
                                .focusRequester(frSettings)
                                .focusProperties {
                                    up = if (durationMs > 0L) frProgress else chromeUpFromBottom
                                    down = chromePrimaryFocus
                                    left = FocusRequester.Cancel
                                    right = FocusRequester.Cancel
                                }
                                .onFocusChanged { if (it.isFocused) resetHideTimer() },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.media_player_title),
                                tint = Color.White,
                            )
                        }
                        // TV is already fullscreen (landscape + immersive).
                        // JZ gotoFullscreen() reparents to DecorView and leaves Compose overlay behind — skip.
                        // TvPlayerIconButton(
                        //     onClick = { toggleFullscreen() },
                        //     modifier = Modifier
                        //         .focusRequester(frFullscreen)
                        //         .focusProperties {
                        //             up = if (durationMs > 0L) frProgress else frPlay
                        //             down = if (channel != null && channel.id > 0) frFavorite else frClose
                        //             left = frSettings
                        //             right = FocusRequester.Cancel
                        //         }
                        //         .onFocusChanged { if (it.isFocused) resetHideTimer() },
                        // ) {
                        //     Icon(
                        //         imageVector = if (isJzFullscreen) {
                        //             Icons.Filled.FullscreenExit
                        //         } else {
                        //             Icons.Filled.Fullscreen
                        //         },
                        //         contentDescription = if (isJzFullscreen) {
                        //             "Exit fullscreen"
                        //         } else {
                        //             "Fullscreen"
                        //         },
                        //         tint = Color.White,
                        //     )
                        // }
                    }
                }

                if (showJzXmlChrome) {
                    Text(
                        text = "DEBUG: Compose + JZ XML",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 8.dp),
                    )
                }
            }
        }

        PlayerChannelSheet(
            visible = channelSheetVisible,
            channels = siblings,
            currentChannelId = channel?.id ?: -1L,
            onChannelSelect = onSelectChannel,
            onDismiss = { channelSheetVisible = false },
        )
    }

    if (showMediaPlayerDialog) {
        PlayerMediaPlayerDialog(
            options = mediaPlayerOptions,
            selectedIndex = mediaPlayerOption.coerceIn(0, mediaPlayerOptions.lastIndex),
            onDismiss = { showMediaPlayerDialog = false },
            onSelect = { index ->
                onMediaPlayerOptionChange(index)
                showMediaPlayerDialog = false
            },
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPlayerIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.55f),
            contentColor = Color.White,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerMediaPlayerDialog(
    options: List<String>,
    selectedIndex: Int,
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
            Text(text = stringResource(R.string.media_player_title))
            options.forEachIndexed { index, label ->
                Button(
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (index == selectedIndex) {
                                Modifier.focusRequester(firstFocus)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    Text(
                        text = if (index == selectedIndex) "✓ $label" else label,
                    )
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.ok))
            }
        }
    }
}

@Composable
private fun HandleTvPlayerImmersiveUi() {
    val context = LocalContext.current
    val view = LocalView.current

    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        }
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        onDispose {
            activity?.let {
                WindowCompat.getInsetsController(it.window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
