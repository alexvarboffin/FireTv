package tv.hdonlinetv.compose.rate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateFeedbackTest {
    @Test fun legacyAddress() = assertEquals("alexvarboffin@gmail.com", RATE_FEEDBACK_EMAIL)

    @Test fun mailtoHasAddressAndSubject() =
        assertEquals(
            "mailto:dev@example.com?subject=Feedback%20for%20App",
            feedbackMailUri("dev@example.com", "Feedback for App"),
        )

    @Test fun subjectIsPercentEncodedUtf8_noPlusForSpaces() {
        val uri = feedbackMailUri(RATE_FEEDBACK_EMAIL, "🥇Feedback for Ultimate-TV: IPTV плеер M3U")
        assertTrue(uri.startsWith("mailto:alexvarboffin@gmail.com?subject="))
        assertFalse(uri.contains("+"))
        assertFalse(uri.contains(" "))
        assertEquals(
            "🥇Feedback for Ultimate-TV: IPTV плеер M3U",
            java.net.URLDecoder.decode(uri.substringAfter("subject="), "UTF-8"),
        )
    }

    @Test fun reservedCharsInSubjectAreEncoded() {
        val uri = feedbackMailUri("a@b.c", "x&y=z?#")
        assertEquals("mailto:a@b.c?subject=x%26y%3Dz%3F%23", uri)
    }

    @Test fun addressIsTrimmed() =
        assertTrue(feedbackMailUri(" a@b.c ", "s").startsWith("mailto:a@b.c?"))
}