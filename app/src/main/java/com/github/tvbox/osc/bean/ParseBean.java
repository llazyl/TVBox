package com.github.tvbox.osc.bean;

import java.io.Serializable;

import com.github.tvbox.osc.cache.LocalParse;

/**
 * @author pj567
 * @date :2021/3/8
 * @description:
 */
public class ParseBean implements Serializable {
    public static ParseBean addNewBean = new ParseBean();

    static {
        addNewBean.parseName = "_add_new_parse";
    }

    private String parseName;
    private String parseUrl;

    private boolean isDefault = false;

    private LocalParse local;

    public String getParseName() {
        return parseName;
    }

    public void setParseName(String parseName) {
        this.parseName = parseName;
    }

    public String getParseUrl() {
        return parseUrl;
    }

    public void setParseUrl(String parseUrl) {
        this.parseUrl = parseUrl;
    }

    public void initFromLocal(LocalParse lp) {
        setParseName(lp.name);
        setParseUrl(lp.url);
        local = lp;
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

    public LocalParse getLocal() {
        return local;
    }


    public boolean isForAdd() {
        return this == addNewBean;
    }
}