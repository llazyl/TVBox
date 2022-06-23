package com.github.tvbox.osc.api;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Base64;

import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.cache.LocalLive;
import com.github.tvbox.osc.cache.LocalParse;
import com.github.tvbox.osc.cache.LocalSource;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.SourceState;
import com.github.tvbox.osc.util.AdBlocker;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class ApiConfig {
    private static ApiConfig instance;
    private List<SourceBean> sourceBeanList;
    private SourceBean mHomeSource;
    private ParseBean mDefaultParse;
    private List<LiveChannel> channelList;
    private List<ParseBean> parseBeanList;
    private List<String> vipParseFlags;
    private List<IJKCode> ijkCodes;

    private ApiConfig() {
        sourceBeanList = new ArrayList<>();
        channelList = new ArrayList<>();
        parseBeanList = new ArrayList<>();
    }

    public static ApiConfig get() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    private String decompressGZIP(byte[] src) throws IOException {
        // 创建一个新的输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        // 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n = 0;
        // 将未压缩数据读入字节数组
        while ((n = gzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toString("utf-8");
    }

    public void loadConfig(LoadConfigCallback callback, Activity activity) {
        boolean isSourceModeLocal = Hawk.get(HawkConfig.SOURCE_MODE_LOCAL, true);
        if (isSourceModeLocal) {
            loadConfigLocal(callback, activity);
        } else {
            loadConfigServer(callback, activity);
        }
    }

    private void loadConfigLocal(LoadConfigCallback callback, Activity activity) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = activity.getAssets().open("cfg.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
            parseJson(sb.toString());
            callback.success();
        } catch (IOException e) {
            e.printStackTrace();
            callback.error("加载配置失败");
        }

    }

    private void loadConfigLocal1(LoadConfigCallback callback, Activity activity) {
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(activity.getAssets().open("cfg.json")));
            String line = bf.readLine();
            String timestamp = line.substring(line.length() - 13);
            String infoString = line.substring(0, line.length() - 13);
            byte[] infoBytes = Base64.decode(infoString, Base64.DEFAULT);
            for (int i = 0; i < infoBytes.length; i++) {
                if (i % 2 == 0) {
                    int idx = i % timestamp.length();
                    int c = timestamp.charAt(idx);
                    infoBytes[i] ^= c + 10;
                }
            }
            String finalRemoteJson = decompressGZIP(infoBytes);
            parseJson(finalRemoteJson);
            callback.success();
        } catch (IOException e) {
            e.printStackTrace();
            callback.error("加载配置失败");
        }
    }

    public static final int REG_CODE_INVALID = -2;

    private void loadConfigServer(LoadConfigCallback callback, Activity activity) {
    }

    private void parseJson(String jsonStr) {
        JsonObject infoJson = new Gson().fromJson(jsonStr, JsonObject.class);
        // 远端站点源
        sourceBeanList = new Gson().fromJson(infoJson.get("sources").toString(), new TypeToken<List<SourceBean>>() {
        }.getType());
        // 获取本地站点源
        List<LocalSource> localSources = RoomDataManger.getAllLocalSource();
        for (LocalSource sbl : localSources) {
            SourceBean local = new SourceBean();
            local.initFromLocal(sbl);
            sourceBeanList.add(local);
        }
        if (sourceBeanList != null && sourceBeanList.size() > 0) {
            // 获取启用状态
            HashMap<String, SourceState> sourceStates = RoomDataManger.getAllSourceState();
            for (SourceBean sb : sourceBeanList) {
                if (sourceStates.containsKey(sb.getKey()))
                    sb.setState(sourceStates.get(sb.getKey()));
                if (sb.isHome())
                    setSourceBean(sb);
            }
            // 如果没有home source 使用第一个
            if (mHomeSource == null)
                setSourceBean(sourceBeanList.get(0));
        }
        // 需要使用vip解析的flag
        vipParseFlags = new Gson().fromJson(infoJson.get("parseFlag").toString(), new TypeToken<List<String>>() {
        }.getType());
        // 解析地址
        parseBeanList = new Gson().fromJson(infoJson.get("parseUrl").toString(), new TypeToken<List<ParseBean>>() {
        }.getType());
        // 获取本地解析地址
        List<LocalParse> localParses = RoomDataManger.getAllLocalParse();
        for (LocalParse lp : localParses) {
            ParseBean local = new ParseBean();
            local.initFromLocal(lp);
            parseBeanList.add(local);
        }
        // 获取启用状态
        if (parseBeanList != null && parseBeanList.size() > 0) {
            String defaultParse = Hawk.get(HawkConfig.DEFAULT_PARSE, "");
            if (!TextUtils.isEmpty(defaultParse))
                for (ParseBean pb : parseBeanList) {
                    if (pb.getParseName().equals(defaultParse))
                        setDefaultParse(pb);
                }
            if (mDefaultParse == null)
                setDefaultParse(parseBeanList.get(0));
        }
        // 直播源
        channelList = new Gson().fromJson(infoJson.get("live").toString(), new TypeToken<List<LiveChannel>>() {
        }.getType());
        // 获取本地解析地址
        List<LocalLive> localLives = RoomDataManger.getAllLocalLive();
        for (LocalLive ll : localLives) {
            LiveChannel lc = new LiveChannel();
            lc.initFromLocal(ll);
            channelList.add(lc);
        }
        // 广告地址
        for (JsonElement host : infoJson.getAsJsonArray("ads")) {
            AdBlocker.addAdHost(host.getAsString());
        }
        // 屏蔽分类
        List<String> rmBase = new ArrayList<>();
        List<String> rmAdolescent = new ArrayList<>();
        for (JsonElement rm : infoJson.get("filter").getAsJsonObject().get("base").getAsJsonArray()) {
            rmBase.add(rm.getAsString());
        }
        DefaultConfig.initRemove(rmBase);
        // IJK解码配置
        boolean foundOldSelect = false;
        String ijkCodec = Hawk.get(HawkConfig.IJK_CODEC, "");
        ijkCodes = new ArrayList<>();
        LinkedHashMap<String, String> baseOpt = new LinkedHashMap<>();
        for (JsonElement opt : infoJson.get("ijk").getAsJsonObject().get("option").getAsJsonArray()) {
            String s = opt.getAsString();
            baseOpt.put(s.substring(0, s.lastIndexOf("|")), s.substring(s.lastIndexOf("|") + 1));
        }
        for (JsonElement cfg : infoJson.get("ijk").getAsJsonObject().get("config").getAsJsonArray()) {
            String name = cfg.getAsJsonObject().get("name").getAsString();
            LinkedHashMap<String, String> mergeOpt = new LinkedHashMap<>();
            mergeOpt.putAll(baseOpt);
            for (JsonElement opt : cfg.getAsJsonObject().get("option").getAsJsonArray()) {
                String s = opt.getAsString();
                mergeOpt.put(s.substring(0, s.lastIndexOf("|")), s.substring(s.lastIndexOf("|") + 1));
            }
            IJKCode codec = new IJKCode();
            codec.setName(name);
            codec.setOption(mergeOpt);
            if (name.equals(ijkCodec) || TextUtils.isEmpty(ijkCodec)) {
                codec.selected(true);
                ijkCodec = name;
                foundOldSelect = true;
            } else {
                codec.selected(false);
            }
            ijkCodes.add(codec);
        }
        if (!foundOldSelect && ijkCodes.size() > 0) {
            ijkCodes.get(0).selected(true);
        }
    }

    public interface LoadConfigCallback {
        void success();

        void retry();

        void error(String msg);
    }

    public interface FastParseCallback {
        void success(boolean parse, String url, Map<String, String> header);

        void fail(int code, String msg);
    }

    public SourceBean getSource(String key) {
        for (SourceBean bean : sourceBeanList) {
            if (bean.getKey().equals(key))
                return bean;
        }
        return null;
    }

    public void setSourceBean(SourceBean sourceBean) {
        if (this.mHomeSource != null)
            this.mHomeSource.setHome(false);
        this.mHomeSource = sourceBean;
        sourceBean.setHome(true);
    }

    public void setDefaultParse(ParseBean parseBean) {
        if (this.mDefaultParse != null)
            this.mDefaultParse.setDefault(false);
        this.mDefaultParse = parseBean;
        Hawk.put(HawkConfig.DEFAULT_PARSE, parseBean.getParseName());
        parseBean.setDefault(true);
    }

    public ParseBean getDefaultParse() {
        return mDefaultParse;
    }

    public List<SourceBean> getSourceBeanList() {
        return sourceBeanList;
    }

    public List<ParseBean> getParseBeanList() {
        return parseBeanList;
    }

    public List<String> getVipParseFlags() {
        return vipParseFlags;
    }

    public SourceBean getHomeSourceBean() {
        return mHomeSource;
    }

    public List<LiveChannel> getChannelList() {
        return channelList;
    }

    public List<IJKCode> getIjkCodes() {
        return ijkCodes;
    }

    public IJKCode getCurrentIJKCode() {
        String codeName = Hawk.get(HawkConfig.IJK_CODEC, "");
        for (IJKCode code : ijkCodes) {
            if (code.getName().equals(codeName))
                return code;
        }
        return ijkCodes.get(0);
    }
}