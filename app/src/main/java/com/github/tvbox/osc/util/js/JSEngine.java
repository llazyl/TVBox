package com.github.tvbox.osc.util.js;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.quickjs.JSArray;
import com.github.tvbox.quickjs.JSCallFunction;
import com.github.tvbox.quickjs.JSModule;
import com.github.tvbox.quickjs.JSObject;
import com.github.tvbox.quickjs.QuickJSContext;
import com.lzy.okgo.OkGo;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JSEngine {
    private static final String TAG = "JSEngine";

    static JSEngine instance = null;

    public static JSEngine getInstance() {
        if (instance == null)
            instance = new JSEngine();
        return instance;
    }

    private Handler handler;
    private Thread thread;
    private QuickJSContext jsContext;
    private HashMap<String, JSObject> jsSpiders = new HashMap<>();


    public QuickJSContext getJsContext() {
        return jsContext;
    }

    public JSObject getGlobalObj() {
        return jsContext.getGlobalObject();
    }

    static String loadModule(String name) {
        try {
            if (name.startsWith("http://") || name.startsWith("https://")) {
                return OkGo.<String>get(name).execute().body().string();
            }
            if (name.startsWith("assets://")) {
                InputStream is = App.getInstance().getAssets().open(name.substring(9));
                byte[] data = new byte[is.available()];
                is.read(data);
                return new String(data, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void create() {
        System.loadLibrary("quickjs");
        Object[] objects = new Object[2];
        HandlerThread handlerThread = new HandlerThread("QuickJS-Thread");
        handlerThread.start();
        new Handler(handlerThread.getLooper()).post(() -> {
            this.thread = Thread.currentThread();
            this.handler = Looper.myLooper() != null ? new Handler(Looper.myLooper()) : null;
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
        jsContext = (QuickJSContext) objects[0];
        JSModule.setModuleLoader(new JSModule.Loader() {
            @Override
            public String getModuleScript(String moduleName) {
                return loadModule(moduleName);
            }
        });
        try {
            postVoid(() -> {
                initConsole();
                initOkHttp();
                initLocalStorage();
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void initConsole() {
        jsContext.evaluate("var console = {};");
        JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("console");
        console.setProperty("log", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                StringBuilder b = new StringBuilder();
                for (Object o : args) {
                    b.append(o == null ? "null" : o.toString());
                }
                System.out.println(TAG + " >>> " + b.toString());
                return null;
            }
        });
    }

    void initLocalStorage() {
        jsContext.evaluate("var local = {};");
        JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("local");
        console.setProperty("get", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                return sharedPreferences.getString(args[1].toString(), "");
            }
        });
        console.setProperty("set", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(args[1].toString(), args[2].toString()).commit();
                return null;
            }
        });
        console.setProperty("delete", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                SharedPreferences sharedPreferences = App.getInstance().getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                sharedPreferences.edit().remove(args[1].toString()).commit();
                return null;
            }
        });
    }

    void initOkHttp() {
        jsContext.getGlobalObject().setProperty("req", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                try {
                    String url = args[0].toString();
                    JSONObject opt = new JSONObject(jsContext.stringify((JSObject) args[1]));
                    Headers.Builder headerBuilder = new Headers.Builder();
                    JSONObject optHeader = opt.optJSONObject("headers");
                    if (optHeader != null) {
                        Iterator<String> hdKeys = optHeader.keys();
                        while (hdKeys.hasNext()) {
                            String k = hdKeys.next();
                            String v = optHeader.optString(k);
                            headerBuilder.add(k, v);
                        }
                    }
                    Headers headers = headerBuilder.build();
                    String method = opt.optString("method").toLowerCase();
                    Request.Builder requestBuilder = new Request.Builder().url(url).headers(headers);
                    Request request = null;
                    if (method.equals("post")) {
                        RequestBody body = RequestBody.create(MediaType.get(headers.get("content-type")), opt.optString("body", ""));
                        request = requestBuilder.post(body).build();
                    } else if (method.equals("header")) {
                        request = requestBuilder.head().build();
                    } else {
                        request = requestBuilder.get().build();
                    }
                    Response response = opt.optInt("redirect", 1) != 1 ? OkGoHelper.getDefaultClient().newCall(request).execute() : OkGoHelper.getNoRedirectClient().newCall(request).execute();
                    JSObject jsObject = jsContext.createNewJSObject();
                    Set<String> resHeaders = response.headers().names();
                    JSObject resHeader = jsContext.createNewJSObject();
                    for (String header : resHeaders) {
                        resHeader.setProperty(header, response.header(header));
                    }
                    jsObject.setProperty("headers", resHeader);
                    if (opt.optInt("buffer", 0) == 1) {
                        JSArray array = jsContext.createNewJSArray();
                        byte[] bytes = response.body().bytes();
                        for (int i = 0; i < bytes.length; i++) {
                            array.set(bytes[i], i);
                        }
                        jsObject.setProperty("content", array);
                    } else {
                        jsObject.setProperty("content", response.body().string());
                    }
                    return jsObject;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return "";
            }
        });
    }

    public void destroy() {
        if (thread != null) {
            thread.interrupt();
        }
        if (jsContext != null) {
            jsContext.destroyContext();
        }
        thread = null;
        jsContext = null;
    }

    public interface Event<T> {
        T run();
    }

    public <T> T post(Event<T> event) throws Throwable {
        if ((thread != null && thread.isInterrupted())) {
            Log.e("QuickJS", "QuickJS is released");
            return null;
        }
        if (Thread.currentThread() == thread) {
            return event.run();
        }
        if (handler == null) {
            return event.run();
        }
        Object[] result = new Object[2];
        RuntimeException[] errors = new RuntimeException[1];
        handler.post(() -> {
            try {
                result[0] = event.run();
            } catch (RuntimeException e) {
                errors[0] = e;
            }
            synchronized (result) {
                result[1] = true;
                result.notifyAll();
            }
        });
        synchronized (result) {
            try {
                if (result[1] == null) {
                    result.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (errors[0] != null) {
            throw errors[0];
        }
        return (T) result[0];
    }

    public void postVoid(Runnable event) throws Throwable {
        postVoid(event, true);
    }

    public void postVoid(Runnable event, boolean block) throws Throwable {
        if ((thread != null && thread.isInterrupted())) {
            Log.e("QuickJS", "QuickJS is released");
            return;
        }
        if (Thread.currentThread() == thread) {
            event.run();
            return;
        }
        if (handler == null) {
            event.run();
            return;
        }
        Object[] result = new Object[2];
        RuntimeException[] errors = new RuntimeException[1];
        handler.post(() -> {
            try {
                event.run();
            } catch (RuntimeException e) {
                errors[0] = e;
            }
            if (block) {
                synchronized (result) {
                    result[1] = true;
                    result.notifyAll();
                }
            }
        });
        if (block) {
            synchronized (result) {
                try {
                    if (result[1] == null) {
                        result.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (errors[0] != null) {
                throw errors[0];
            }
        }
    }
}
