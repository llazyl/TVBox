package com.github.tvbox.osc.util;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoParseRuler {

    private static final HashMap<String, ArrayList<String>> HOSTS_RULE = new HashMap<>();

    public static void addHostRule(String host, ArrayList<String> rule) {
        HOSTS_RULE.put(host, rule);
    }

    @Nullable
    public static ArrayList<String> getHostRule(String host) {
        if (HOSTS_RULE.containsKey(host)) {
            return HOSTS_RULE.get(host);
        }
        return null;
    }

    public static boolean checkIsVideoForParse(String webUrl, String url) {
        try {
            boolean isVideo = DefaultConfig.isVideoFormat(url);
            if (webUrl == null || webUrl.isEmpty()) {
                return isVideo;
            }
            if (!isVideo) {
                Uri uri = Uri.parse(webUrl);
                ArrayList<String> hostRule = getHostRule(uri.getHost());
                if (hostRule != null && hostRule.size() > 0) {
                    boolean checkIsVideo = true;
                    for(int i=0; i<hostRule.size(); i++) {
                        if (!url.contains(hostRule.get(i))) {
                            checkIsVideo = false;
                            break;
                        }
                    }
                    if (checkIsVideo) {
                        isVideo = true;
                    }
                }
            }
            return isVideo;
        } catch (Exception e) {

        }
        return false;
    }

}
