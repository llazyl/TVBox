package com.github.tvbox.osc.util;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.SourceBean;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;

public class SearchHelper {

    public static HashMap<String, SourceBean> getSourcesForSearch() {
        HashMap<String, SourceBean> mCheckSources = Hawk.get(HawkConfig.SOURCES_FOR_SEARCH, new HashMap<>());
        if (mCheckSources == null || mCheckSources.size() <= 0) {
            for (SourceBean bean : ApiConfig.get().getSourceBeanList()) {
                if (!bean.isSearchable()) {
                    continue;
                }
                mCheckSources.put(bean.getKey(), bean);
            }
            Hawk.put(HawkConfig.SOURCES_FOR_SEARCH, mCheckSources);
        }
        return mCheckSources;
    }

}
