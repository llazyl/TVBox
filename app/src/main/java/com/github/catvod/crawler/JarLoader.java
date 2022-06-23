package com.github.catvod.crawler;

import android.content.Context;

import com.github.tvbox.osc.base.App;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class JarLoader {
    private DexClassLoader classLoader = null;
    private ConcurrentHashMap<String, Spider> spiders = new ConcurrentHashMap<>();

    /**
     * 不要在主线程调用我
     *
     * @param context
     * @param jarData
     */
    public boolean load(Context context, byte[] jarData) {
        spiders.clear();
        boolean success = false;
        try {
            File cacheDir = new File(context.getCacheDir().getAbsolutePath() + "/catvod_csp");
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            String cache = context.getCacheDir().getAbsolutePath() + "/catvod_csp.jar";
            FileOutputStream fos = new FileOutputStream(cache);
            fos.write(jarData);
            fos.flush();
            fos.close();
            classLoader = new DexClassLoader(cache, cacheDir.getAbsolutePath(), null, context.getClassLoader());
            // make force wait here, some device async dex load
            int count = 0;
            do {
                try {
                    if (classLoader.loadClass("com.github.catvod.spider.Init") != null) {
                        success = true;
                        break;
                    }
                    Thread.sleep(200);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                count++;
            } while (count < 5);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return success;
    }

    public Spider getSpider(String key, String ext) {
        String clsKey = key.replace("csp_", "");
        if (spiders.contains(clsKey))
            return spiders.get(clsKey);
        if (classLoader == null)
            return new SpiderNull();
        try {
            Spider sp = (Spider) classLoader.loadClass("com.github.catvod.spider." + clsKey).newInstance();
            sp.init(App.getInstance(), ext);
            spiders.put(clsKey, sp);
            return sp;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return new SpiderNull();
    }
}
