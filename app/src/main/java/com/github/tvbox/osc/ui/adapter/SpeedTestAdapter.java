package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.SourceBeanSpeed;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SpeedTestAdapter extends BaseQuickAdapter<SourceBeanSpeed, BaseViewHolder> {
    public SpeedTestAdapter() {
        super(R.layout.item_speed_test_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, SourceBeanSpeed item) {
        helper.setText(R.id.tvName, item.getBean().getName());
        int speed = item.getSpeed();
        int color = Color.WHITE;
        String speedText = "正在测速";
        if (speed == Integer.MAX_VALUE) {
            speedText = "连接失败";
            color = Color.RED;
        } else if (speed > 0) {
            speedText = speed + "ms";
            if (speed < 500) {
                color = Color.GREEN;
            } else {
                color = Color.RED;
            }
        }
        helper.setText(R.id.tvSpeed, speedText);
        helper.setTextColor(R.id.tvSpeed, color);
    }
}