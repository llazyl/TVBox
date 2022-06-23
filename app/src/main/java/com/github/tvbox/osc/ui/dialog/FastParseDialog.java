package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.github.tvbox.osc.R;
import com.lzy.okgo.OkGo;

/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/27
 */
public class FastParseDialog {
    private View rootView;
    private Dialog mDialog;

    public FastParseDialog() {

    }

    public FastParseDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.fast_parse_dialog, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.setContentView(rootView);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OkGo.getInstance().cancelTag("fast_parse");
            }
        });
        init(context);
        return this;
    }

    private void init(Context context) {

    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }
}
