package tv.hdonlinetv.compose.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import cn.jzvd.Jzvd

private data class SavedWindowState(
    val statusBarColor: Int,
    val navigationBarColor: Int,
    val requestedOrientation: Int,
    val systemUiVisibility: Int,
)

/**
 * Black status/navigation bars for the whole player screen (portrait toolbar + video).
 */
@Composable
fun HandlePlayerScreenSystemUi() {
    val context = LocalContext.current
    val view = LocalView.current

    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        applyPlayerScreenChrome(activity.window, view, immersive = false)
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val window = activity.window
        val saved = window.saveState(view)
        onDispose {
            window.restoreState(view, saved)
        }
    }
}

/**
 * JZVD window fullscreen: landscape + hide system bars. Exiting fullscreen keeps player chrome.
 */
@Composable
fun HandleJzPlayerFullscreen(
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current

    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            applyPlayerScreenChrome(activity.window, view, immersive = true)
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            applyPlayerScreenChrome(activity.window, view, immersive = false)
        }
    }

    DisposableEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen) {
            Jzvd.backPress()
        }
        onDispose { }
    }

    DisposableEffect(Unit) {
        onDispose {
            onFullscreenChange(false)
            context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

private fun applyPlayerScreenChrome(window: Window, view: View, immersive: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    @Suppress("DEPRECATION")
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    @Suppress("DEPRECATION")
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.BLACK
    window.navigationBarColor = Color.BLACK

    val controller = WindowCompat.getInsetsController(window, view)
    controller.isAppearanceLightStatusBars = false
    controller.isAppearanceLightNavigationBars = false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (immersive) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if (immersive) {
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }
}

private fun Window.saveState(view: View): SavedWindowState {
    @Suppress("DEPRECATION")
    val uiVisibility = decorView.systemUiVisibility
    return SavedWindowState(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        requestedOrientation = findActivityFromWindow()?.requestedOrientation
            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        systemUiVisibility = uiVisibility,
    )
}

private fun Window.restoreState(view: View, saved: SavedWindowState) {
    statusBarColor = saved.statusBarColor
    navigationBarColor = saved.navigationBarColor
    WindowCompat.setDecorFitsSystemWindows(this, false)
    findActivityFromWindow()?.requestedOrientation = saved.requestedOrientation
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.getInsetsController(this, view).show(WindowInsetsCompat.Type.systemBars())
    } else {
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility = saved.systemUiVisibility
    }
}

private fun Window.findActivityFromWindow(): Activity? {
    return decorView.context.findActivity()
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
