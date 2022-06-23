package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.adapter.ParseDialogAdapter;
import com.github.tvbox.osc.util.AdBlocker;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.XWalkUtils;
import com.lzy.okgo.OkGo;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pj567
 * @date :2021/1/6
 * @description:
 */
public class ParseDialog {
    private FrameLayout rootView;
    private Dialog mDialog;
    private Context mContext;
    private TextView mParseTip;

    private static final String IPHONE_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";
    private static final String PC_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";
    private static final String ANDROID_UA = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Mobile Safari/537.36";

    private TvRecyclerView mGridView;
    private ParseDialogAdapter parseAdapter;
    private List<ParseBean> parseBeans = new ArrayList<>();
    private Map<String, Boolean> loadedUrls = new HashMap<>();
    private Handler mHandler = new Handler();
    private boolean loadFound = false;

    private Runnable mParseTimeOut = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(mContext, "解析超时，请尝试切换解析重试!", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable mGridFocus = new Runnable() {
        @Override
        public void run() {
            mGridView.requestFocus();
        }
    };

    public ParseDialog build(Context context, BackPress backPress) {
        mContext = context;
        rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.dialog_parse, null);
        mParseTip = findViewById(R.id.mParse_tip);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK
                        && event.getRepeatCount() == 0) {
                    OkGo.getInstance().cancelTag("fast_parse");
                    backPress.onBack();
                    return true;
                }
                return false;
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mX5WebView != null) {
                    mX5WebView.stopLoading();
                    mX5WebView.clearCache(true);
                    mX5WebView.removeAllViews();
                    mX5WebView.onDestroy();
                }
                if (mSysWebView != null) {
                    mSysWebView.stopLoading();
                    mSysWebView.clearCache(true);
                    mSysWebView.removeAllViews();
                    mSysWebView.destroy();
                }
                mHandler.removeCallbacks(mParseTimeOut);
            }
        });
        try {
            mDialog.getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this;
    }

    public interface BackPress {
        void onBack();
    }

    public interface ParseCallback {
        void success(String playUrl, Map<String, String> headers);

        void fail();
    }

    public void parse(String sourceKey, String flag, String url, ParseCallback callback) {
        Uri uir = Uri.parse(url);
        String urlPath = uir.getPath();
        String format = DefaultConfig.getFileSuffix(urlPath);
        SourceBean sb = ApiConfig.get().getSource(sourceKey);
        if (DefaultConfig.isVideoFormat(format)) {
            callback.success(url, null);
        } else { // 解析咯
            initParse(sourceKey, flag, url, callback);
        }
    }

    private XWalkView mX5WebView;
    private XWalkWebClient mX5WebClient;
    private WebView mSysWebView;
    private SysWebClient mSysWebClient;

    private void initParse(String sourceKey, String flag, String url, ParseCallback callback) {
        mParseTip.setText("资源解析中，请稍后");
        if (mSysWebView == null && mX5WebView == null) {
            boolean useSystemWebView = Hawk.get(HawkConfig.PARSE_WEBVIEW, true);
            if (!useSystemWebView) {
                XWalkUtils.tryUseXWalk(mContext, new XWalkUtils.XWalkState() {
                    @Override
                    public void success() {
                        initWebView(false);
                        initParseBean(sourceKey, flag, url, callback);
                    }

                    @Override
                    public void fail() {
                        Toast.makeText(mContext, "XWalkView不兼容，已替换为系统自带WebView", Toast.LENGTH_LONG).show();
                        initWebView(true);
                        initParseBean(sourceKey, flag, url, callback);
                    }

                    @Override
                    public void ignore() {
                        Toast.makeText(mContext, "XWalkView运行组件未下载，已替换为系统自带WebView", Toast.LENGTH_LONG).show();
                        initWebView(true);
                        initParseBean(sourceKey, flag, url, callback);
                    }
                });
            } else {
                initWebView(true);
                initParseBean(sourceKey, flag, url, callback);
            }
        } else {
            initParseBean(sourceKey, flag, url, callback);
        }
    }

    private void initWebView(boolean useSystemWebView) {
        if (useSystemWebView) {
            mSysWebView = new WebView(mContext);
            configWebViewSys(mSysWebView);
        } else {
            mX5WebView = new XWalkView(mContext);
            configWebViewX5(mX5WebView);
        }
    }

    private void loadUrl(String url, ParseCallback callback) {
        if (mSysWebClient != null) {
            mSysWebClient.setCallback(callback);
        }
        if (mX5WebClient != null) {
            mX5WebClient.setCallback(callback);
        }
        if (mX5WebView != null) {
            mX5WebView.stopLoading();
            mX5WebView.clearCache(true);
            mX5WebView.loadUrl(url);
        }
        if (mSysWebView != null) {
            mSysWebView.stopLoading();
            mSysWebView.clearCache(true);
            mSysWebView.loadUrl(url);
        }
        mHandler.removeCallbacks(mParseTimeOut);
        mHandler.postDelayed(mGridFocus, 200);
        mHandler.postDelayed(mParseTimeOut, 30000);
    }

    private void initParseBean(String sourceKey, String flag, final String url, ParseCallback callback) {
        if (mGridView == null) {
            mGridView = findViewById(R.id.mGridView);
            parseAdapter = new ParseDialogAdapter();
            mGridView.setAdapter(parseAdapter);
            mGridView.setLayoutManager(new V7GridLayoutManager(mContext, 6));
            parseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    FastClickCheckUtil.check(view);
                    ParseBean parseBean = parseAdapter.getData().get(position);
                    // 当前默认解析需要刷新
                    int currentDefault = parseBeans.indexOf(ApiConfig.get().getDefaultParse());
                    parseAdapter.notifyItemChanged(currentDefault);
                    ApiConfig.get().setDefaultParse(parseBean);
                    parseAdapter.notifyItemChanged(position);
                    loadFound = false;
                    loadUrl(parseBean.getParseUrl() + url, callback);
                }
            });
            mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
                @Override
                public boolean onInBorderKeyEvent(int direction, View focused) {
                    return true;
                }
            });
        }
        int focusParseIdx = 0;
        SourceBean sb = ApiConfig.get().getSource(sourceKey);
        String parseUrl = "";
        if (ApiConfig.get().getVipParseFlags().contains(flag) || TextUtils.isEmpty(flag)) {
            parseBeans.addAll(ApiConfig.get().getParseBeanList());
            ParseBean parseBean = ApiConfig.get().getDefaultParse();
            parseUrl = parseBean.getParseUrl();
            focusParseIdx = parseBeans.indexOf(parseBean);
        } else {
            parseUrl = sb == null ? "" : sb.getPlayerUrl();
            focusParseIdx = 0;
        }
        loadFound = false;
        parseAdapter.setNewData(parseBeans);
        mGridView.setSelection(focusParseIdx);
        final String fullParseUrl = parseUrl + url;
        if (parseBeans.size() > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadUrl(fullParseUrl, callback);
                }
            }, 3000);
        } else {
            loadUrl(fullParseUrl, callback);
        }
    }

    private void configWebViewSys(WebView webView) {
        if (webView == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = Hawk.get(HawkConfig.DEBUG_OPEN, false)
                ? new ViewGroup.LayoutParams(800, 400) :
                new ViewGroup.LayoutParams(1, 1);
        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.clearFocus();
        webView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        rootView.addView(webView, layoutParams);
        /* 添加webView配置 */
        final WebSettings settings = webView.getSettings();
        settings.setNeedInitialFocus(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }
        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            settings.setBlockNetworkImage(false);
        } else {
            settings.setBlockNetworkImage(true);
        }
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        /* 添加webView配置 */
        //设置编码
        settings.setDefaultTextEncodingName("utf-8");
        settings.setUserAgentString(webView.getSettings().getUserAgentString());
        settings.setUserAgentString(ANDROID_UA);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return false;
            }
        });
        mSysWebClient = new SysWebClient();
        webView.setWebViewClient(mSysWebClient);
        webView.setBackgroundColor(Color.BLACK);
    }

    private class SysWebClient extends WebViewClient {
        private ParseCallback callback;

        public void setCallback(ParseCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            // suppress favicon requests as we don't display them anywhere
            if (url.endsWith("/favicon.ico")) {
                return new WebResourceResponse("image/png", null, null);
            }
            LOG.i("shouldInterceptRequest url:" + url);
            boolean ad;
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url);
                loadedUrls.put(url, ad);
            } else {
                ad = loadedUrls.get(url);
            }

            if (!ad && !loadFound) {
                if (DefaultConfig.isVideoFormat(url)) {
                    loadFound = true;
                    mHandler.removeCallbacks(mParseTimeOut);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.stopLoading();
                            callback.success(url, null);
                        }
                    });
                }
            }

            return ad || loadFound ?
                    AdBlocker.createEmptyResource() :
                    super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onLoadResource(WebView webView, String url) {
            super.onLoadResource(webView, url);
        }
    }

    private void configWebViewX5(XWalkView webView) {
        if (webView == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = Hawk.get(HawkConfig.DEBUG_OPEN, false)
                ? new ViewGroup.LayoutParams(800, 400) :
                new ViewGroup.LayoutParams(1, 1);
        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.clearFocus();
        webView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        rootView.addView(webView, layoutParams);
        /* 添加webView配置 */
        final XWalkSettings settings = webView.getSettings();
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            settings.setBlockNetworkImage(false);
        } else {
            settings.setBlockNetworkImage(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(ANDROID_UA);

        webView.setBackgroundColor(Color.BLACK);
        webView.setUIClient(new XWalkUIClient(webView) {
            @Override
            public boolean onConsoleMessage(XWalkView view, String message, int lineNumber, String sourceId, ConsoleMessageType messageType) {
                return false;
            }
        });
        mX5WebClient = new XWalkWebClient(webView);
        webView.setResourceClient(mX5WebClient);
    }

    private class XWalkWebClient extends XWalkResourceClient {
        private ParseCallback callback;

        public void setCallback(ParseCallback callback) {
            this.callback = callback;
        }

        public XWalkWebClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onDocumentLoadedInFrame(XWalkView view, long frameId) {
            super.onDocumentLoadedInFrame(view, frameId);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
        }

        @Override
        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
            String url = request.getUrl().toString();
            // suppress favicon requests as we don't display them anywhere
            if (url.endsWith("/favicon.ico")) {
                return createXWalkWebResourceResponse("image/png", null, null);
            }
            LOG.i("shouldInterceptLoadRequest url:" + url);
            boolean ad;
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url);
                loadedUrls.put(url, ad);
            } else {
                ad = loadedUrls.get(url);
            }
            if (!ad && !loadFound) {
                if (DefaultConfig.isVideoFormat(url)) {
                    loadFound = true;
                    mHandler.removeCallbacks(mParseTimeOut);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.stopLoading();
                            callback.success(url, null);
                        }
                    });
                }
            }
            return ad || loadFound ?
                    createXWalkWebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes())) :
                    super.shouldInterceptLoadRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String s) {
            return false;
        }

        @Override
        public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
            callback.onReceiveValue(true);
        }
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewById(@IdRes int viewId) {
        View view = null;
        if (rootView != null) {
            view = rootView.findViewById(viewId);
        }
        return (T) view;
    }
}