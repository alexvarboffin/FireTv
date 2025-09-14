package com.walhalla.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.walhalla.data.model.Channel;


import com.walhalla.ui.BuildConfig;
import com.walhalla.ui.DLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class M3UParser {

    //http-user-agent OR http-referrer without quotes ""
    static final String extVlCoptHttpUserAgent = "http-user-agent=([^\"]*)";
    static final String extVlCoptHttpReferrer = "http-referrer=([^\"]*)";

    static final Pattern extVlCoptHttpUserPattern = Pattern.compile(extVlCoptHttpUserAgent);
    static final Pattern extVlCoptHttpReferrerPattern = Pattern.compile(extVlCoptHttpReferrer);


    static final String regex = "(?:^|\\n)#EXTINF:([^,]*),([^\n]*)[\\r\\n]*(#EXTVLCOPT:[^\n]*[\\r\\n]*)?(.*)";

    private static TreeSet<String> categorySet;


    public static List<Channel> parseM3U(Context context, String text) {

        categorySet = new TreeSet<>();


        List<Channel> result = new ArrayList<>();
//        final String reg2 =
//                "(?:\\s+tvg-id=\"([^\"]*)\")?" +
//                "(?:\\s+tvg-name=\"([^\"]*)\")?" +
//                "(?:\\s+tvg-logo=\"([^\"]*)\")?" +
//                "(?:\\s+group-title=\"([^\"]*)\")?";


// Регулярные выражения для каждого атрибута
        final String idRegex = "tvg-id=\"([^\"]*)\"";
        final String nameRegex = "tvg-name=\"([^\"]*)\"";
        final String logoRegex = "tvg-logo=\"([^\"]*)\"";
        final String groupRegex = "group-title=\"([^\"]*)\"";
        final String userAgentRegex = "user-agent=\"([^\"]*)\"";

        //+++
        final String _tvgLanguage = "tvg-language=\"([^\"]*)\"";
        final String _tvgCountry = "tvg-country=\"([^\"]*)\"";
        final String _tvgUrl = "tvg-url=\"([^\"]*)\"";

        final Pattern tvgLanguagePattern = Pattern.compile(_tvgLanguage);
        final Pattern tvgCountryPattern = Pattern.compile(_tvgCountry);
        final Pattern tvgUrlPattern = Pattern.compile(_tvgUrl);

// Создаем паттерны
        final Pattern idPattern = Pattern.compile(idRegex);
        final Pattern namePattern = Pattern.compile(nameRegex);
        final Pattern logoPattern = Pattern.compile(logoRegex);
        final Pattern groupPattern = Pattern.compile(groupRegex);
        final Pattern userAgentPattern = Pattern.compile(userAgentRegex);

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(text);

        int k = 0;
        while (matcher.find()) {
            ++k;

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
            String _link_ = matcher.group(4);
            String _name_ = matcher.group(2);
            String _extvlcopt_ = matcher.group(3);


            Channel channel = new Channel();
            channel.setType("url");
            channel.setLnk(_link_);

            String name = clearTitle(_name_);
            String description = _name_;

            final String EXTVLCOPT = matcher.group(3);
            if (EXTVLCOPT != null) {
                final String extUserAgent = extractValue(EXTVLCOPT, extVlCoptHttpUserPattern);
                final String extReferer = extractValue(EXTVLCOPT, extVlCoptHttpReferrerPattern);

                //DLog.d(extReferer + "@@@@" + extUserAgent);

                channel.extUserAgent = extUserAgent;
                channel.extReferer = extReferer;
            }
            String line = matcher.group(1);
            if (line != null) {
                String tvGid = extractValue(line, idPattern);
                String tvgName = extractValue(line, namePattern);
                String tvgLogo = extractValue(line, logoPattern);
                String groupTitle = extractValue(line, groupPattern);
                String userAgent = extractValue(line, userAgentPattern);


                //
                final String tvgLanguage = extractValue(line, tvgLanguagePattern);
                final String tvgCountry = extractValue(line, tvgCountryPattern);
                final String tvgUrl = extractValue(line, tvgUrlPattern);
                //

                groupTitle = clearGroup(groupTitle);

                if (groupTitle.contains(";")) {
                    String[] m = groupTitle.split(";");
                    //@@
                    categorySet.addAll(Arrays.asList(m));
                } else {
                    //@@
                    categorySet.add(groupTitle);
                }

                if (BuildConfig.DEBUG) {
                    //if (!TextUtils.isEmpty(tvgName)) {
                    description = description + " :: "
                            + tvgName
                            + "::" + _extvlcopt_
                            + "::" + tvgLanguage
                            + "::" + tvgCountry
                            + "::" + tvgUrl


                    ;
                    //}
                } else {
                    description = description
                            + (TextUtils.isEmpty(tvgName) ? "" : " :: " + tvgName)
                            + (TextUtils.isEmpty(_extvlcopt_) ? "" : " :: " + _extvlcopt_);
                }

                channel.setUa(userAgent);
                channel.setName(name);
                channel.setTvgId(tvGid);//empty or not
                channel.setDesc(description);
                channel.setCover(tvgLogo);
                //@@@channel.country = groupTitle;
                channel.setCat(groupTitle);
                //@@@channel.lang = lang;


                channel.tvgLanguage = tvgLanguage;
                channel.tvgCountry = tvgCountry;
                channel.tvgUrl = tvgUrl;

//                DLog.d("-----------------------------------");
//                DLog.d(line);
////                DLog.d("tvg-id: " + (tvGid != null ? tvGid : "Not present"));
////                DLog.d("tvg-name: " + (tvgName != null ? tvgName : "Not present"));
////                DLog.d("tvg-logo: " + (tvgLogo != null ? tvgLogo : "Not present"));
////                DLog.d("group-title: " + (groupTitle != null ? groupTitle : "Not present"));

//                DLog.d("-----------------------------------");

                if (BuildConfig.DEBUG) {
                    if (TextUtils.isEmpty(channel.getName())) {
                        DLog.d("=NAME=> [" + tvGid + "] @@@@" + channel.getLnk() + " " + name);
                    } else if (TextUtils.isEmpty(channel.getTvgId())) {
                        //DLog.d("=TV-GID=> [" + tvGid + "] @@@@" + channel.getLnk() + " " + name);
                    }
                }

                result.add(channel);
            }


        }

        if (BuildConfig.DEBUG) {

            String substring = "#EXTINF:";
            int occurrences = countOccurrences(text, substring);
            //11692
            DLog.d("@@@@" + result.size() + "/" + occurrences);
        }

        LocalDatabaseRepo repo = LocalDatabaseRepo.getStoreInfoDatabase(context);
        repo.addCategory(categorySet);
        //System.out.println(k);
        return result;
    }

    public static int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length(); // Перемещаем индекс вперед на длину подстроки
        }

        return count;
    }

    private static String clearGroup(String trim) {
        //   $#[]./
        if (trim == null) {
            return "";
        }
        return trim.trim().replace(".", "_");
    }

    private static String clearTitle(String trim) {
        //   $#[]./
        if (trim == null) {
            return trim;
        }
        return trim.trim().replace(".", "_");
    }

    private static String extractValue(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
