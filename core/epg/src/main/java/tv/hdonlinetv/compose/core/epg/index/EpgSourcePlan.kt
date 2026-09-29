package tv.hdonlinetv.compose.core.epg.index

/**
 * Picks one XMLTV file per matched channel so that as few files as possible are downloaded:
 * each channel takes the file shared by the most matched channels (ties: the index's order).
 */
object EpgSourcePlan {

    /** Guide file URL → guide channel ids to keep from it. */
    fun plan(matches: Collection<EpgGuideMatch>): Map<String, Set<String>> {
        val popularity = HashMap<String, Int>()
        matches.forEach { m -> m.sourceUrls.distinct().forEach { popularity.merge(it, 1, Int::plus) } }
        val out = LinkedHashMap<String, MutableSet<String>>()
        matches.forEach { m ->
            val url = m.sourceUrls.maxByOrNull { popularity.getValue(it) } ?: return@forEach
            out.getOrPut(url) { LinkedHashSet() } += m.guideChannelId
        }
        return out
    }
}
