package com.github.tvbox.osc.js;

import android.net.Uri;
import android.text.TextUtils;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.OkGoHelper;
import com.quickjs.QuickJS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Headers;
import okhttp3.Request;

public class ES6Module extends com.quickjs.ES6Module {

    private Map<String, String> moduleCache = new ConcurrentHashMap<>();
    protected String[] modules = {"cheerio.min.js", "crypto-js.js", "dayjs.min.js", "uri.min.js", "underscore-esm-min.js"};
    private String moduleUrl = "";

    public ES6Module(QuickJS quickJS) {
        super(quickJS);
    }

    public void setModuleUrl(String url) {
        if (url.startsWith("clan://")) url = ApiConfig.get().clanToAddress(url);
        moduleUrl = url;
    }

    @Override
    public String getModuleScript(String name) {
        if (moduleCache.containsKey(name)) return moduleCache.get(name);
        try {
            for (String one : modules) {
                if (name.contains(one)) {
                    name = one;
                    break;
                }
            }
            String content = "";
            if (name.startsWith("http:") || name.startsWith("https:")) {
                content = getModuleContent(name);
            } else if (name.startsWith("assets://")) {
                content = FileUtils.getAssetFile(name.substring(9));
            } else if (FileUtils.isAssetFile(name, "js/lib")) {
                content = FileUtils.getAssetFile("js/lib/" + name);
            } else {
                String curModuleUrl = "";
                if (name.startsWith(".")) name = name.substring(1);
                if (name.startsWith("/")) name = name.substring(1);
                Uri uri = Uri.parse(moduleUrl);
                if (uri.getLastPathSegment() == null) {
                    curModuleUrl = uri.getScheme() + "://" + name;
                } else {
                    curModuleUrl = uri.toString().replace(uri.getLastPathSegment(), name);
                }
                content = getModuleContent(curModuleUrl);
            }
            if (TextUtils.isEmpty(content)) return "";
            moduleCache.put(name, content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getModuleContent(String url) throws Exception {
        if (url.startsWith("file://")) return FileUtils.read(url);
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", "Mozilla/5.0");
        Request request = new Request.Builder().url(url).headers(Headers.of(header)).build();
        return OkGoHelper.getDefaultClient().newCall(request).execute().body().string();
    }

    @Override
    public String convertModuleName(String moduleBaseName, String moduleName) {
        return super.convertModuleName(moduleBaseName, moduleName);
    }

}
