package com.github.tvbox.osc.js;

import android.webkit.JavascriptInterface;

import com.orhanobut.hawk.Hawk;

public class Local {

    @JavascriptInterface
    public void delete(String str, String str2) {
        try {
            Hawk.delete("jsRuntime_" + str + "_" + str2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public String get(String str, String str2) {
        try {
            return (String) Hawk.get("jsRuntime_" + str + "_" + str2, "");
        } catch (Exception unused) {
            return str2;
        }
    }

    @JavascriptInterface
    public void set(String str, String str2, String str3) {
        try {
            Hawk.put("jsRuntime_" + str + "_" + str2, str3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
