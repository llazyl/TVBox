package com.github.tvbox.osc.bean;

/**
 * @author pj567
 * @date :2021/3/8
 * @description:
 */
public class ParseBean {

    private String name;
    private String url;
    private int type;   // 0 普通嗅探 1 json 2 Json扩展 3 聚合

    private boolean isDefault = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean b) {
        isDefault = b;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}