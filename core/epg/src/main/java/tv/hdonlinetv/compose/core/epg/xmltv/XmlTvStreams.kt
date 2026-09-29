package tv.hdonlinetv.compose.core.epg.xmltv

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.Reader
import java.util.zip.GZIPInputStream

object XmlTvStreams {
    /** UTF-8 reader; transparently gunzips by magic bytes (URL suffix / Content-Type lie often). */
    fun reader(input: InputStream): Reader {
        val buffered = if (input is BufferedInputStream) input else BufferedInputStream(input, 64 * 1024)
        buffered.mark(2)
        val b0 = buffered.read()
        val b1 = buffered.read()
        buffered.reset()
        val isGzip = b0 == 0x1f && b1 == 0x8b
        val raw = if (isGzip) GZIPInputStream(buffered, 64 * 1024) else buffered
        return raw.bufferedReader(Charsets.UTF_8)
    }
}
