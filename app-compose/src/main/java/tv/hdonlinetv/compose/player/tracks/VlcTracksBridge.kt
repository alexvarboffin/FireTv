package tv.hdonlinetv.compose.player.tracks

import android.content.Context
import org.videolan.libvlc.MediaPlayer
import java.util.Locale

/**
 * Exposes libvlc audio / SPU tracks to [PlayerTracksController] (same UI model as Exo).
 * TrackOption.groupIndex = [VLC_GROUP], TrackOption.trackIndex = libvlc track id.
 * Created per JZMediaVlc player; call [refresh] on ES / Playing / Vout events, [release] on release.
 */
class VlcTracksBridge(context: Context, private val player: MediaPlayer) : PlayerTracksController.ExternalTracks {

    private val appContext = context.applicationContext
    private var audioPrefApplied = false
    private var textPrefApplied = false
    private var released = false

    init {
        PlayerTracksController.attachExternal(appContext, this)
    }

    fun refresh() {
        if (released) return
        val ui = runCatching { buildUi() }.getOrNull() ?: return
        PlayerTracksController.publishExternal(this, ui)
        applySavedPreferences(ui)
    }

    fun release() {
        released = true
        PlayerTracksController.detachExternal(this)
    }

    override fun selectAudio(id: Int) {
        if (!released) runCatching { player.setAudioTrack(id) }
        audioPrefApplied = true
        refresh()
    }

    override fun selectText(id: Int) {
        if (!released) runCatching { player.setSpuTrack(id) }
        textPrefApplied = true
        refresh()
    }

    override fun disableText() {
        if (!released) runCatching { player.setSpuTrack(-1) }
        textPrefApplied = true
        refresh()
    }

    private fun applySavedPreferences(ui: PlayerTracksUi) {
        if (audioPrefApplied && textPrefApplied) return
        PlayerTracksController.readPreferences(appContext) { audioLang, textLang, textOff ->
            if (released) return@readPreferences
            if (!audioPrefApplied && ui.audio.isNotEmpty()) {
                audioPrefApplied = true
                audioLang?.let { lang ->
                    ui.audio.firstOrNull { sameLang(it.language, lang) && !it.selected }
                        ?.let { runCatching { player.setAudioTrack(it.trackIndex) } }
                }
            }
            if (!textPrefApplied && ui.text.isNotEmpty()) {
                textPrefApplied = true
                if (textOff) {
                    if (ui.text.any { it.selected }) runCatching { player.setSpuTrack(-1) }
                } else {
                    textLang?.let { lang ->
                        ui.text.firstOrNull { sameLang(it.language, lang) && !it.selected }
                            ?.let { runCatching { player.setSpuTrack(it.trackIndex) } }
                    }
                }
            }
            runCatching { PlayerTracksController.publishExternal(this, buildUi()) }
        }
    }

    private fun buildUi(): PlayerTracksUi {
        val langs = HashMap<Int, String?>()
        player.media?.let { media ->
            try {
                for (i in 0 until media.trackCount) {
                    val t = media.getTrack(i) ?: continue
                    langs[t.id] = t.language?.takeIf { it.isNotBlank() && it != "und" }
                }
            } finally {
                media.release()
            }
        }
        val curAudio = player.audioTrack
        val curSpu = player.spuTrack
        val audio = player.audioTracks.orEmpty().filter { it.id >= 0 }.mapIndexed { i, d ->
            option(d.id, d.name, langs[d.id], i + 1, d.id == curAudio)
        }
        val text = player.spuTracks.orEmpty().filter { it.id >= 0 }.mapIndexed { i, d ->
            option(d.id, d.name, langs[d.id], i + 1, d.id == curSpu)
        }
        return PlayerTracksUi(audio, text)
    }

    private fun option(id: Int, name: String?, lang: String?, ordinal: Int, selected: Boolean): TrackOption {
        val langName = lang?.let { Locale.forLanguageTag(it).getDisplayLanguage(Locale.getDefault()) }
            ?.takeIf { it.isNotBlank() && it != lang }
            ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        val label = when {
            !name.isNullOrBlank() && langName != null && !name.contains(langName, true) -> "$name · $langName"
            !name.isNullOrBlank() -> name
            langName != null -> langName
            else -> "#$ordinal"
        }
        return TrackOption(VLC_GROUP, id, label, lang, selected)
    }

    private fun sameLang(a: String?, b: String): Boolean {
        if (a == null) return false
        fun norm(x: String) = runCatching { Locale.forLanguageTag(x).isO3Language }.getOrNull() ?: x.lowercase()
        return a.equals(b, true) || norm(a) == norm(b)
    }

    companion object {
        const val VLC_GROUP = -100
    }
}
