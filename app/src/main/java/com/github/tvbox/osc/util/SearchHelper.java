package com.github.tvbox.osc.util;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.SourceBean;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;

public class SearchHelper {

    public static HashMap<String, String> getSourcesForSearch() {
        String api = Hawk.get(HawkConfig.API_URL, "");
        if (api.isEmpty()) {
            return null;
        }
        HashMap<String, String> mCheckSources = new HashMap<>();
        try {
            HashMap<String, HashMap<String, String>> mCheckSourcesForApi = Hawk.get(HawkConfig.SOURCES_FOR_SEARCH, new HashMap<String, HashMap<String, String>>());
            mCheckSources = mCheckSourcesForApi.get(api);
        } catch (Exception e) {

        }
        if (mCheckSources == null || mCheckSources.size() <= 0) {
            if (mCheckSources == null) {
                mCheckSources = new HashMap<String, String>();
            }
            for (SourceBean bean : ApiConfig.get().getSourceBeanList()) {
                if (!bean.isSearchable()) {
                    continue;
                }
                mCheckSources.put(bean.getKey(), "1");
            }
        }
        return mCheckSources;
    }

    public static void putCheckedSources(HashMap<String, String> mCheckSources) {
        String api = Hawk.get(HawkConfig.API_URL, "");
        if (api.isEmpty()) {
            return;
        }
        HashMap<String, HashMap<String, String>> mCheckSourcesForApi = Hawk.get(HawkConfig.SOURCES_FOR_SEARCH, new HashMap<String, HashMap<String, String>>());
        if (mCheckSourcesForApi == null || mCheckSourcesForApi.isEmpty()) {
            mCheckSourcesForApi = new HashMap<String, HashMap<String, String>>();
        }
        mCheckSourcesForApi.put(api, mCheckSources);
        Hawk.put(HawkConfig.SOURCES_FOR_SEARCH, mCheckSourcesForApi);
    }

}
