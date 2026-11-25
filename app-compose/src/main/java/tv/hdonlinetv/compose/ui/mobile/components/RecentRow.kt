package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.recent.RecentHistory
import tv.hdonlinetv.compose.recent.RecentItem
import tv.hdonlinetv.compose.ui.components.RemoteImage

/** "Recently watched" — first row of the phone home. Hidden when empty. Long-press removes. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentRow(onOpen: (RecentItem) -> Unit) {
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
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.key }) { item ->
                Card(
                    modifier = Modifier
                        .width(104.dp)
                        .combinedClickable(
                            onClick = { onOpen(item) },
                            // onLongClick = { RecentHistory.remove(item.key) }, // instant remove -> vanished
                            onLongClick = { pendingRemove = item },
                        ),
                ) {
                    RemoteImage(
                        url = item.cover,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                        contentScale = ContentScale.Fit,
                    )
                    if (item.isVod) {
                        LinearProgressIndicator(
                            progress = { item.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Text(
                        text = item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                    )
                }
            }
        }
    }
}
