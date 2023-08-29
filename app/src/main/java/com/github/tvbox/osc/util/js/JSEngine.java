package com.github.tvbox.osc.util.js;

import android.os.Handler;
import android.os.HandlerThread;

import com.github.tvbox.osc.util.FileUtils;
import com.google.android.exoplayer2.util.UriUtil;
import com.whl.quickjs.wrapper.JSModule;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import java.util.concurrent.ConcurrentHashMap;

public class JSEngine {

    private static JSEngine instance = null;

    public static JSEngine getInstance() {
        if (instance == null)
            instance = new JSEngine();
        return instance;
    }

    private ConcurrentHashMap<String, JSThread> threads = new ConcurrentHashMap<>();

    public JSThread getJSThread(Class<?> cls) {
        byte count = Byte.MAX_VALUE;
        JSThread thread = null;
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread != null && jsThread.retain < count && jsThread.retain < 1) {
                thread = jsThread;
                count = jsThread.retain;
            }
        }
        if (thread == null) {
            Object[] objects = new Object[2];
            String name = "QuickJS-Thread-" + threads.size();
            HandlerThread handlerThread = new HandlerThread(name + "-0");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            handler.post(() -> {
                objects[0] = QuickJSContext.create();
                synchronized (objects) {
                    objects[1] = true;
                    objects.notify();
                }
            });
            synchronized (objects) {
                try {
                    if (objects[1] == null) {
                        objects.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            JSModule.setModuleLoader(new JSModule.ModuleLoader() {
                @Override
                public String convertModuleName(String moduleBaseName, String moduleName) {
                    return UriUtil.resolve(moduleBaseName, moduleName);
                }
                @Override
                public String getModuleScript(String moduleName) {
                    return FileUtils.loadModule(moduleName);
                }
            });
            JSThread jsThread = new JSThread();
            jsThread.handler = handler;
            jsThread.thread = handlerThread;
            jsThread.jsContext = (QuickJSContext) objects[0];
            jsThread.retain = 0;
            thread = jsThread;
            try {
                jsThread.postVoid((ctx, globalThis) -> {
                    jsThread.init(cls);
                    return null;
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            threads.put(name, jsThread);
        }
        thread.retain++;
        String name = thread.thread.getName();
        name = name.substring(0, name.lastIndexOf("-") + 1) + thread.retain;
        thread.thread.setName(name);
        return thread;
    }

    public void destroy() {
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread != null) {
                if (jsThread.thread != null) {
                    jsThread.thread.interrupt();
                }
                if (jsThread.jsContext != null) {
                    jsThread.jsContext.destroy();
                }
            }
        }
        threads.clear();
    }

    public void stopAll() {
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread != null) {
                jsThread.cancelByTag("js_okhttp_tag");
                if (jsThread.handler != null) {
                    jsThread.handler.removeCallbacksAndMessages(null);
                }
            }
        }
    }

    public interface Event<T> {
        T run(QuickJSContext ctx, JSObject globalThis);
    }
}