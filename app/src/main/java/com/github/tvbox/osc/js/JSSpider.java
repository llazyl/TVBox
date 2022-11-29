package com.github.tvbox.osc.js;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class JSSpider extends Spider {

    public String key;

    public JSSpider(String key) {
        this.key = key;
    }

    public final String jsEval(String str) {
        String jsStr = str.replace("__JS_SPIDER__", "globalThis." + this.key);
        if (str.startsWith("__JS_SPIDER__.init")) {
            JSEngine.getInstance().getModule().executeVoidScript(jsStr, null);
            return "";
        }
        return JSEngine.getInstance().getModule().executeStringScript(jsStr, null);
    }

    @Override
    public final void init(Context context) {
        super.init(context);
    }

    @Override
    public final void init(Context context, String str) {
        String js;
        super.init(context, str);
        if (TextUtils.isEmpty(str)) {
            js = "__JS_SPIDER__.init('');";
        } else if (str.startsWith("{")) {
            js = "__JS_SPIDER__.init(" + str.trim() + ");";
        } else {
            str = str.replace("\"", "\\\"")
                    .replace("\n", "\"+\"\\n\"+\"")
                    .trim();
            js = "__JS_SPIDER__.init(\"" + str + "\");";
        }
        jsEval(js);
    }

    @Override
    public final String homeContent(boolean filter) {
        return jsEval("__JS_SPIDER__.home(" + filter + ");");
    }

    @Override
    public final String homeVideoContent() {
        return jsEval("__JS_SPIDER__.homeVod();");
    }

    @Override
    public final String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        JSONObject jsonObject = new JSONObject(extend);
        return jsEval("__JS_SPIDER__.category(\"" + tid.trim() + "\", \"" + pg.trim() + "\", " + filter + ", " + jsonObject.toString() + ");");
    }

    @Override
    public final String detailContent(List<String> ids) {
        return jsEval("__JS_SPIDER__.detail(\"" + ids.get(0) + "\");");
    }

    @Override
    public final String playerContent(String flag, String id, List<String> vipFlags) {
        JSONArray jsonArray = new JSONArray((Collection) vipFlags);
        return jsEval("__JS_SPIDER__.play(\"" + flag.trim() + "\", \"" + id.trim() + "\", " + jsonArray.toString() + ");");
    }

    @Override
    public final String searchContent(String key, boolean quick) {
        return jsEval("__JS_SPIDER__.search(\"" + key.trim() + "\", " + quick + ");");
    }
}
