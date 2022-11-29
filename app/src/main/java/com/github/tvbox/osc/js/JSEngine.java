package com.github.tvbox.osc.js;

import android.net.UrlQuerySanitizer;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.MD5;
import com.github.tvbox.osc.util.OkGoHelper;
import com.lzy.okgo.OkGo;
import com.quickjs.QuickJS;
import com.quickjs.plugin.ConsolePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Headers;
import okhttp3.Request;

public class JSEngine {

    private static volatile JSEngine instance;
    private ES6Module module;
    private QuickJS quickJS;
    private final ConcurrentHashMap<String, Spider> spiders = new ConcurrentHashMap<>();

    private JSEngine() {

    }

    public static JSEngine getInstance() {
        if (instance == null) {
            synchronized (JSEngine.class) {
                if (instance == null) {
                    instance = new JSEngine();
                }
            }
        }
        return instance;
    }

    public void init() {
        try {
            quickJS = QuickJS.createRuntimeWithEventQueue();
            module = new ES6Module(quickJS);
            registerPluginsAndMethods();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public ES6Module getModule() {
        return module;
    }

    public Spider getSpider(SourceBean sourceBean) {
        if (sourceBean.getExt().length() == 0) return new SpiderNull();
        if (spiders.containsKey(sourceBean.getKey())) return spiders.get(sourceBean.getKey());
        String key = "J" + MD5.string2MD5(sourceBean.getKey() + System.currentTimeMillis());
        String moduleJsStr = "";
        String moduleName = "";
        if (sourceBean.getApi().startsWith("js_")) {
            moduleJsStr = loadModule(sourceBean.getApi());
            moduleName = sourceBean.getExt();
        } else {
            String modJsStr = loadModule(sourceBean.getApi());
            if (sourceBean.getApi().contains(".js?")) {
                int indexOf = sourceBean.getApi().indexOf(".js?");
                String query = sourceBean.getApi().substring(indexOf + 4);
                String moduleBaseName = sourceBean.getApi().substring(0, indexOf);
                moduleName = moduleBaseName;
                UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
                sanitizer.setAllowUnregisteredParamaters(true);
                sanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
                sanitizer.parseQuery(query);
                for (String param : sanitizer.getParameterSet()) {
                    String paramVal = sanitizer.getValue(param);
                    String curModuleName = module.convertModuleName(moduleBaseName, paramVal);
                    String content = loadModule(curModuleName);
                    modJsStr = modJsStr.replace("__" + param.toUpperCase() + "__", content);
                }
            }
            moduleJsStr = modJsStr;
        }
        if (TextUtils.isEmpty(moduleJsStr)) return new SpiderNull();
        if (moduleJsStr.contains("export default{")) {
            moduleJsStr = moduleJsStr.replace("export default{", "globalThis." + key + " ={");
        } else if (moduleJsStr.contains("export default {")) {
            moduleJsStr = moduleJsStr.replace("export default {", "globalThis." + key + " ={");
        } else {
            moduleJsStr = moduleJsStr.replace("__JS_SPIDER__", "globalThis." + key);
        }
        module.setModuleUrl(sourceBean.getApi());
        module.executeModuleScript(moduleJsStr, moduleName);
        JSSpider spider = new JSSpider(key);
        String extJs = sourceBean.getExt().startsWith("http") ?  sourceBean.getExt() : loadExt(sourceBean.getExt());
        spider.init(App.getInstance(), extJs);
        spiders.put(sourceBean.getKey(), spider);
        return spider;
    }

    private String loadExt(String ext) {
        try {
            return loadJs(ext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private String loadModule(String name) {
        try {
            return loadJs(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Map<String, String> jsCache = new ConcurrentHashMap<>();

    private String loadJs(String name) throws Exception {
        if (jsCache.containsKey(name)) return jsCache.get(name);
        String content = null;
        if (name.startsWith("clan://")) name = ApiConfig.get().clanToAddress(name);
        if (name.startsWith("http://") || name.startsWith("https://")) {
            Map<String, String> header = new HashMap<>();
            header.put("User-Agent", "Mozilla/5.0");
            Request request = new Request.Builder().url(name).headers(Headers.of(header)).build();
            content = OkGoHelper.getDefaultClient().newCall(request).execute().body().string();
        } else if (name.startsWith("assets://")) {
            content = FileUtils.getAssetFile(name.substring(9));
        } else if (name.startsWith("file://")) {
            content = FileUtils.read(name);
        } else {
            content = name;
        }
        if (TextUtils.isEmpty(content)) return "";
        jsCache.put(name, content);
        return content;
    }

    protected void registerPluginsAndMethods() {
        module.addPlugin(new ConsolePlugin());
        module.addJavascriptInterface(new Local(), "local"); //适配drpy的js
        module.addJavascriptInterface(new DrpyMethods(module), "drpymethods"); //适配drpy的js
        Map<String, String> alias = new HashMap<>();
        alias.put("req", "drpymethods.ajax");
        alias.put("joinUrl", "drpymethods.joinUrl");
        alias.put("pdfh", "drpymethods.parseDomForHtml");
        alias.put("pd", "drpymethods.parseDom");
        alias.put("pdfa", "drpymethods.parseDomForArray");
        for (String one : alias.keySet()) {
            String methodName = alias.get(one);
            module.executeVoidScript("var " + one + " = " + methodName + ";\n", null);
        }
    }

    public void stopAll() {
        OkGo.getInstance().cancelTag("js_okhttp_tag");
        clearAll();
    }

    public void clearAll() {
        if (quickJS == null) return;
        clear();
        module.close();
        quickJS.close();
        module = null;
        quickJS = null;
    }

    public void clear() {
        spiders.clear();
        jsCache.clear();
    }

}
