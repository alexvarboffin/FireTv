package com.walhalla.data.repository

import android.content.Context
import android.text.TextUtils
import com.walhalla.data.model.Channel
import com.walhalla.ui.BuildConfig
import com.walhalla.ui.DLog.d
import java.util.Arrays
import java.util.TreeSet
import java.util.regex.Pattern

object M3UParser {
    //http-user-agent OR http-referrer without quotes ""
    const val extVlCoptHttpUserAgent: String = "http-user-agent=([^\"]*)"
    const val extVlCoptHttpReferrer: String = "http-referrer=([^\"]*)"

    val extVlCoptHttpUserPattern: Pattern = Pattern.compile(
        extVlCoptHttpUserAgent
    )
    val extVlCoptHttpReferrerPattern: Pattern = Pattern.compile(
        extVlCoptHttpReferrer
    )


    const val regex: String =
        "(?:^|\\n)#EXTINF:([^,]*),([^\n]*)[\\r\\n]*(#EXTVLCOPT:[^\n]*[\\r\\n]*)?(.*)"

    private var categorySet: TreeSet<String> = TreeSet()


    @JvmStatic
    fun parseM3U(context: Context, text: String): List<Channel> {
        categorySet = TreeSet()


        val result: MutableList<Channel> = ArrayList()


        //        final String reg2 =
//                "(?:\\s+tvg-id=\"([^\"]*)\")?" +
//                "(?:\\s+tvg-name=\"([^\"]*)\")?" +
//                "(?:\\s+tvg-logo=\"([^\"]*)\")?" +
//                "(?:\\s+group-title=\"([^\"]*)\")?";


// Регулярные выражения для каждого атрибута
        val idRegex = "tvg-id=\"([^\"]*)\""
        val nameRegex = "tvg-name=\"([^\"]*)\""
        val logoRegex = "tvg-logo=\"([^\"]*)\""
        val groupRegex = "group-title=\"([^\"]*)\""
        val userAgentRegex = "user-agent=\"([^\"]*)\""

        //+++
        val _tvgLanguage = "tvg-language=\"([^\"]*)\""
        val _tvgCountry = "tvg-country=\"([^\"]*)\""
        val _tvgUrl = "tvg-url=\"([^\"]*)\""

        val tvgLanguagePattern = Pattern.compile(_tvgLanguage)
        val tvgCountryPattern = Pattern.compile(_tvgCountry)
        val tvgUrlPattern = Pattern.compile(_tvgUrl)

        // Создаем паттерны
        val idPattern = Pattern.compile(idRegex)
        val namePattern = Pattern.compile(nameRegex)
        val logoPattern = Pattern.compile(logoRegex)
        val groupPattern = Pattern.compile(groupRegex)
        val userAgentPattern = Pattern.compile(userAgentRegex)

        val pattern = Pattern.compile(regex, Pattern.MULTILINE)
        val matcher = pattern.matcher(text)

        var k = 0
        while (matcher.find()) {
            ++k

            //            System.out.println("Full match: " + matcher.group(0));
//
//            for (int i = 1; i <= matcher.groupCount(); i++) {
//                System.out.println("Group " + i + ": " + matcher.group(i));
//            }
            //System.out.println(matcher.group());
//            Group 1: -1 tvg-id="" tvg-logo="" group-title="Undefined"
//            Group 2: 龙岩综合 (540p)
//            Group 3: http://stream.lytv.net.cn/2/sd/live.m3u8

            //String _link_ = matcher.group(3);
            val _link_ = matcher.group(4)
            val _name_ = matcher.group(2)
            val _extvlcopt_ = matcher.group(3)


            val channel = Channel()
            channel.type = "url"
            channel.lnk = _link_

            val name = clearTitle(_name_)
            var description = _name_

            val EXTVLCOPT = matcher.group(3)
            if (EXTVLCOPT != null) {
                val extUserAgent = extractValue(EXTVLCOPT, extVlCoptHttpUserPattern)
                val extReferer = extractValue(EXTVLCOPT, extVlCoptHttpReferrerPattern)

                //DLog.d(extReferer + "@@@@" + extUserAgent);
                channel.extUserAgent = extUserAgent
                channel.extReferer = extReferer
            }
            val line = matcher.group(1)
            if (line != null) {
                val tvGid = extractValue(line, idPattern)
                val tvgName = extractValue(line, namePattern)
                val tvgLogo = extractValue(line, logoPattern)
                var groupTitle = extractValue(line, groupPattern)
                val userAgent = extractValue(line, userAgentPattern)


                //
                val tvgLanguage = extractValue(line, tvgLanguagePattern)
                val tvgCountry = extractValue(line, tvgCountryPattern)
                val tvgUrl = extractValue(line, tvgUrlPattern)

                //
                groupTitle = clearGroup(groupTitle)

                if (groupTitle.contains(";")) {
                    val m = groupTitle.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    //@@
                    categorySet.addAll(listOf(*m))
                } else {
                    //@@
                    categorySet.add(groupTitle)
                }
                description =""
                description = if (BuildConfig.DEBUG) {
                    //if (!TextUtils.isEmpty(tvgName)) {
                    (description + " :: "
                            + tvgName
                            + "::" + _extvlcopt_
                            + "::" + tvgLanguage
                            + "::" + tvgCountry
                            + "::" + tvgUrl)
                    //}
                } else {
                    (description
                            + (if (TextUtils.isEmpty(tvgName)) "" else " :: $tvgName")
                            + (if (TextUtils.isEmpty(_extvlcopt_)) "" else " :: $_extvlcopt_"))
                }

                channel.ua = userAgent
                channel.name = name
                channel.tvgId = tvGid //empty or not
                channel.desc = description
                channel.cover = tvgLogo
                //@@@channel.country = groupTitle;
                channel.cat = groupTitle


                //@@@channel.lang = lang;
                channel.tvgLanguage = tvgLanguage
                channel.tvgCountry = tvgCountry
                channel.tvgUrl = tvgUrl

                //                DLog.d("-----------------------------------");
//                DLog.d(line);
                /*               d("tvg-id: " + (tvGid != null ? tvGid : "Not present"));
                * /                DLog.d("tvg-name: "+(tvgName != null ? tvgName : "Not present"));
                * /                DLog.d("tvg-logo: "+(tvgLogo != null ? tvgLogo : "Not present"));
                * /                DLog.d("group-title: "+(groupTitle != null ? groupTitle : "Not present")); */

//                DLog.d("-----------------------------------");
                if (BuildConfig.DEBUG) {
                    if (TextUtils.isEmpty(channel.name)) {
                        d("=NAME=> [" + tvGid + "] @@@@" + channel.lnk + " " + name)
                    } else if (TextUtils.isEmpty(channel.tvgId)) {
                        //DLog.d("=TV-GID=> [" + tvGid + "] @@@@" + channel.getLnk() + " " + name);
                    }
                }

                result.add(channel)
            }
        }

        if (BuildConfig.DEBUG) {
            val substring = "#EXTINF:"
            val occurrences = countOccurrences(text, substring)
            //11692
            d("@@@@" + result.size + "/" + occurrences)
        }

        val repo = LocalDatabaseRepo.getStoreInfoDatabase(context)
        repo.addCategory(categorySet)
        //System.out.println(k);
        return result
    }

    fun countOccurrences(text: String, substring: String): Int {
        var count = 0
        var index = 0

        while ((text.indexOf(substring, index).also { index = it }) != -1) {
            count++
            index += substring.length // Перемещаем индекс вперед на длину подстроки
        }

        return count
    }

    private fun clearGroup(trim: String?): String {
        //   $#[]./
        if (trim == null) {
            return ""
        }
        return trim.trim { it <= ' ' }.replace(".", "_")
    }

    private fun clearTitle(trim: String?): String? {
        //   $#[]./
        if (trim == null) {
            return trim
        }
        return trim.trim { it <= ' ' }.replace(".", "_")
    }

    private fun extractValue(line: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(line)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }
}
