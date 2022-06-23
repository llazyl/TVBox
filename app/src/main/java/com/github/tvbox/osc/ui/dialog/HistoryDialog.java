package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class HistoryDialog {
    private View rootView;
    private Dialog mDialog;
    private OnHistoryListener historyListener;
    private VodInfo vodInfo;

    public HistoryDialog build(Context context, VodInfo vodInfo) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_history, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        this.vodInfo = vodInfo;
        init(context);
        return this;
    }

    private void init(Context context) {
        TextView tvLook = findViewById(R.id.tvLook);
        TextView tvDelete = findViewById(R.id.tvDelete);
        tvLook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                dismiss();
                if (historyListener != null) {
                    historyListener.onLook(vodInfo);
                }
            }
        });
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                dismiss();
                if (historyListener != null) {
                    historyListener.onDelete(vodInfo);
                }
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

    public HistoryDialog setOnHistoryListener(OnHistoryListener listener) {
        historyListener = listener;
        return this;
    }

    public interface OnHistoryListener {
        void onLook(VodInfo vodInfo);

        void onDelete(VodInfo vodInfo);
    }
}