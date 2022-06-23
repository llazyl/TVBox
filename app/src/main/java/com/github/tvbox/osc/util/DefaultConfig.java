package com.github.tvbox.osc.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/21
 * @description:
 */
public class DefaultConfig {

    public static List<MovieSort.SortData> adjustSort(String sourceKey, List<MovieSort.SortData> list, boolean withMy) {
        List<MovieSort.SortData> data = new ArrayList<>();
        SourceBean sb = ApiConfig.get().getSource(sourceKey);
        HashMap<Integer, Integer> tidSort = sb.getTidSort();
        for (MovieSort.SortData sortData : list) {
            // 默认排序 是 tid + 1000
            sortData.sort = sortData.id + 1000;
            if (tidSort != null && tidSort.containsKey(sortData.id))
                sortData.sort = tidSort.get(sortData.id);
            data.add(sortData);
        }
        if (withMy)
            data.add(0, new MovieSort.SortData(0, "我的"));
        Collections.sort(data);
        return data;
    }

    public static int getAppVersionCode(Context mContext) {
        //包管理操作管理类
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getAppVersionName(Context mContext) {
        //包管理操作管理类
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 后缀
     *
     * @param name
     * @return
     */
    public static String getFileSuffix(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        int endP = name.lastIndexOf(".");
        return endP > -1 ? name.substring(endP) : "";
    }

    /**
     * 获取文件的前缀
     *
     * @param fileName
     * @return
     */
    public static String getFilePrefixName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        int start = fileName.lastIndexOf(".");
        return start > -1 ? fileName.substring(0, start) : fileName;
    }

    public static boolean isVideoFormat(String urlOri) {
        String url = urlOri.toLowerCase();
        if (url.contains("=http") || url.contains("=https") || url.contains("=https%3a%2f") || url.contains("=http%3a%2f")) {
            return false;
        }
        Iterator<String> keys = videoFormatList.keySet().iterator();
        while (keys.hasNext()) {
            String format = keys.next();
            if (url.contains(format)) {
                LOG.i("isVideoFormat url:" + urlOri);
                return true;
            }
        }
        return false;
    }

    private static final HashMap videoFormatList = new HashMap<String, List<String>>() {{
        put(".m3u8", Arrays.asList("application/octet-stream", "application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl"));
        put(".mp4", Arrays.asList("video/mp4", "application/mp4", "video/h264"));
        put(".flv", Arrays.asList("video/x-flv"));
        put(".f4v", Arrays.asList("video/x-f4v"));
        put(".mpeg", Arrays.asList("video/vnd.mpegurl"));
    }};
}