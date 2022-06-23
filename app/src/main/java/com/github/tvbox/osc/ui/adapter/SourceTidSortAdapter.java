package com.github.tvbox.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MovieSort;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/21
 * @description:
 */
public class SourceTidSortAdapter extends BaseQuickAdapter<MovieSort.SortData, BaseViewHolder> {
    public SourceTidSortAdapter() {
        super(R.layout.item_source_sort_tid_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, MovieSort.SortData item) {
        helper.setText(R.id.tvSortName, item.name);
        helper.setVisible(R.id.tvSortSelect, item.select);
    }
}