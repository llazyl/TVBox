package com.github.tvbox.osc.util;

import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class VideoParseRuler {

    private static final HashMap<String, ArrayList<ArrayList<String>>> HOSTS_RULE = new HashMap<>();

    public static void addHostRule(String host, ArrayList<String> rule) {
        ArrayList<ArrayList<String>> rules = new ArrayList<>();
        if (HOSTS_RULE.get(host) != null && HOSTS_RULE.get(host).size() > 0) {
            rules = HOSTS_RULE.get(host);
        }
        rules.add(rule);
        HOSTS_RULE.put(host, rules);
    }

    public static ArrayList<ArrayList<String>> getHostRules(String host) {
        if (HOSTS_RULE.containsKey(host)) {
            return HOSTS_RULE.get(host);
        }
        return null;
    }

    public static boolean checkIsVideoForParse(String webUrl, String url) {
        try {
            boolean isVideo = DefaultConfig.isVideoFormat(url);
            if (!HOSTS_RULE.isEmpty() && !isVideo && webUrl != null) {
                Uri uri = Uri.parse(webUrl);
                if(getHostRules(uri.getHost()) != null){
                    isVideo = checkVideoForOneHostRules(uri.getHost(), url);
                }else {
                    isVideo = checkVideoForOneHostRules("*", url);
                }
            }
            return isVideo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkVideoForOneHostRules(String host, String url) {
        boolean isVideo = false;
        ArrayList<ArrayList<String>> hostRules = getHostRules(host);
        if (hostRules != null && hostRules.size() > 0) {
            boolean isVideoRuleCheck = false;
            for(int i=0; i<hostRules.size(); i++) {
                boolean checkIsVideo = true;
                if (hostRules.get(i) != null && hostRules.get(i).size() > 0) {
                    for(int j=0; j<hostRules.get(i).size(); j++) {
                        Pattern onePattern = Pattern.compile("" + hostRules.get(i).get(j));
                        if (!onePattern.matcher(url).find()) {
                            checkIsVideo = false;
                            break;
                        }
                        LOG.i("RULE:" + hostRules.get(i).get(j));
                    }
                } else {
                    checkIsVideo = false;
                }
                if (checkIsVideo) {
                    isVideoRuleCheck = true;
                    break;
                }
            }
            if (isVideoRuleCheck) {
                isVideo = true;
            }
        }
        return isVideo;
    }

}
