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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
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
import tv.hdonlinetv.compose.navigation.Routes
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
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    var mediaPlayerOption by remember {
        mutableIntStateOf(settingsRepository.getSettings().mediaPlayerOption)
    }
    val viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            repository = repository,
            channelId = channelId,
            directStreamUrl = streamUrl.takeIf { it.isNotBlank() },
            directStreamTitle = streamTitle.takeIf { it.isNotBlank() },
        ),
    )
    val state by viewModel.uiState.collectAsState()
    PlayerScreenBody(
        channel = state.channel,
        isLoading = state.isLoading,
        mediaPlayerOption = mediaPlayerOption,
        onBack = { navController.popBackStack() },
        onToggleFavorite = { viewModel.toggleFavorite() },
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
    isLoading: Boolean,
    mediaPlayerOption: Int,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMediaPlayerOptionChange: (Int) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showMediaPlayerDialog by remember { mutableStateOf(false) }
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
    val rootFocus = remember { FocusRequester() }
    val (frFavorite, frClose, frRefresh, frProgress, frSettings) = remember {
        FocusRequester.createRefs()
    }
    // DEBUG: keep JZ XML chrome visible to verify native seek/replay still work.
    val hideJzChrome = !BuildConfig.DEBUG

    val displayedProgress = scrubProgress
        ?: if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    // Cinema TvUI_VideoLayout: any interaction restarts the auto-hide countdown.
    fun resetHideTimer() {
        lastInteractionTime = System.currentTimeMillis()
        hideControlsJob?.cancel()
        if (!controlsVisible || showMediaPlayerDialog) return
        hideControlsJob = scope.launch {
            delay(CONTROL_VISIBILITY_TIMEOUT_MS)
            if (System.currentTimeMillis() - lastInteractionTime >= CONTROL_VISIBILITY_TIMEOUT_MS) {
                controlsVisible = false
            }
        }
    }

    fun refreshStream() {
        resetHideTimer()
        val player = playerRef ?: return
        val ch = channel ?: return
        val playerSetup = setup ?: return
        LegacyJzPlayerSetup.apply(player, ch, playerSetup, mediaPlayerOption)
        positionMs = 0L
        scrubProgress = null
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
            !Jzvd.backPress() -> exitPlayer()
            else -> Unit
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

    LaunchedEffect(controlsVisible, showMediaPlayerDialog) {
        if (controlsVisible && !showMediaPlayerDialog) {
            resetHideTimer()
            // Cinema: initial focus on center play/refresh control.
            frRefresh.requestFocus()
        } else {
            hideControlsJob?.cancel()
            if (!controlsVisible) {
                rootFocus.requestFocus()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocus)
            .focusProperties {
                // Cinema root: when chrome visible, D-pad enters center control.
                left = FocusRequester.Cancel
                up = FocusRequester.Cancel
                right = if (controlsVisible) frRefresh else FocusRequester.Cancel
                down = if (controlsVisible) frRefresh else FocusRequester.Cancel
            }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    resetHideTimer()
                    if (!controlsVisible && !showMediaPlayerDialog) {
                        controlsVisible = true
                        return@onKeyEvent true
                    }
                }
                false
            },
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
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
                        player.onPlaybackProgress = { position, duration ->
                            if (scrubProgress == null) {
                                positionMs = position
                            }
                            durationMs = duration
                        }
                    },
                    onRelease = { player ->
                        player.onPlaybackProgress = null
                        if (!isExiting) {
                            LegacyJzPlayerRelease.stopAndRelease(player)
                        }
                    },
                )
            }
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
                        if (channel != null && channel.id > 0) {
                            TvPlayerIconButton(
                                onClick = {
                                    resetHideTimer()
                                    onToggleFavorite()
                                },
                                modifier = Modifier
                                    .focusRequester(frFavorite)
                                    .focusProperties {
                                        // Cyclic top row: Favorite ↔ Close
                                        left = frClose
                                        right = frClose
                                        down = frRefresh
                                        up = frSettings
                                    }
                                    .onFocusChanged { if (it.isFocused) resetHideTimer() },
                            ) {
                                Icon(
                                    imageVector = if (channel.isFavorite) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = stringResource(
                                        if (channel.isFavorite) R.string.remove_fav else R.string.add_fav,
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
                                    left = if (channel != null && channel.id > 0) frFavorite else frClose
                                    right = if (channel != null && channel.id > 0) frFavorite else frClose
                                    down = frRefresh
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

                TvPlayerIconButton(
                    onClick = { refreshStream() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(frRefresh)
                        .focusProperties {
                            up = if (channel != null && channel.id > 0) frFavorite else frClose
                            down = if (durationMs > 0L) frProgress else frSettings
                            left = FocusRequester.Cancel
                            right = FocusRequester.Cancel
                        }
                        .size(72.dp)
                        .onFocusChanged { if (it.isFocused) resetHideTimer() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
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
                        upFocus = frRefresh,
                        downFocus = frSettings,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.End,
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
                                    up = if (durationMs > 0L) frProgress else frRefresh
                                    down = if (channel != null && channel.id > 0) frFavorite else frClose
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
                    }
                }
            }
        }
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
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
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
