package com.github.tvbox.osc.server;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import com.github.tvbox.osc.util.LOG;

/**
 * @author pj567
 * @date :2021/1/5
 * @description: 响应按键和输入
 */

public class InputRequestProcess implements RequestProcess {
    private RemoteServer remoteServer;

    public InputRequestProcess(RemoteServer remoteServer) {
        this.remoteServer = remoteServer;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            switch (fileName) {
                case "/projection":
                case "/text":
                case "/key":
                case "/keyDown":
                case "/keyUp":
                case "/custom":
                    return true;
            }
        }
        return false;
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        DataReceiver mDataReceiver = remoteServer.getDataReceiver();
        switch (fileName) {
            case "/projection":
                LOG.e("------");
                if (params.get("text") != null && mDataReceiver != null) {
                    LOG.e("+++++");
                    mDataReceiver.onProjectionReceived(params.get("text"));
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            case "/text":
                if (params.get("text") != null && mDataReceiver != null) {
                    mDataReceiver.onTextReceived(params.get("text"));
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            case "/key":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), KEY_ACTION_PRESSED);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            case "/keyUp":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), KEY_ACTION_UP);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            case "/keyDown":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), KEY_ACTION_DOWN);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            case "/custom":
                if (params.get("action") != null && mDataReceiver != null) {
                    String action = params.get("action");
                    if ("source".equals(action)) {
                        if (params.get("name") != null && params.get("api") != null && params.get("play") != null && params.get("type") != null)
                            mDataReceiver.onSourceReceived(params.get("name"), params.get("api"), params.get("play"), params.get("type"));
                    }else if ("parse".equals(action)) {
                        if (params.get("name") != null && params.get("url") != null)
                            mDataReceiver.onParseReceived(params.get("name"), params.get("url"));
                    }else if ("live".equals(action)) {
                        if (params.get("name") != null && params.get("url") != null)
                            mDataReceiver.onLiveReceived(params.get("name"), params.get("url"));
                    }
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            default:
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404, file not found.");
        }
    }
}
