package com.github.tvbox.osc.js;

import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import com.github.tvbox.osc.util.CharsetUtils;
import com.github.tvbox.osc.util.Json;
import com.github.tvbox.osc.util.OkGoHelper;
import com.quickjs.JSArray;
import com.quickjs.JSObject;
import org.json.JSONObject;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DrpyMethods {

    private ES6Module module;

    public DrpyMethods(ES6Module module) {
        this.module = module;
    }

    @JavascriptInterface
    public JSObject ajax(String url, JSObject obj) {
        try {
            JSONObject jsonObject = obj.toJSONObject();
            String method = jsonObject.optString("method", "get");
            JSONObject headersJSONObject = jsonObject.optJSONObject("headers");
            Map<String, String> headers = Json.toMap(headersJSONObject);
            String contentType = null;
            for(String headerKey : headers.keySet()) {
                if (headerKey.equalsIgnoreCase("method")) method = headers.get(headerKey);
                if (headerKey.equalsIgnoreCase("content-type")) contentType = headers.get(headerKey);
            }
            Request.Builder requestBuilder = new Request.Builder().url(url).headers(Headers.of(headers));
            requestBuilder.tag("js_okhttp_tag");
            Request request = null;
            if (method.equalsIgnoreCase("post")) {
                String data = jsonObject.optString("data", "");
                String body = jsonObject.optString("body", "");
                RequestBody requestBody = null;
                if (!TextUtils.isEmpty(data)) {
                    requestBody = RequestBody.create(MediaType.parse("application/json"), data);
                } else if (!TextUtils.isEmpty(body) && !TextUtils.isEmpty(contentType)) {
                    requestBody = RequestBody.create(MediaType.parse(contentType), body);
                } else {
                    requestBody = RequestBody.create(null, "");
                }
                request = requestBuilder.post(requestBody).build();
            } else if (method.equalsIgnoreCase("header")) {
                request = requestBuilder.head().build();
            } else {
                request = requestBuilder.get().build();
            }
            int redirect = jsonObject.optInt("redirect", 1);

            OkHttpClient client = null;
            if (redirect == 1) {
                client  = OkGoHelper.getDefaultClient();
            } else {
                client  = OkGoHelper.getNoRedirectClient();
            }
            Response response = client.newCall(request).execute();
            JSObject result = new JSObject(module);
            JSObject resultHeaders = new JSObject(module);
            Set<String> names = response.headers().names();
            for (String headerKey : names) {
                resultHeaders.set(headerKey, response.header(headerKey));
            }
            result.set("headers", resultHeaders);
            result.set("code", response.code());
            int isBuffer = jsonObject.optInt("buffer", 0);
            if (isBuffer == 1) {
                JSArray jsArray = new JSArray(module);
                byte[] bytes = response.body().bytes();
                for (int i = 0; i < bytes.length; i++) {
                    jsArray.set(i + "", (int) bytes[i]);
                }
                result.set("content", jsArray);
            } else if (isBuffer == 2) {
                result.set("content", Base64.encodeToString(response.body().bytes(), 0));
            } else {
                String content = response.body().string();
                byte[] bytes = content.getBytes();
                Charset charset = CharsetUtils.detect(bytes);
                if (!charset.name().toLowerCase().startsWith("utf")) {
                    content = new String(bytes, charset.name());
                }
                result.set("content", content);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @JavascriptInterface
    public String joinUrl(String parent, String child) {
        return HtmlParser.joinUrl(parent, child);
    }

    @JavascriptInterface
    public String parseDomForHtml(String html, String rule) {
        try {
            return HtmlParser.parseDomForUrl(html, rule, "");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return "";
    }

    @JavascriptInterface
    public String parseDom(String html, String rule, String urlKey) {
        try {
            return HtmlParser.parseDomForUrl(html, rule, urlKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @JavascriptInterface
    public JSObject parseDomForArray(String html, String rule) {
        JSArray array = new JSArray(module);
        try {
            List<String> list = HtmlParser.parseDomForList(html, rule);
            for (String one : list) {
                array.push(one);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return array;
    }


}
