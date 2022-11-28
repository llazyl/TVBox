package com.github.tvbox.osc.js;

import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.OkGoHelper;
import com.quickjs.QuickJS;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;

public class ES6Module extends com.quickjs.ES6Module {

    protected String[] modules = {"cheerio.min.js", "crypto-js.js", "dayjs.min.js", "uri.min.js", "underscore-esm-min.js"};

    public ES6Module(QuickJS quickJS) {
        super(quickJS);
    }


    @Override
    public String getModuleScript(String name) {
        try {
            for (String one : modules) {
                if (name.contains(one)) {
                    name = one;
                    break;
                }
            }
            String content = "";
            if (name.startsWith("http://") || name.startsWith("https://")) {
                Map<String, String> header = new HashMap<>();
                header.put("User-Agent", "Mozilla/5.0");
                Request request = new Request.Builder().url(name).headers(Headers.of(header)).build();
                content = OkGoHelper.getDefaultClient().newCall(request).execute().body().string();
            } else if (name.startsWith("assets://")) {
                content = FileUtils.getAssetFile(name.substring(9));
            } else if (FileUtils.isAssetFile(name, "js/lib")) {
                content = FileUtils.getAssetFile("js/lib/" + name);
            }
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String convertModuleName(String moduleBaseName, String moduleName) {
        return super.convertModuleName(moduleBaseName, moduleName);
    }

}
