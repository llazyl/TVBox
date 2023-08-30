package com.github.tvbox.osc.util.js;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.MD5;


import com.github.tvbox.osc.util.StringUtils;
import com.whl.quickjs.wrapper.JSArray;
import com.whl.quickjs.wrapper.JSObject;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SpiderJS extends Spider {

    private String key;
    private String js;
    private String ext;
    private Class<?> cls;
    private JSObject jsObject = null;
    private JSThread jsThread = null;
    private final Pattern pattern = Pattern.compile("/\\*.+?\\*/", Pattern.DOTALL);

    public SpiderJS(String key, String js, String ext, Class<?> cls) {
        this.key = key;
        this.js = js;
        this.ext = ext;
        this.cls = cls;
    }

    private void checkLoaderJS() {
        if (jsThread == null) {
            jsThread = JSEngine.getInstance().getJSThread(cls);
        }
        if (jsObject == null && jsThread != null) {
            try {
                jsThread.postVoid((ctx, globalThis) -> {
                    String moduleKey = "__" + MD5.encode(key) + "__";
                    String jsContent = FileUtils.loadModule(js);

                    if(jsContent.contains("export default{") || jsContent.contains("export default {")){
                        jsContent = jsContent.replaceAll("export default.*?[{]", "globalThis." + moduleKey+" = {");
                    } else {
                        jsContent = jsContent.replace("__JS_SPIDER__", "globalThis." + moduleKey);
                    }
                    ctx.evaluateModule(jsContent, js);
                    jsObject = (JSObject) ctx.getProperty(globalThis, moduleKey);
                    jsObject.getJSFunction("init").call(ext);
                    //ext = FileUtils.loadModule(ext);
                    //jsObject.getJSFunction("init").call(StringUtils.isJsonType(ext)?ctx.parse(ext):ext);
                    return null;
                });
            } catch (Throwable throwable) {
          //      LOG.e(throwable);
            }
        }
    }

    private <T> T postFunc(String func, Object... args) {
        checkLoaderJS();
        try {
            if (jsObject != null) {

                return jsThread.post((ctx, globalThis) -> (T) jsObject.getJSFunction(func).call(args));

            }
        } catch (Throwable throwable) {
      //      LOG.e(throwable);
        }
        return null;
    }

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        checkLoaderJS();
    }

    @Override
    public String homeContent(boolean filter) {
        return postFunc("home", filter);
    }

    @Override
    public String homeVideoContent() {
        return postFunc("homeVod");
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            JSObject obj = jsThread.post((ctx, globalThis) -> {
                JSObject o = ctx.createNewJSObject();
                if (extend != null) {
                    for (String s : extend.keySet()) {
                        o.setProperty(s, extend.get(s));
                    }
                }
                return o;
            });
            return postFunc("category", tid, pg, filter, obj);
        } catch (Throwable throwable) {
       //     LOG.e(throwable);
            return "";
        }
    }

    @Override
    public String detailContent(List<String> ids) {
        return postFunc("detail", ids.get(0));
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            JSArray array = jsThread.post((ctx, globalThis) -> {
                JSArray arr = ctx.createNewJSArray();
                if (vipFlags != null) {
                    for (int i = 0; i < vipFlags.size(); i++) {
                        arr.set(vipFlags.get(i), i);
                    }
                }
                return arr;
            });
            return postFunc("play", flag, id, array);
        } catch (Throwable throwable) {
         //   LOG.e(throwable);
            return "";
        }
    }

    @Override
    public String searchContent(String key, boolean quick) {
        return postFunc("search", key, quick);
    }

    public Object[] proxyInvoke(Map<String, String> params) {
        checkLoaderJS();
        try {
            return jsThread.post((ctx, globalThis) -> {
                try {
                    JSObject o = ctx.createNewJSObject();
                    if (params != null) {
                        for (String s : params.keySet()) {
                            o.setProperty(s, params.get(s));
                        }
                    }
                    //JSONArray opt = ((JSArray) jsObject.getJSFunction("proxy").call(new Object[]{o})).toJSONArray();
                    JSONArray opt = new JSONArray(((JSArray) jsObject.getJSFunction("proxy").call(new Object[]{o})).stringify());
                    Object[] result = new Object[3];
                    result[0] = opt.opt(0);
                    result[1] = opt.opt(1);
                    Object obj = opt.opt(2);
                    ByteArrayInputStream baos;
                    if (obj instanceof JSONArray) {
                        JSONArray json = (JSONArray) obj;
                        byte[] b = new byte[json.length()];
                        for (int i = 0; i < json.length(); i++) {
                            b[i] = (byte) json.optInt(i);
                        }
                        baos = new ByteArrayInputStream(b);
                    } else {
                        baos = new ByteArrayInputStream(opt.opt(2).toString().getBytes());
                    }
                    result[2] = baos;
                    return result;
                } catch (Throwable throwable) {
               //     LOG.e(throwable);
                    return new Object[0];
                }
            });
        } catch (Throwable throwable) {
      //      LOG.e(throwable);
            return new Object[0];
        }
    }

    @Override
    public boolean manualVideoCheck() {
        return Boolean.TRUE.equals(postFunc("sniffer"));
    }

    @Override
    public boolean isVideoFormat(String url) {
        return Boolean.TRUE.equals(postFunc("isVideo", url));
    }
}