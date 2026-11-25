package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.recent.RecentHistory
import tv.hdonlinetv.compose.recent.RecentItem
import tv.hdonlinetv.compose.ui.components.RemoteImage

/** "Recently watched" — first row of the TV home. Hidden when empty. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvRecentRow(onOpen: (RecentItem) -> Unit) {
    RecentHistory.init(LocalContext.current)
    val items by RecentHistory.items.collectAsState()
    var pendingRemove by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<RecentItem?>(null)
    }

    pendingRemove?.let { target ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { androidx.compose.material3.Text(target.title) },
            text = { androidx.compose.material3.Text(stringResource(R.string.recent_remove_confirm)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    RecentHistory.remove(target.key)
                    pendingRemove = null
                }) { androidx.compose.material3.Text(stringResource(R.string.recent_remove)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingRemove = null }) {
                    androidx.compose.material3.Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
    if (items.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.recent_watched_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items, key = { it.key }) { item ->
                Card(
                    onClick = { onOpen(item) },
                    // Was: instant remove on long-press -> items vanished on a held OK/tap.
                    // onLongClick = { RecentHistory.remove(item.key) },
                    onLongClick = { pendingRemove = item },
                    // modifier = Modifier.width(120.dp).padding(4.dp),
                    modifier = Modifier.width(120.dp).padding(4.dp)
                        .tvDpadClick(onClick = { onOpen(item) }, onLongClick = { pendingRemove = item }),
                ) {
                    Column {
                        Box {
                            RemoteImage(
                                url = item.cover,
                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                                contentScale = ContentScale.Fit,
                            )
                            if (item.isVod) {
                                RecentProgressBar(item.progress, Modifier.align(Alignment.BottomCenter))
                            }
                        }
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentProgressBar(progress: Float, modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(4.dp).background(androidx.compose.ui.graphics.Color(0x66000000))) {
        Box(
            modifier = Modifier.fillMaxWidth(progress).height(4.dp)
                .background(androidx.compose.ui.graphics.Color(0xFFE53935)),
        )
    }
}
