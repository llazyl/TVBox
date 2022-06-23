package com.github.tvbox.osc.bean;

import com.github.tvbox.osc.cache.LocalSource;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.SourceState;
import com.github.tvbox.osc.spider.Spider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class SourceBean implements Serializable {
    public static SourceBean addNewBean = new SourceBean();
    public static SourceBean speedTestBean = new SourceBean();

    static {
        addNewBean.key = "_add_new_source";
        addNewBean.api = "";
        speedTestBean.key = "_source_speed_test";
        speedTestBean.api = "";
    }

    /**
     * name : 最大资源网
     * api : http://www.zdziyuan.com/inc/api.php
     * download : http://www.zdziyuan.com/inc/apidown.php
     */
    private String key;
    private String name;
    private String api;
    private int type;   // 0 xml 1 json
    private String playerUrl;
    private SourceState state;
    private LocalSource local;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setPlayerUrl(String playerUrl) {
        this.playerUrl = playerUrl;
    }

    public String getPlayerUrl() {
        return playerUrl;
    }

    public boolean isInternal() {
        return local == null;
    }

    public void initFromLocal(LocalSource local) {
        setKey(local.name);
        setName(local.name);
        setApi(local.api);
        setType(local.type);
        this.local = local;
        setPlayerUrl(local.playerUrl);
    }

    public void setState(SourceState state) {
        this.state = state;
    }

    public SourceState getState() {
        if (state == null) {
            state = new SourceState();
            state.sourceKey = getKey();
            state.active = true;
            state.home = false;
        }
        return state;
    }

    public boolean isHome() {
        return getState().home;
    }

    public void setHome(boolean home) {
        getState().home = home;
        RoomDataManger.addSourceState(getState());
    }

    public boolean isActive() {
        return getState().active;
    }

    public void setActive(boolean act) {
        getState().active = act;
        RoomDataManger.addSourceState(getState());
    }

    public boolean isAddition() {
        return this == addNewBean || this == speedTestBean;
    }

    public LocalSource getLocal() {
        return local;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTidSort(HashMap<Integer, Integer> tidSort) {
        getState().tidSort = new Gson().toJson(tidSort);
        RoomDataManger.addSourceState(getState());
    }

    public HashMap<Integer, Integer> getTidSort() {
        return new Gson().fromJson(state.tidSort, new TypeToken<HashMap<Integer, Integer>>() {
        }.getType());
    }
}