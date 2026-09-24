package tv.hdonlinetv.compose.ui.tv.info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.TvFocusTabRow

private const val SEARCH_IPTV_URL =
    "https://www.google.com/search?q=Free+Popular+IPTV+Playlist"

private const val PLAYLIST_UK_URL = "https://iptv-org.github.io/iptv/countries/uk.m3u"
private const val PLAYLIST_COUNTRY_URL = "https://iptv-org.github.io/iptv/index.country.m3u"
private const val PLAYLIST_CATEGORY_URL = "https://iptv-org.github.io/iptv/index.m3u"
private const val PLAYLIST_FREE_TV_URL =
    "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8"

private val FAQ_HTML_ASSETS = listOf(
    "file:///android_asset/00.html",
    "file:///android_asset/02.html",
)

private sealed interface FaqRow {
    data class Header(val title: String) : FaqRow
    data class Question(val text: String, val htmlAssetUrl: String) : FaqRow
    data class Playlist(val title: String, val subtitle: String, val url: String) : FaqRow
}

@Composable
fun TutorialScreen(
    onBack: (() -> Unit)? = null,
) {
    val navController = LocalTvNavController.current
    val exit: () -> Unit = onBack ?: {
        navController.popBackStack()
        Unit
    }
    TutorialScreenBody(
        onBack = exit,
        onOpenInfoWeb = { url, title ->
            navController.navigate(Routes.InfoWeb.build(url = url, title = title))
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TutorialScreenBody(
    onBack: () -> Unit,
    onOpenInfoWeb: (url: String, title: String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val drawerFocus = LocalTvDrawerFocusRequester.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabRowFallback = remember { FocusRequester() }
    val tabs = listOf(
        stringResource(R.string.tab_tutorial),
        stringResource(R.string.tab_faqs),
    )
    val leftToDrawer = if (drawerFocus != null) {
        Modifier.focusProperties { left = drawerFocus }
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = leftToDrawer,
        ) {
            Text(text = stringResource(R.string.ok))
        }
        Text(
            text = stringResource(R.string.menu_tutorial),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        TvFocusTabRow(
            tabs = tabs,
            selectedIndex = selectedTab,
            onSelectedIndexChange = { selectedTab = it },
            tabRowFallback = tabRowFallback,
            leftFocusRequester = drawerFocus,
        )
        when (selectedTab) {
            0 -> TutorialTabContent(leftToDrawer = leftToDrawer)
            else -> FaqsTabContent(
                onOpenInfoWeb = onOpenInfoWeb,
                leftToDrawer = leftToDrawer,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TutorialTabContent(
    leftToDrawer: Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            TutorialFocusBlock(modifier = leftToDrawer) {
                Text(text = stringResource(R.string.how_to_add_playlist))
                Text(
                    text = stringResource(R.string.tutorial),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item(key = "step1") {
            TutorialFocusBlock(modifier = leftToDrawer) {
                Text(text = stringResource(R.string.step1))
            }
        }
        item(key = "step2") {
            TutorialFocusBlock(modifier = leftToDrawer) {
                Text(text = stringResource(R.string.step2))
                Image(
                    painter = painterResource(R.drawable.ic_tutor2),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 8.dp),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
        item(key = "step3") {
            TutorialFocusBlock(modifier = leftToDrawer) {
                Text(text = stringResource(R.string.step3))
                Image(
                    painter = painterResource(R.drawable.ic_tutor3),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 8.dp),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
        item(key = "search") {
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SEARCH_IPTV_URL)))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .then(leftToDrawer),
            ) {
                Text(text = stringResource(R.string.search_button))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TutorialFocusBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = {},
        modifier = modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FaqsTabContent(
    onOpenInfoWeb: (url: String, title: String) -> Unit,
    leftToDrawer: Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val faqQuestions = stringArrayResource(R.array.faq_questions)
    val rows = remember(
        faqQuestions,
        stringResource(R.string.frequentlyAskedQuestions),
        stringResource(R.string.sample_playlist_url),
        stringResource(R.string.playlist_uk),
        stringResource(R.string.playlist_country),
        stringResource(R.string.playlist_category),
    ) {
        buildFaqRows(
            faqQuestions = faqQuestions,
            faqHeader = context.getString(R.string.frequentlyAskedQuestions),
            sampleHeader = context.getString(R.string.sample_playlist_url),
            ukTitle = context.getString(R.string.playlist_uk),
            countryTitle = context.getString(R.string.playlist_country),
            categoryTitle = context.getString(R.string.playlist_category),
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rows, key = { row ->
            when (row) {
                is FaqRow.Header -> "h:${row.title}"
                is FaqRow.Question -> "q:${row.text}"
                is FaqRow.Playlist -> "p:${row.url}"
            }
        }) { row ->
            when (row) {
                is FaqRow.Header -> Text(text = row.title)
                is FaqRow.Question -> {
                    Surface(
                        onClick = { onOpenInfoWeb(row.htmlAssetUrl, row.text) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(leftToDrawer),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = colors.surface,
                            contentColor = colors.onSurface,
                            focusedContainerColor = colors.primary,
                            focusedContentColor = colors.onPrimary,
                        ),
                    ) {
                        Text(
                            text = row.text,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                is FaqRow.Playlist -> {
                    Surface(
                        onClick = { copyPlaylistUrl(context, row.url) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(leftToDrawer),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = colors.surface,
                            contentColor = colors.onSurface,
                            focusedContainerColor = colors.primary,
                            focusedContentColor = colors.onPrimary,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = row.title)
                            Text(
                                text = row.subtitle,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildFaqRows(
    faqQuestions: Array<String>,
    faqHeader: String,
    sampleHeader: String,
    ukTitle: String,
    countryTitle: String,
    categoryTitle: String,
): List<FaqRow> {
    val rows = mutableListOf<FaqRow>()
    rows += FaqRow.Header(faqHeader)
    faqQuestions.forEachIndexed { index, question ->
        val asset = FAQ_HTML_ASSETS.getOrElse(index) { FAQ_HTML_ASSETS.first() }
        rows += FaqRow.Question(question, asset)
    }
    rows += FaqRow.Header(sampleHeader)
    rows += FaqRow.Playlist(
        title = ukTitle,
        subtitle = PLAYLIST_UK_URL.replace(".io/iptv/countries/", "../") + " ~49.6Kb",
        url = PLAYLIST_UK_URL,
    )
    rows += FaqRow.Playlist(
        title = countryTitle,
        subtitle = PLAYLIST_COUNTRY_URL.replace(".io/iptv/index.", "../") + " ~3.8Mb",
        url = PLAYLIST_COUNTRY_URL,
    )
    rows += FaqRow.Playlist(
        title = categoryTitle,
        subtitle = PLAYLIST_CATEGORY_URL.replace(".io/iptv/index.", "../") + " ~2.6mb",
        url = PLAYLIST_CATEGORY_URL,
    )
    rows += FaqRow.Playlist(
        title = "Free-TV",
        subtitle = PLAYLIST_FREE_TV_URL
            .replace("hubusercontent.com/Free-TV/IPTV/master", ".../Free-TV/IPTV"),
        url = PLAYLIST_FREE_TV_URL,
    )
    return rows
}

private fun copyPlaylistUrl(context: Context, url: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    if (clipboard != null) {
        clipboard.setPrimaryClip(ClipData.newPlainText("Copied URL", url))
        Toast.makeText(context, R.string.playlist_url_copied, Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_LONG).show()
    }
}
