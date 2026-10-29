package tv.hdonlinetv.compose.ui.mobile.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.player.tracks.NewClickableChipColor
import tv.hdonlinetv.compose.player.tracks.PlayerTracksController
import tv.hdonlinetv.compose.player.tracks.PlayerTracksUi

/** YouTube-style single entry point for audio/subtitles; styled like the other top-bar icon buttons. */
@Composable
fun PlayerTracksGearButton(onClick: () -> Unit) {
//    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
//        Icon(
//            imageVector = Icons.Filled.Tune,
//            contentDescription = stringResource(R.string.player_tracks_settings),
//            tint = Color.Black,
//            modifier = Modifier
//                .background(NewClickableChipColor, CircleShape)
//                .padding(5.dp)
//                .size(22.dp),
//        )
//    }
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = stringResource(R.string.player_tracks_settings),
            tint = colorResource(R.color.white),
        )
    }
}

private const val PAGE_MAIN = 0
private const val PAGE_AUDIO = 1
private const val PAGE_TEXT = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTracksSheet(visible: Boolean, tracks: PlayerTracksUi, onDismiss: () -> Unit) {
    if (!visible) return
    var page by rememberSaveable { mutableIntStateOf(PAGE_MAIN) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val offLabel = stringResource(R.string.player_subtitles_off)
    val audioTitle = stringResource(R.string.player_audio_track)
    val subsTitle = stringResource(R.string.player_subtitles)
    val black = colorResource(R.color.black)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.bgMain),
    ) {
        androidx.activity.compose.BackHandler(enabled = page != PAGE_MAIN) { page = PAGE_MAIN }
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                PAGE_MAIN -> {
                    if (tracks.showAudioButton) {
                        MainRow(audioTitle, tracks.audio.firstOrNull { it.selected }?.label ?: "—") { page = PAGE_AUDIO }
                    }
                    if (tracks.showAudioButton && tracks.showSubtitlesButton) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    }
                    if (tracks.showSubtitlesButton) {
                        MainRow(subsTitle, tracks.text.firstOrNull { it.selected }?.label ?: offLabel) { page = PAGE_TEXT }
                    }
                }
                PAGE_AUDIO -> {
                    SubHeader(audioTitle) { page = PAGE_MAIN }
                    tracks.audio.forEach { opt ->
                        ChoiceRow(opt.label, opt.selected) {
                            PlayerTracksController.selectAudio(opt)
                            page = PAGE_MAIN
                            onDismiss()
                        }
                    }
                }
                PAGE_TEXT -> {
                    SubHeader(subsTitle) { page = PAGE_MAIN }
                    ChoiceRow(offLabel, tracks.text.none { it.selected }) {
                        PlayerTracksController.disableSubtitles()
                        page = PAGE_MAIN
                        onDismiss()
                    }
                    tracks.text.forEach { opt ->
                        ChoiceRow(opt.label, opt.selected) {
                            PlayerTracksController.selectText(opt)
                            page = PAGE_MAIN
                            onDismiss()
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun MainRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = colorResource(R.color.black), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = colorResource(R.color.black).copy(alpha = 0.6f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp).weight(1f, fill = false),
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colorResource(R.color.black))
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 24.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colorResource(R.color.black))
        }
        Text(title, color = colorResource(R.color.black), fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), color = colorResource(R.color.black), fontSize = 16.sp)
        if (selected) Icon(Icons.Default.Check, null, tint = colorResource(R.color.colorPrimary))
    }
}
