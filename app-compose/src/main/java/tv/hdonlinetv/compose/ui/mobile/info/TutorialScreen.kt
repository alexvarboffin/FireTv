package tv.hdonlinetv.compose.ui.mobile.info

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

private data class TutorialTab(val titleRes: Int, val iconRes: Int)

private val tutorialTabs = listOf(
    TutorialTab(R.string.tab_tutorial, R.drawable.ic_information),
    TutorialTab(R.string.tab_faqs, R.drawable.ic_faq),
)

@Composable
fun TutorialScreen() {
    val navController = LocalPhoneNavController.current
    TutorialScreenBody(onBack = { navController.popBackStack() })
}

@Composable
fun TutorialScreenBody(onBack: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { tutorialTabs.size })
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = stringResource(R.string.menu_tutorial),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(R.color.bgMain)),
        ) {
            TutorialIconTabRow(
                tabs = tutorialTabs,
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> TutorialTabContent()
                    else -> FaqsTabContent()
                }
            }
        }
    }
}

@Composable
private fun TutorialIconTabRow(
    tabs: List<TutorialTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    // Legacy activity_faq: TabLayout tabMode=fixed + tabGravity=fill
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.bgMain))
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(300.dp))
                    .background(
                        if (selected) colorResource(R.color.colorLight) else androidx.compose.ui.graphics.Color.Transparent,
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = null,
                        tint = if (selected) {
                            colorResource(R.color.colorPrimary)
                        } else {
                            colorResource(R.color.black)
                        },
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(tab.titleRes),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 6.dp),
                        color = if (selected) {
                            colorResource(R.color.colorPrimary)
                        } else {
                            colorResource(R.color.black)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun InfoWebScreen() {
    val navController = LocalPhoneNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val url = args?.getString(Routes.InfoWeb.ARG_URL).orEmpty()
    val title = args?.getString(Routes.InfoWeb.ARG_TITLE).orEmpty()
    InfoWebScreenBody(
        title = title,
        url = url,
        onBack = { navController.popBackStack() },
    )
}

@Composable
fun InfoWebScreenBody(title: String, url: String, onBack: () -> Unit) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = title.ifBlank { stringResource(R.string.policy_privacy) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        },
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    if (url.isNotBlank()) loadUrl(url)
                }
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(R.color.bgMain)),
            update = { webView ->
                if (url.isNotBlank() && webView.url != url) {
                    webView.loadUrl(url)
                }
            },
        )
    }
}
