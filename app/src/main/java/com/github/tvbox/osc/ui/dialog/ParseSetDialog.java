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
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.cache.LocalParse;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ParseSetDialog {
    private Context mContext;
    private View rootView;
    private Dialog mDialog;
    private OnChangeListener setChange;
    private ParseBean parseBean;

    public ParseSetDialog() {

    }

    public ParseSetDialog build(Context context) {
        mContext = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_parse_set, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(context);
        return this;
    }

    void refreshView() {
        TextView tvSrcDefault = findViewById(R.id.tvSrcDefault);
        TextView tvSrcDel = findViewById(R.id.tvSrcDel);
        tvSrcDefault.setText(parseBean.isDefault() ? "当前默认解析地址" : "设为默认解析地址");
        if (parseBean.isDefault()) {
            tvSrcDefault.setTextColor(Color.GRAY);
            tvSrcDel.setTextColor(Color.GRAY);
            tvSrcDel.setText("默认解析地址不可删除");
        } else {
            tvSrcDel.setTextColor(parseBean.isInternal() ? Color.GRAY : Color.BLACK);
            tvSrcDel.setText(parseBean.isInternal() ? "内置解析不可删除" : "删除此解析地址");
        }
        tvSrcDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (!ApiConfig.get().getDefaultParse().equals(parseBean)) {
                    setChange.onDefault();
                    ApiConfig.get().setDefaultParse(parseBean);
                    dismiss();
                }
            }
        });
        tvSrcDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (parseBean.isDefault()) {
                    Toast.makeText(mContext, "默认解析地址不可删除!", Toast.LENGTH_SHORT).show();
                } else if (parseBean.isInternal()) {
                    Toast.makeText(mContext, "内置解析不可删除!", Toast.LENGTH_SHORT).show();
                } else {
                    LocalParse ls = parseBean.getLocal();
                    if (ls != null)
                        RoomDataManger.delLocalParse(ls);
                    setChange.onDelete();
                    ApiConfig.get().getParseBeanList().remove(parseBean);
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

    public ParseSetDialog OnChangeSrcListener(OnChangeListener listener) {
        setChange = listener;
        return this;
    }

    public ParseSetDialog bean(ParseBean bean) {
        parseBean = bean;
        return this;
    }

    public interface OnChangeListener {
        void onDefault();

        void onDelete();
    }
}