package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveChannel;

/**
 * @author pj567
 * @date :2021/3/9
 * @description:
 */
public class LiveLocalAdapter extends BaseQuickAdapter<LiveChannel, BaseViewHolder> {
    public LiveLocalAdapter() {
        super(R.layout.item_live_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, LiveChannel item) {
        TextView tvParse = helper.getView(R.id.tvLive);
        TextView tvParseAdd = helper.getView(R.id.tvLiveAdd);
        if (item.isForAdd()) {
            tvParseAdd.setVisibility(View.VISIBLE);
            tvParse.setVisibility(View.GONE);
            return;
        }
        tvParse.setVisibility(View.VISIBLE);
        tvParseAdd.setVisibility(View.GONE);
        if (item.isDefault()) {
            tvParse.setTextColor(mContext.getResources().getColor(R.color.color_02F8E1));
        } else {
            tvParse.setTextColor(Color.WHITE);
        }
        tvParse.setText(item.getChannelName());
    }
}