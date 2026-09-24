package tv.hdonlinetv.compose.ui.mobile.info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController

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
fun TutorialTabContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 72.dp),
        ) {
            Text(
                text = stringResource(R.string.how_to_add_playlist),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.black),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Text(
                text = stringResource(R.string.tutorial),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.black),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Text(
                text = stringResource(R.string.step1),
                color = colorResource(R.color.tabText),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = stringResource(R.string.step2),
                color = colorResource(R.color.tabText),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_tutor2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 8.dp),
                contentScale = ContentScale.FillWidth,
            )
            Text(
                text = stringResource(R.string.step3),
                color = colorResource(R.color.tabText),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_tutor3),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 8.dp),
                contentScale = ContentScale.FillWidth,
            )
        }
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SEARCH_IPTV_URL)))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.colorPrimary),
                contentColor = colorResource(R.color.white),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.search_button))
        }
    }
}

@Composable
fun FaqsTabContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = LocalPhoneNavController.current
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
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.bgMain)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        items(rows, key = { row ->
            when (row) {
                is FaqRow.Header -> "h:${row.title}"
                is FaqRow.Question -> "q:${row.text}"
                is FaqRow.Playlist -> "p:${row.url}"
            }
        }) { row ->
            when (row) {
                is FaqRow.Header -> FaqHeaderRow(title = row.title)
                is FaqRow.Question -> FaqQuestionRow(
                    question = row.text,
                    onClick = {
                        navController.navigate(
                            Routes.InfoWeb.build(url = row.htmlAssetUrl, title = row.text),
                        )
                    },
                )
                is FaqRow.Playlist -> FaqPlaylistRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    onCopy = { copyPlaylistUrl(context, row.url) },
                )
            }
        }
    }
}

@Composable
private fun FaqHeaderRow(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(R.color.black),
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    )
}

@Composable
private fun FaqQuestionRow(question: String, onClick: () -> Unit) {
    Text(
        text = question,
        fontSize = 16.sp,
        color = colorResource(R.color.link_color),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun FaqPlaylistRow(
    title: String,
    subtitle: String,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                shape = RoundedCornerShape(4.dp),
            )
            .clickable(onClick = onCopy)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = colorResource(R.color.link_color),
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = colorResource(R.color.tabText),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_copy_02),
            contentDescription = null,
            tint = colorResource(R.color.tabText),
            // Legacy item_playlist2: 32dp ImageView (drawable intrinsic is 42dp)
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onCopy),
        )
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
