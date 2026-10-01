package tv.hdonlinetv.compose.ui.tv.player

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.epg.EpgProgramme
import tv.hdonlinetv.compose.core.epg.EpgStore
import tv.hdonlinetv.compose.core.epg.index.EpgChannelRef
import java.util.Calendar

/**
 * Cinema `ProgramGuideContainer`: schedule of the channel focused in [PlayerChannelSheet],
 * grouped by day, scrolled to the programme on air. Rows are focusable so long lists scroll.
 * Left → back to the channel list; Right / Home / Esc → [onDismiss].
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerChannelGuide(
    channel: ChannelUi?,
    groupFocus: FocusRequester,
    onBackToChannels: () -> Unit,
    onDismiss: () -> Unit,
    onHasProgrammes: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember { EpgStore.get(context) }
    val version by store.version.collectAsState()
    val programmes by produceState<List<EpgProgramme>?>(null, channel?.id, channel?.name, version) {
        value = null
        val ch = channel ?: return@produceState
        delay(FOCUS_SETTLE_MS)
        val now = System.currentTimeMillis()
        val ref = EpgChannelRef(ch.tvgId?.trim()?.ifEmpty { null }, ch.name)
        value = store.schedule(ref, from = now - PAST_MS, to = now + AHEAD_MS)
    }
    LaunchedEffect(programmes) { onHasProgrammes(!programmes.isNullOrEmpty()) }

    val now = remember(programmes) { System.currentTimeMillis() }
    val timeFormat = remember { DateFormat.getTimeFormat(context) }
    val rows = remember(programmes) { programmes.orEmpty().toGuideRows(context) }
    val currentIndex = rows.indexOfFirst { it is GuideRow.Item && it.programme.start <= now && it.programme.stop > now }
        .takeIf { it >= 0 }
        ?: rows.indexOfFirst { it is GuideRow.Item }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    val currentFocus = remember { FocusRequester() }
    LaunchedEffect(rows) {
        if (rows.isNotEmpty()) listState.scrollToItem((currentIndex - 1).coerceAtLeast(0))
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = channel?.name.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when {
            programmes == null -> Unit
            rows.isEmpty() -> Text(
                text = stringResource(R.string.epg_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(groupFocus)
                    .focusRestorer(currentFocus),
            ) {
                itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
                    when (row) {
                        is GuideRow.Day -> Text(
                            text = row.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                        )
                        is GuideRow.Item -> GuideProgrammeRow(
                            programme = row.programme,
                            time = timeFormat.format(row.programme.start),
                            isCurrent = row.programme.start <= now && row.programme.stop > now,
                            onLeft = onBackToChannels,
                            onDismiss = onDismiss,
                            modifier = if (index == currentIndex) {
                                Modifier.focusRequester(currentFocus)
                            } else {
                                Modifier
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GuideProgrammeRow(
    programme: EpgProgramme,
    time: String,
    isCurrent: Boolean,
    onLeft: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> {
                        onLeft()
                        true
                    }
                    Key.DirectionRight, Key.SystemHome, Key.MoveHome, Key.Escape -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            },
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCurrent) colors.primary.copy(alpha = 0.28f) else Color.Transparent,
            focusedContainerColor = colors.primary.copy(alpha = 0.72f),
            pressedContainerColor = colors.primary.copy(alpha = 0.85f),
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = programme.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isCurrent) {
                programme.desc?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.25f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(programme.progress(System.currentTimeMillis()))
                            .background(colorResource(R.color.colorPrimary)),
                    )
                }
            }
        }
    }
}

private sealed interface GuideRow {
    val key: Any

    data class Day(val title: String, override val key: Any) : GuideRow
    data class Item(val programme: EpgProgramme) : GuideRow {
        override val key: Any get() = programme.start to programme.title
    }
}

/** Day headers + programmes; a channel present in two guide files yields duplicates, keep one. */
private fun List<EpgProgramme>.toGuideRows(context: Context): List<GuideRow> {
    val out = ArrayList<GuideRow>(size + 3)
    var lastDay: Long = -1
    for (p in distinctBy { it.start to it.title }) {
        val day = Calendar.getInstance().apply {
            timeInMillis = p.start
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        if (day != lastDay) {
            lastDay = day
            val title = DateUtils.formatDateTime(
                context,
                p.start,
                DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR,
            )
            out += GuideRow.Day(title, "d$day")
        }
        out += GuideRow.Item(p)
    }
    return out
}

private const val FOCUS_SETTLE_MS = 150L
private const val PAST_MS = 3 * 60 * 60_000L
private const val AHEAD_MS = 36 * 60 * 60_000L
