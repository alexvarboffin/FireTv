package tv.hdonlinetv.compose.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.epg.EpgNowNext
import tv.hdonlinetv.compose.core.epg.EpgProgramme
import tv.hdonlinetv.compose.core.epg.EpgStore
import tv.hdonlinetv.compose.core.epg.index.EpgChannelRef

/** Current / next programme of [channel] from `epg.db`; null until the guide has it. */
@Composable
fun rememberChannelNowNext(channel: ChannelUi): EpgNowNext? {
    if (LocalInspectionMode.current) return null
    val context = LocalContext.current
    val store = remember { EpgStore.get(context) }
    val version by store.version.collectAsState()
    val ref = remember(channel.tvgId, channel.name) {
        EpgChannelRef(channel.tvgId?.trim()?.ifEmpty { null }, channel.name)
    }
    val nowNext by produceState<EpgNowNext?>(null, ref, version) {
        while (true) {
            val current = store.nowNextFor(listOf(ref))[ref]
            value = current?.takeIf { it.now != null || it.next != null }
            val now = System.currentTimeMillis()
            val change = current?.now?.stop ?: current?.next?.start ?: (now + IDLE_REFRESH_MS)
            delay((change - now).coerceIn(MIN_REFRESH_MS, IDLE_REFRESH_MS))
        }
    }
    return nowNext
}

/** Current programme + progress on a dark strip, for the bottom of a channel logo. */
@Composable
fun ChannelEpgNowStrip(
    nowNext: EpgNowNext?,
    modifier: Modifier = Modifier,
) {
    val programme = nowNext?.now ?: return
    Column(modifier = modifier.fillMaxWidth().background(Color(0xB3000000))) {
        BasicText(
            text = programme.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = Color.White, fontSize = 11.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
        EpgProgress(programme)
    }
}

/** «HH:mm Title» for now and «Next: HH:mm Title», for list rows. */
@Composable
fun ChannelEpgNowNextLines(
    nowNext: EpgNowNext?,
    color: Color,
    modifier: Modifier = Modifier,
    showNext: Boolean = true,
) {
    nowNext ?: return
    val format = DateFormat.getTimeFormat(LocalContext.current)
    Column(modifier = modifier) {
        nowNext.now?.let { p ->
            BasicText(
                text = "${format.format(p.start)}  ${p.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(color = color, fontSize = 13.sp),
            )
            EpgProgress(p, Modifier.padding(top = 3.dp, bottom = 2.dp))
        }
        nowNext.next?.takeIf { showNext }?.let { p ->
            BasicText(
                text = stringResource(R.string.epg_next, format.format(p.start), p.title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(color = color.copy(alpha = color.alpha * 0.7f), fontSize = 12.sp),
            )
        }
    }
}

@Composable
private fun EpgProgress(programme: EpgProgramme, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(3.dp).background(Color(0x40FFFFFF))) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(programme.progress(System.currentTimeMillis()))
                .background(colorResource(R.color.colorPrimary)),
        )
    }
}

private const val MIN_REFRESH_MS = 30_000L
private const val IDLE_REFRESH_MS = 10 * 60_000L
