package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.ui.adapter.IJKOptionAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.util.List;


/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ChangeIJKCodeDialog {
    public interface Callback {
        void change();
    }

    private View rootView;
    private Dialog mDialog;
    private TvRecyclerView mGridView;
    private IJKOptionAdapter ijkOptionAdapter;
    private Callback callback;

    public ChangeIJKCodeDialog() {

    }

    public ChangeIJKCodeDialog build(Context context, Callback callback) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_change_ijk_code, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        this.callback = callback;
        init(context);
        return this;
    }

    private void init(Context context) {
        mGridView = findViewById(R.id.tvIJKCodecGrid);
        mGridView.setHasFixedSize(true);
        ijkOptionAdapter = new IJKOptionAdapter();
        mGridView.setAdapter(ijkOptionAdapter);
        mGridView.setLayoutManager(new V7LinearLayoutManager(context, 1, false));
        List<IJKCode> codes = ApiConfig.get().getIjkCodes();
        // fix size
//        ViewGroup.LayoutParams layoutParams = mGridView.getLayoutParams();
//        int height = layoutParams.height;
//        layoutParams.height = height / 3 * codes.size();
        ijkOptionAdapter.setNewData(codes);
        for (int i = 0; i < codes.size(); i++) {
            if (codes.get(i).isSelected()) {
                mGridView.setSelection(i);
                break;
            }
        }
        ijkOptionAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                for (int i = 0; i < codes.size(); i++) {
                    if (codes.get(i).isSelected()) {
                        ijkOptionAdapter.notifyItemChanged(i);
                        codes.get(i).selected(false);
                        break;
                    }
                }
                codes.get(position).selected(true);
                ijkOptionAdapter.notifyItemChanged(position);
                callback.change();
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
}