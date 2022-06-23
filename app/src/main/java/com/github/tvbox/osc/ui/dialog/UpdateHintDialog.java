package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class UpdateHintDialog {
    private View rootView;
    private Dialog mDialog;
    private TextView mSure;


    public UpdateHintDialog() {

    }

    public UpdateHintDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_update_hint, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mSure = findViewById(R.id.tvSure);
        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSureListener.sure();
                dismiss();
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.setContentView(rootView);
        init(context);
        return this;
    }

    private void init(Context context) {

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


    private OnSureListener onSureListener;

    public UpdateHintDialog OnSureListener(OnSureListener listener) {
        onSureListener = listener;
        return this;
    }


    public interface OnSureListener {
        void sure();
    }
}