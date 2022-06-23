package com.github.tvbox.osc.bean;

import com.github.tvbox.osc.cache.LocalLive;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LiveChannel implements Serializable {
    public static LiveChannel addNewBean = new LiveChannel();

    static {
        addNewBean.channelName = "_add_new_live";
    }

    public boolean isForAdd() {
        return this == addNewBean;
    }

    /**
     * channelNum : 1
     * channelName : CCTV-1 综合
     * channelUrl : http://117.148.187.37/PLTV/88888888/224/3221226154/index.m3u8
     * channelLogo : https://upload.wikimedia.org/wikipedia/zh/6/65/CCTV-1_Logo.png
     */

    private int channelNum;
    private String channelName;
    private ArrayList<String> channelUrl;
    private LocalLive local;
    private boolean isDefault;
    public int sourceIdx = 0;

    private LocalLive live;

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelUrl() {
        if (sourceIdx <= 0 || sourceIdx >= channelUrl.size())
            sourceIdx = 0;
        return channelUrl.get(sourceIdx);
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean b) {
        isDefault = b;
    }

    public boolean isInternal() {
        return local == null;
    }

    public LocalLive getLocal() {
        return local;
    }

    public void initFromLocal(LocalLive ll) {
        channelName = ll.name;
        channelUrl = new ArrayList<>();
        channelUrl.add(ll.url);
        local = ll;
    }
}