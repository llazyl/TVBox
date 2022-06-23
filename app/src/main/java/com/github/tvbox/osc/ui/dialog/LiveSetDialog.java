package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.cache.LocalLive;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class LiveSetDialog {
    private Context mContext;
    private View rootView;
    private Dialog mDialog;
    private OnChangeListener setChange;
    private LiveChannel liveBean;

    public LiveSetDialog() {

    }

    public LiveSetDialog build(Context context) {
        mContext = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_live_set, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(context);
        return this;
    }

    void refreshView() {
        TextView tvSrcDel = findViewById(R.id.tvSrcDel);
        tvSrcDel.setTextColor(liveBean.isInternal() ? Color.GRAY : Color.BLACK);
        tvSrcDel.setText(liveBean.isInternal() ? "内置源不可删除" : "删除此直播源");
        tvSrcDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (liveBean.isInternal()) {
                    Toast.makeText(mContext, "内置源不可删除!", Toast.LENGTH_SHORT).show();
                } else {
                    LocalLive ls = liveBean.getLocal();
                    if (ls != null)
                        RoomDataManger.delLocalLive(ls);
                    ApiConfig.get().getChannelList().remove(liveBean);
                    setChange.onDelete();
                    dismiss();
                }
            }
        });
    }

    private void init(Context context) {
        refreshView();
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

    public LiveSetDialog OnChangeSrcListener(OnChangeListener listener) {
        setChange = listener;
        return this;
    }

    public LiveSetDialog bean(LiveChannel bean) {
        liveBean = bean;
        return this;
    }

    public interface OnChangeListener {
        void onDelete();
    }
}