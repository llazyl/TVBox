package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.fragment.SourceSettingFragment;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SourceSetDialog {
    private SourceSettingFragment mFragment;
    private View rootView;
    private Dialog mDialog;
    private OnChangeSrcListener srcSetChange;
    private SourceBean source;

    public SourceSetDialog() {

    }

    public SourceSetDialog build(SourceSettingFragment fragment) {
        mFragment = fragment;
        rootView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.dialog_source_set, null);
        mDialog = new Dialog(fragment.getContext(), R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(fragment);
        return this;
    }

    void refreshView() {
        TextView tvActive = findViewById(R.id.tvSrcActive);
        TextView tvSrcHome = findViewById(R.id.tvSrcHome);
        TextView tvSrcSort = findViewById(R.id.tvSrcSort);
        tvActive.setText(source.isActive() ? "禁用" : "启用");
        if (source.isActive()) {
            tvSrcHome.setTextColor(source.isHome() ? Color.GRAY : Color.BLACK);
            tvSrcHome.setText(source.isHome() ? "当前首页数据源" : "设为首页数据源");
        } else {
            tvSrcHome.setTextColor(Color.GRAY);
            tvSrcHome.setText("尚未启用");
        }
        if (source.isHome()) {
            tvActive.setTextColor(Color.GRAY);
        }
        tvSrcSort.setTextColor(source.isActive() ? Color.BLACK : Color.GRAY);
        tvActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (source.isHome()) {
                    Toast.makeText(mFragment.getContext(), "当前首页数据源不可禁用!", Toast.LENGTH_SHORT).show();
                } else {
                    source.setActive(!source.isActive());
                    srcSetChange.onRefresh();
                    refreshView();
                    dismiss();
                }
            }
        });
        tvSrcHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (source.isActive()) {
                    srcSetChange.onHome();
                    ApiConfig.get().setSourceBean(source);
                    refreshView();
                    dismiss();
                } else {
                    Toast.makeText(mFragment.getContext(), "请先启用数据源!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tvSrcSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!source.isActive()) {
                    Toast.makeText(mFragment.getContext(), "尚未启用!", Toast.LENGTH_SHORT).show();
                } else {
                    new SourceTidSortDialog().bean(source).build(mFragment).show();
                }
            }
        });
    }

    private void init(SourceSettingFragment fragment) {
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

    public SourceSetDialog OnChangeSrcListener(OnChangeSrcListener listener) {
        srcSetChange = listener;
        return this;
    }

    public SourceSetDialog bean(SourceBean bean) {
        source = bean;
        return this;
    }

    public interface OnChangeSrcListener {
        void onHome();

        void onRefresh();

        void onDelete();
    }
}