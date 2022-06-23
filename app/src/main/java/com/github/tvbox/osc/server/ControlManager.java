package com.github.tvbox.osc.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.github.tvbox.osc.receiver.CustomWebReceiver;
import com.github.tvbox.osc.receiver.ProjectionReceiver;
import com.github.tvbox.osc.receiver.SearchReceiver;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.util.AppManager;

import java.io.IOException;

import static com.github.tvbox.osc.server.RequestProcess.KEY_ACTION_DOWN;
import static com.github.tvbox.osc.server.RequestProcess.KEY_ACTION_PRESSED;

/**
 * @author pj567
 * @date :2021/1/4
 * @description:
 */
public class ControlManager {
    private static ControlManager instance;
    private RemoteServer mServer = null;
    public static Context mContext;

    private ControlManager() {

    }

    public static ControlManager get() {
        if (instance == null) {
            synchronized (ControlManager.class) {
                if (instance == null) {
                    instance = new ControlManager();
                }
            }
        }
        return instance;
    }

    public static void init(Context context) {
        mContext = context;
    }

    public void startServer() {
        if (mServer != null) {
            return;
        }
        do {
            mServer = new RemoteServer(RemoteServer.serverPort, mContext);
            mServer.setDataReceiver(new DataReceiver() {
                @Override
                public void onKeyEventReceived(String keyCode, final int keyAction) {
                    if (keyCode != null) {
                        final int kc = KeyEvent.keyCodeFromString(keyCode);
                        if (kc != KeyEvent.KEYCODE_UNKNOWN) {
                            switch (keyAction) {
                                case KEY_ACTION_PRESSED:
                                case KEY_ACTION_DOWN:
                                    sendKeyCode(kc);
                                    break;
                            }
                        }
                    }
                }

                @Override
                public void onTextReceived(String text) {
                    if (!TextUtils.isEmpty(text)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("title", text);
                        intent.setAction(SearchReceiver.action);
                        intent.setPackage(mContext.getPackageName());
                        intent.setComponent(new ComponentName(mContext, SearchReceiver.class));
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onProjectionReceived(String text) {
                    if (!TextUtils.isEmpty(text)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("html", text);
                        intent.setAction(ProjectionReceiver.action);
                        intent.setPackage(mContext.getPackageName());
                        intent.setComponent(new ComponentName(mContext, ProjectionReceiver.class));
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onSourceReceived(String name, String api, String play, String type) {
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(api)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("action", CustomWebReceiver.REFRESH_SOURCE);
                        bundle.putString("name", name);
                        bundle.putString("api", api);
                        bundle.putString("play", play);
                        bundle.putInt("type", Integer.parseInt(type));
                        intent.setAction(CustomWebReceiver.action);
                        intent.setPackage(mContext.getPackageName());
                        intent.setComponent(new ComponentName(mContext, CustomWebReceiver.class));
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onLiveReceived(String name, String url) {
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(url)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("action", CustomWebReceiver.REFRESH_LIVE);
                        bundle.putString("name", name);
                        bundle.putString("url", url);
                        intent.setAction(CustomWebReceiver.action);
                        intent.setPackage(mContext.getPackageName());
                        intent.setComponent(new ComponentName(mContext, CustomWebReceiver.class));
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);
                    }
                }

                @Override
                public void onParseReceived(String name, String url) {
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(url)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("action", CustomWebReceiver.REFRESH_PARSE);
                        bundle.putString("name", name);
                        bundle.putString("url", url);
                        intent.setAction(CustomWebReceiver.action);
                        intent.setPackage(mContext.getPackageName());
                        intent.setComponent(new ComponentName(mContext, CustomWebReceiver.class));
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);
                    }
                }
            });
            try {
                mServer.start();
                break;
            } catch (IOException ex) {
                RemoteServer.serverPort++;
                mServer.stop();
            }
        } while (RemoteServer.serverPort < 9999);
    }

    private void sendKeyCode(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            //拦截HOME键
            AppManager.getInstance().backActivity(HomeActivity.class);
        } else {
            ShellUtils.execCommand("input keyevent " + keyCode, false);
        }
    }

    public void stopServer() {
        if (mServer != null && mServer.isStarting()) {
            mServer.stop();
        }
    }
}