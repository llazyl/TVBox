package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ChangeRenderDialog {
    private View rootView;
    private Dialog mDialog;
    private OnChangeRenderListener renderListener;

    public ChangeRenderDialog() {

    }

    public ChangeRenderDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_change_render, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(context);
        return this;
    }

    private void init(Context context) {
        TextView tvTextureView = findViewById(R.id.tvTextureView);
        TextView tvSurfaceView = findViewById(R.id.tvSurfaceView);
        int renderType = Hawk.get(HawkConfig.PLAY_RENDER, 0);
        if (renderType == 0) {
            tvTextureView.requestFocus();
            tvTextureView.setTextColor(context.getResources().getColor(R.color.color_058AF4));
        } else if (renderType == 1) {
            tvSurfaceView.requestFocus();
            tvSurfaceView.setTextColor(context.getResources().getColor(R.color.color_058AF4));
        } else {
            tvTextureView.requestFocus();
            tvTextureView.setTextColor(context.getResources().getColor(R.color.color_058AF4));
        }
        tvTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (renderType != 0 && renderListener != null) {
                    Hawk.put(HawkConfig.PLAY_RENDER, 0);
                    renderListener.onChange();
                }
                dismiss();
            }
        });
        tvSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (renderType != 1 && renderListener != null) {
                    Hawk.put(HawkConfig.PLAY_RENDER, 1);
                    renderListener.onChange();
                }
                dismiss();
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
    public ChangeRenderDialog setOnChangePlayListener(OnChangeRenderListener listener) {
        renderListener = listener;
        return this;
    }

    public interface OnChangeRenderListener {
        void onChange();
    }
}