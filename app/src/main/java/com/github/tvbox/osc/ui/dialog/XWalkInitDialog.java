package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.XWalkUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;

import java.io.File;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class XWalkInitDialog {
    private View rootView;
    private Dialog mDialog;
    private OnListener listener;

    public XWalkInitDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_xwalk, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OkGo.getInstance().cancelTag("down_xwalk");
            }
        });
        init(context);
        return this;
    }

    private void init(Context context) {
        TextView downText = findViewById(R.id.downXWalk);
        TextView downTip = findViewById(R.id.downXWalkArch);

        downTip.setText("下载XWalkView运行组件\nArch:" + XWalkUtils.getRuntimeAbi());

        if (XWalkUtils.xWalkLibExist(context)) {
            downText.setText("重新下载");
        }

        downText.setOnClickListener(new View.OnClickListener() {

            private void setTextEnable(boolean enable) {
                downText.setEnabled(enable);
                downText.setTextColor(enable ? Color.BLACK : Color.GRAY);
            }

            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                setTextEnable(false);
                OkGo.<File>get(XWalkUtils.downUrl()).tag("down_xwalk").execute(new FileCallback(context.getCacheDir().getAbsolutePath(), XWalkUtils.saveZipFile()) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        try {
                            XWalkUtils.unzipXWalkZip(context, response.body().getAbsolutePath());
                            XWalkUtils.extractXWalkLib(context);
                            downText.setText("重新下载");
                            if (listener != null)
                                listener.onchange();
                            dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            setTextEnable(true);
                        }
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        Toast.makeText(context, response.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setTextEnable(true);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        downText.setText(String.format("%.2f%%", progress.fraction * 100));
                    }
                });
            }
        });
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

    public XWalkInitDialog setOnListener(OnListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnListener {
        void onchange();

    }
}