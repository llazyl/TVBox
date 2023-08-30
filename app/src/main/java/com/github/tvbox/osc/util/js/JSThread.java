package com.github.tvbox.osc.util.js;

import android.os.Handler;
import android.util.Base64;

import androidx.annotation.Keep;

import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.OkGoHelper;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;

import com.github.tvbox.osc.util.StringUtils;
import com.whl.quickjs.wrapper.JSArray;
import com.whl.quickjs.wrapper.JSCallFunction;
import com.whl.quickjs.wrapper.JSMethod;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JSThread {
    public QuickJSContext jsContext;
    public Handler handler;
    public Thread thread;
    public byte retain;
    private static final String TAG = "JSEngine";
    private OkHttpClient okHttpClient;

    public QuickJSContext getJsContext() {
        return jsContext;
    }

    public JSObject getGlobalObj() {
        return jsContext.getGlobalObject();
    }

    public <T> T post(JSEngine.Event<T> event) throws Throwable {
        if ((thread != null && thread.isInterrupted())) {
           // LOG.e("QuickJS", "QuickJS is released");
            return null;
        }
        if (Thread.currentThread() == thread) {
            return event.run(getJsContext(), getGlobalObj());
        }
        if (handler == null) {
            return event.run(getJsContext(), getGlobalObj());
        }
        Object[] result = new Object[2];
        RuntimeException[] errors = new RuntimeException[1];
        handler.post(() -> {
            try {
                result[0] = event.run(getJsContext(), getGlobalObj());
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
               // LOG.e(e);
            }
        }
        if (errors[0] != null) {
           // LOG.e(errors[0]);
            throw errors[0];
        }
        return (T) result[0];
    }

    public void postVoid(JSEngine.Event<Void> event) {
        postVoid(event, true);
    }

    public void postVoid(JSEngine.Event<Void> event, boolean block) {
        if ((thread != null && thread.isInterrupted())) {
         //   LOG.e("QuickJS", "QuickJS is released");
            return;
        }
        if (Thread.currentThread() == thread) {
            event.run(getJsContext(), getGlobalObj());
            return;
        }
        if (handler == null) {
            event.run(getJsContext(), getGlobalObj());
            return;
        }
        Object[] result = new Object[2];
        RuntimeException[] errors = new RuntimeException[1];
        handler.post(() -> {
            try {
                event.run(getJsContext(), getGlobalObj());
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
               //     LOG.e(e);
                }
            }
            if (errors[0] != null) {
               // LOG.e(errors[0]);
                throw errors[0];
            }
        }
    }

    private void initProperty() {
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            JSMethod an = method.getAnnotation(JSMethod.class);
            if (an == null) continue;
            String functionName = method.getName();

            getGlobalObj().setProperty(functionName, args -> {
                try {
                    return method.invoke(this, args);
                } catch (Exception e) {
                  //  LOG.e(e);
                    throw new RuntimeException(e);
                }
            });

            /*if (StringUtils.isNotEmpty(an.alias())) {
                getJsContext().evaluate("var " + an.alias() + " = " + functionName + ";\n");
            }*/
        }
    }

    public void init(Class<?> cls) {
        initProperty();
        initConsole();
        if(cls != null){
            getGlobalObj().setProperty("jsapi", getJsContext().createNewJSObject());
            Class<?>[] classes = cls.getDeclaredClasses();
            JSObject apiObj = getGlobalObj().getJSObject("jsapi");
          //  LOG.e("cls","" + classes.length);
            for (Class<?> classe : classes) {
                Object javaObj = null;
                try {
                    javaObj = classe.getDeclaredConstructor(cls).newInstance(cls.getDeclaredConstructor(QuickJSContext.class).newInstance(getJsContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (javaObj == null) {
                    throw new NullPointerException("The JavaObj cannot be null. An error occurred in newInstance!");
                }
                JSObject claObj = getJsContext().createNewJSObject();
                Method[] methods = classe.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(JSMethod.class)) {
                        Object finalJavaObj = javaObj;
                        claObj.setProperty(method.getName(), args -> {
                            try {
                                return method.invoke(finalJavaObj, args);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        });
                    }
                }
                apiObj.setProperty(classe.getSimpleName(), claObj);
              //  LOG.e("cls", classe.getSimpleName());
            }
        }
    }

    @Keep
    @JSMethod
    public String getProxy(boolean local) {
        return ControlManager.get().getAddress(local) + "proxy?do=js";
    }

    @Keep
    @JSMethod
    public String joinUrl(String parent, String child) {
        return HtmlParser.joinUrl(parent, child);
    }

    @Keep
    @JSMethod
    public String pdfh(String html, String rule) {
        try {
            return HtmlParser.parseDomForUrl(html, rule, "");
        } catch (Exception th) {
          //  LOG.e(th);
            return "";
        }
    }

    @Keep
    @JSMethod
    public Object pdfa(String html, String rule) {
        try {
            return getJsContext().parse(new Gson().toJson(HtmlParser.parseDomForArray(html, rule)));
        } catch (Exception th) {
         //   LOG.e(th);
            return getJsContext().createNewJSArray();
        }
    }

    @Keep
    @JSMethod
    public Object pdfl(String html, String p1, String list_text, String list_url, String urlKey) {
        try {
            return getJsContext().parse(new Gson().toJson(HtmlParser.parseDomForList(html, p1, list_text, list_url, urlKey)));
        } catch (Exception th) {
          //  LOG.e(th);
            return getJsContext().createNewJSArray();
        }
    }

    @Keep
    @JSMethod
    public String pd(String html, String rule, String urlKey) {
        try {
            return HtmlParser.parseDomForUrl(html, rule, urlKey);
        } catch (Exception th) {
          //  LOG.e(th);
            return "";
        }
    }

    @Keep
    @JSMethod
    public Object req(String url, Object o2) {
        try {
            //JSONObject opt = ((JSObject) o2).toJSONObject();
            JSONObject opt = new JSONObject(jsContext.stringify((JSObject) o2));
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
            Request.Builder requestBuilder = new Request.Builder().url(url).headers(headers);
            requestBuilder.tag("js_okhttp_tag");
            Request request;
            String contentType = null;
            if (!StringUtils.isEmpty(headers.get("content-type"))) {
                contentType = headers.get("Content-Type");
            }
            String method = opt.optString("method").toLowerCase();
            String charset = "utf-8";
            if (contentType != null && contentType.split("charset=").length > 1) {
                charset = contentType.split("charset=")[1];
            }

            if (method.equals("post")) {
                RequestBody body = null;
                String data = opt.optString("data", "").trim();
                if (!StringUtils.isEmpty(data)) {
                    body = RequestBody.create(MediaType.parse("application/json"), data);
                }
                if (body == null) {
                    String dataBody = opt.optString("body", "").trim();
                    if (!StringUtils.isEmpty(dataBody) && contentType != null) {
                        body = RequestBody.create(MediaType.parse(contentType), opt.optString("body", ""));
                    }
                }
                if (body == null) {
                    body = RequestBody.create(null, "");
                }
                request = requestBuilder.post(body).build();
            } else if (method.equals("header")) {
                request = requestBuilder.head().build();
            } else {
                request = requestBuilder.get().build();
            }
            okHttpClient = opt.optInt("redirect", 1) == 1 ? OkGoHelper.getDefaultClient() : OkGoHelper.getNoRedirectClient();
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            if (opt.has("timeout")) {
                long timeout = opt.optInt("timeout");
                builder.readTimeout(timeout, TimeUnit.MILLISECONDS);
                builder.writeTimeout(timeout, TimeUnit.MILLISECONDS);
                builder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
            }
            Response response = builder.build().newCall(request).execute();
            JSObject jsObject = jsContext.createNewJSObject();
            JSObject resHeader = jsContext.createNewJSObject();

            for (Map.Entry<String, List<String>> entry : response.headers().toMultimap().entrySet()) {
                if (entry.getValue().size() == 1) {
                    resHeader.setProperty(entry.getKey(), entry.getValue().get(0));
                }
                if (entry.getValue().size() >= 2) {
                    JSArray array = jsContext.createNewJSArray();
                    List<String> items = entry.getValue();
                    if (items == null || items.isEmpty()) return array;
                    for (int i = 0; i < items.size(); i++) {
                        array.set(items.get(i), i);
                    }
                    resHeader.setProperty(entry.getKey(), array);
                };
            }

            jsObject.setProperty("headers", resHeader);
            jsObject.setProperty("code", response.code());

            int returnBuffer = opt.optInt("buffer", 0);
            if (returnBuffer == 1) {
                JSArray array = jsContext.createNewJSArray();
                byte[] bytes = response.body().bytes();
                for (int i = 0; i < bytes.length; i++) {
                    array.set(bytes[i], i);
                }
                jsObject.setProperty("content", array);
            } else if (returnBuffer == 2) {
                jsObject.setProperty("content", Base64.encodeToString(response.body().bytes(), Base64.DEFAULT));
            } else {
                String res;
                byte[] responseBytes = UTF8BOMFighter.removeUTF8BOM(response.body().bytes());
                res = new String(responseBytes, charset);
                jsObject.setProperty("content", res);
            }
            return jsObject;
        } catch (Throwable throwable) {
         //   LOG.e(throwable);
            return "";
        }
    }

    void initConsole() {
        getGlobalObj().setProperty("local", local.class);
        getGlobalObj().setProperty("console", jsContext.createNewJSObject());
        JSObject console = jsContext.getGlobalObject().getJSObject("console");
        console.setProperty("log", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                StringBuilder b = new StringBuilder();
                for (Object o : args) {
                    b.append(o == null ? "null" : o.toString());
                }
               // LOG.i("QuickJS", b.toString());
                return null;
            }
        });
    }

    public void cancelByTag(Object tag) {
        try {
            if (okHttpClient != null) {
                for (Call call : okHttpClient.dispatcher().queuedCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                for (Call call : okHttpClient.dispatcher().runningCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
            OkGo.getInstance().cancelTag(tag);
        } catch (Exception e) {
         //   LOG.e(e);
        }
    }
}