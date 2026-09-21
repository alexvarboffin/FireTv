package tv.hdonlinetv.compose.ui.tv.info

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun InfoWebScreen() {
    val navController = LocalTvNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val url = args?.getString(Routes.InfoWeb.ARG_URL).orEmpty()
    val title = args?.getString(Routes.InfoWeb.ARG_TITLE).orEmpty()
    InfoWebScreenBody(
        title = title,
        url = url,
        onBack = { navController.popBackStack() },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun InfoWebScreenBody(
    title: String,
    url: String,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = stringResource(R.string.ok))
        }
        Text(
            text = title.ifBlank { stringResource(R.string.policy_privacy) },
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    if (url.isNotBlank()) loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            update = { webView ->
                if (url.isNotBlank() && webView.url != url) {
                    webView.loadUrl(url)
                }
            },
        )
    }
}
