package com.github.tvbox.osc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.cache.LocalLive;
import com.github.tvbox.osc.cache.LocalParse;
import com.github.tvbox.osc.cache.LocalSource;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.util.LOG;

/**
 * @author pj567
 * @date :2021/1/5
 * @description:
 */
public class CustomWebReceiver extends BroadcastReceiver {
    public static String action = "android.content.movie.custom.web.Action";

    public static String REFRESH_SOURCE = "source";
    public static String REFRESH_LIVE = "live";
    public static String REFRESH_PARSE = "parse";

    public static List<Callback> callback = new ArrayList<>();

    public interface Callback {
        void onChange(String action, Object obj);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (action.equals(intent.getAction()) && intent.getExtras() != null) {
            Object refreshObj = null;
            String action = intent.getExtras().getString("action");
            if (action.equals(REFRESH_SOURCE)) {
                String name = intent.getExtras().getString("name");
                String api = intent.getExtras().getString("api");
                String play = intent.getExtras().getString("play");
                int type = intent.getExtras().getInt("type");
                LOG.i(name);
                LOG.i(api);
                LOG.i(play);
                LOG.i(type + "");
                for (SourceBean sb : ApiConfig.get().getSourceBeanList()) {
                    if (sb.getKey().equals(name)) {
                        Toast.makeText(context, "数据源【" + name + "】已存在!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                LocalSource ls = new LocalSource();
                ls.name = name;
                ls.api = api;
                ls.type = type;
                ls.playerUrl = play;
                RoomDataManger.addLocalSource(ls);
                SourceBean sb = new SourceBean();
                sb.initFromLocal(ls);
                ApiConfig.get().getSourceBeanList().add(sb);
                refreshObj = sb;
            } else if (action.equals(REFRESH_PARSE)) {
                String name = intent.getExtras().getString("name");
                String url = intent.getExtras().getString("url");
                LOG.i(name);
                LOG.i(url);
                for (ParseBean sb : ApiConfig.get().getParseBeanList()) {
                    if (sb.getParseName().equals(name)) {
                        Toast.makeText(context, "解析【" + name + "】已存在!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                LocalParse ls = new LocalParse();
                ls.name = name;
                ls.url = url;
                RoomDataManger.addLocalParse(ls);
                ParseBean sb = new ParseBean();
                sb.initFromLocal(ls);
                refreshObj = sb;
                ApiConfig.get().getParseBeanList().add(sb);
            } else if (action.equals(REFRESH_LIVE)) {
                String name = intent.getExtras().getString("name");
                String url = intent.getExtras().getString("url");
                LOG.i(name);
                LOG.i(url);
                for (LiveChannel sb : ApiConfig.get().getChannelList()) {
                    if (sb.getChannelName().equals(name)) {
                        Toast.makeText(context, "直播【" + name + "】已存在!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                LocalLive ls = new LocalLive();
                ls.name = name;
                ls.url = url;
                RoomDataManger.addLocalLive(ls);
                LiveChannel sb = new LiveChannel();
                sb.initFromLocal(ls);
                refreshObj = sb;
                ApiConfig.get().getChannelList().add(sb);
            }
            if (callback != null) {
                for (Callback call : callback) {
                    call.onChange(action, refreshObj);
                }
            }
        }
    }
}