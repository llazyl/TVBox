package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.IJKCode;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class IJKOptionAdapter extends BaseQuickAdapter<IJKCode, BaseViewHolder> {
    public IJKOptionAdapter() {
        super(R.layout.item_ijk_codec_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, IJKCode item) {
        TextView codeName = helper.getView(R.id.tvIJKCodecName);
        if (item.isSelected()) {
            codeName.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        } else {
            codeName.setTextColor(Color.BLACK);
        }
        codeName.setText(item.getName());
    }
}