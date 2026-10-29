package tv.hdonlinetv.compose.player.tracks

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.SubtitleView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/** One selectable audio/text track (group index + track index inside currentTracks). */
data class TrackOption(
    val groupIndex: Int,
    val trackIndex: Int,
    val label: String,
    val language: String?,
    val selected: Boolean,
)

data class PlayerTracksUi(
    val audio: List<TrackOption> = emptyList(),
    val text: List<TrackOption> = emptyList(),
) {
    val showAudioButton: Boolean get() = audio.size > 1
    val showSubtitlesButton: Boolean get() = text.isNotEmpty()
    val subtitlesOff: Boolean get() = text.none { it.selected }
}

private val Context.trackPrefs: DataStore<Preferences> by preferencesDataStore(name = "player_tracks")
private val KEY_AUDIO_LANG = stringPreferencesKey("preferred_audio_language")
private val KEY_TEXT_LANG = stringPreferencesKey("preferred_text_language")
private val KEY_TEXT_OFF = booleanPreferencesKey("subtitles_off")

/**
 * Bridge between the legacy JZ ExoPlayer backend (JZMediaExo) and Compose player UI.
 * JZMediaExo calls [attach] / [detach]; phone and TV screens collect [tracks] / [cues].
 * Only the Exo backend reports tracks; other backends leave the lists empty (buttons hidden).
 */
@OptIn(UnstableApi::class)
object PlayerTracksController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _tracks = MutableStateFlow(PlayerTracksUi())
    val tracks: StateFlow<PlayerTracksUi> = _tracks.asStateFlow()
    private val _cues = MutableStateFlow<List<Cue>>(emptyList())
    val cues: StateFlow<List<Cue>> = _cues.asStateFlow()

    private var player: Player? = null
    private var appContext: Context? = null

    private val listener = object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            _tracks.value = buildUi(tracks)
        }

        override fun onCues(cueGroup: CueGroup) {
            _cues.value = cueGroup.cues
        }
    }

    @JvmStatic
    fun attach(context: Context, newPlayer: Player) {
        player?.let { detach(it) }
        player = newPlayer
        appContext = context.applicationContext
        newPlayer.addListener(listener)
        _tracks.value = buildUi(newPlayer.currentTracks)
        applySavedPreferences(newPlayer)
    }

    @JvmStatic
    fun detach(oldPlayer: Player?) {
        if (oldPlayer == null || oldPlayer !== player) return
        oldPlayer.removeListener(listener)
        player = null
        _tracks.value = PlayerTracksUi()
        _cues.value = emptyList()
    }

    private fun applySavedPreferences(target: Player) {
        val ctx = appContext ?: return
        scope.launch {
            val prefs = runCatching { ctx.trackPrefs.data.first() }.getOrNull() ?: return@launch
            if (player !== target) return@launch
            val builder = target.trackSelectionParameters.buildUpon()
            prefs[KEY_AUDIO_LANG]?.let { builder.setPreferredAudioLanguage(it) }
            if (prefs[KEY_TEXT_OFF] == true) {
                builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            } else {
                prefs[KEY_TEXT_LANG]?.let {
                    builder.setPreferredTextLanguage(it)
                    builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                }
            }
            target.trackSelectionParameters = builder.build()
        }
    }

    fun selectAudio(option: TrackOption) = select(C.TRACK_TYPE_AUDIO, option)

    fun selectText(option: TrackOption) = select(C.TRACK_TYPE_TEXT, option)

    private fun select(type: Int, option: TrackOption) {
        val p = player ?: return
        val group = p.currentTracks.groups.getOrNull(option.groupIndex) ?: return
        p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(type, false)
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, option.trackIndex))
            .build()
        val lang = option.language ?: return
        save { prefs ->
            if (type == C.TRACK_TYPE_AUDIO) {
                prefs[KEY_AUDIO_LANG] = lang
            } else {
                prefs[KEY_TEXT_LANG] = lang
                prefs[KEY_TEXT_OFF] = false
            }
        }
    }

    fun disableSubtitles() {
        val p = player ?: return
        p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
        _cues.value = emptyList()
        save { it[KEY_TEXT_OFF] = true }
    }

    private fun save(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        val ctx = appContext ?: return
        scope.launch(Dispatchers.IO) {
            runCatching { ctx.trackPrefs.edit { block(it) } }
        }
    }

    private fun buildUi(tracks: Tracks): PlayerTracksUi {
        val audio = mutableListOf<TrackOption>()
        val text = mutableListOf<TrackOption>()
        tracks.groups.forEachIndexed { groupIndex, group ->
            val target = when (group.type) {
                C.TRACK_TYPE_AUDIO -> audio
                C.TRACK_TYPE_TEXT -> text
                else -> return@forEachIndexed
            }
            for (i in 0 until group.length) {
                if (!group.isTrackSupported(i)) continue
                val format = group.getTrackFormat(i)
                target += TrackOption(
                    groupIndex = groupIndex,
                    trackIndex = i,
                    label = describe(format, group.type, target.size + 1),
                    language = format.language?.takeIf { it != C.LANGUAGE_UNDETERMINED },
                    selected = group.isTrackSelected(i),
                )
            }
        }
        return PlayerTracksUi(audio, text)
    }

    private fun describe(format: Format, type: Int, ordinal: Int): String {
        val parts = mutableListOf<String>()
        val lang = format.language?.takeIf { it != C.LANGUAGE_UNDETERMINED }
        val langName = lang?.let { Locale.forLanguageTag(it).getDisplayLanguage(Locale.getDefault()) }
            ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        val label = format.label
        when {
            !label.isNullOrBlank() -> parts += label
            !langName.isNullOrBlank() -> parts += langName
            else -> parts += "#$ordinal"
        }
        val langNameEn = lang?.let { Locale.forLanguageTag(it).getDisplayLanguage(Locale.ENGLISH) }
        if (!label.isNullOrBlank() && !langName.isNullOrBlank() &&
            !label.contains(langName, true) &&
            (langNameEn.isNullOrBlank() || !label.contains(langNameEn, true))
        ) {
            parts += langName
        }
        if (type == C.TRACK_TYPE_AUDIO) {
            when (format.channelCount) {
                1 -> parts += "Mono"
                2 -> parts += "Stereo"
                6 -> parts += "5.1"
                8 -> parts += "7.1"
                Format.NO_VALUE -> Unit
                else -> parts += "${format.channelCount}ch"
            }
        }
        codecName(format)?.let { parts += it }
        if ((format.selectionFlags and C.SELECTION_FLAG_FORCED) != 0) parts += "forced"
        return parts.joinToString(" · ")
    }

    private fun codecName(format: Format): String? = when (format.sampleMimeType ?: format.codecs) {
        MimeTypes.AUDIO_AAC -> "AAC"
        MimeTypes.AUDIO_AC3 -> "AC3"
        MimeTypes.AUDIO_E_AC3, MimeTypes.AUDIO_E_AC3_JOC -> "E-AC3"
        MimeTypes.AUDIO_MPEG -> "MP3"
        MimeTypes.AUDIO_OPUS -> "Opus"
        MimeTypes.AUDIO_DTS -> "DTS"
        MimeTypes.TEXT_VTT -> "WebVTT"
        MimeTypes.APPLICATION_SUBRIP -> "SRT"
        MimeTypes.APPLICATION_CEA608, MimeTypes.APPLICATION_CEA708 -> "CC"
        MimeTypes.APPLICATION_DVBSUBS -> "DVB"
        MimeTypes.APPLICATION_TTML -> "TTML"
        else -> null
    }
}

private const val SUBTITLE_VIEW_TAG = "player_tracks_subtitle_view"

/**
 * Adds (once) a media3-ui SubtitleView as the top child of the JZ player view, so subtitles
 * follow the player into JZ fullscreen and never intercept touches / D-pad focus.
 */
@OptIn(UnstableApi::class)
fun ensureSubtitleView(host: ViewGroup): SubtitleView {
    (host.findViewWithTag<android.view.View>(SUBTITLE_VIEW_TAG) as? SubtitleView)?.let { return it }
    val view = SubtitleView(host.context).apply {
        tag = SUBTITLE_VIEW_TAG
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        setUserDefaultStyle()
        setUserDefaultTextSize()
        isClickable = false
        isFocusable = false
    }
    host.addView(view)
    return view
}

/** Pushes current cues (Player.Listener.onCues) into the SubtitleView inside [host]. */
@OptIn(UnstableApi::class)
@Composable
fun BindPlayerSubtitles(host: ViewGroup?) {
    val cues by PlayerTracksController.cues.collectAsState()
    LaunchedEffect(host, cues) {
        host?.let { ensureSubtitleView(it).setCues(cues) }
    }
}

/** Accent colour for clickables added in this feature (NewClickableChip rule). */
val NewClickableChipColor = Color(0xFFFFB300)

/** Phone-side chip button ("Audio" / "Subtitles"); new clickable → NewClickableChip colouring. */
@Composable
fun NewClickableChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = text,
        color = Color.Black,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(NewClickableChipColor, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
