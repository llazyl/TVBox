package com.github.tvbox.osc.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.receiver.CustomWebReceiver;
import com.github.tvbox.osc.ui.adapter.ParseAdapter;
import com.github.tvbox.osc.ui.dialog.ParseSetDialog;
import com.github.tvbox.osc.ui.dialog.RemoteDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ParseFragment extends BaseLazyFragment {
    private TvRecyclerView mGridView;
    private ParseAdapter parseAdapter;

    public static ParseFragment newInstance() {
        return new ParseFragment().setArguments();
    }

    public ParseFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_source_grid;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        parseAdapter = new ParseAdapter();
        mGridView.setAdapter(parseAdapter);
        mGridView.setLayoutManager(new V7GridLayoutManager(getContext(), 5));
        List<ParseBean> parseBeanList = new ArrayList<>();
        parseBeanList.addAll(ApiConfig.get().getParseBeanList());
        parseBeanList.add(ParseBean.addNewBean);
        parseAdapter.setNewData(parseBeanList);
        parseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                ParseBean bean = parseAdapter.getData().get(position);
                if (bean.isForAdd()) {
                    RemoteDialog dialog = new RemoteDialog().build(mContext);
                    dialog.show();
                } else {
                    ParseSetDialog dialog = new ParseSetDialog().OnChangeSrcListener(new ParseSetDialog.OnChangeListener() {
                        @Override
                        public void onDefault() {
                            // 当前默认解析需要刷新
                            int defaultPos = parseAdapter.getData().indexOf(ApiConfig.get().getDefaultParse());
                            parseAdapter.notifyItemChanged(defaultPos);
                            parseAdapter.notifyItemChanged(position);
                        }

                        @Override
                        public void onDelete() {
                            parseAdapter.remove(position);
                        }
                    }).bean(bean).build(mContext);
                    dialog.show();
                }
            }
        });
    }

    CustomWebReceiver.Callback refreshView = new CustomWebReceiver.Callback() {
        @Override
        public void onChange(String action, Object obj) {
            if (action.equals(CustomWebReceiver.REFRESH_PARSE)) {
                int len = parseAdapter.getData().size();
                parseAdapter.addData(len - 1, (ParseBean) obj);
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CustomWebReceiver.callback.add(refreshView);
    }

    @Override
    public void onDestroyView() {
        CustomWebReceiver.callback.remove(refreshView);
        super.onDestroyView();
    }
}