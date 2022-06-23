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
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.receiver.CustomWebReceiver;
import com.github.tvbox.osc.ui.adapter.LiveLocalAdapter;
import com.github.tvbox.osc.ui.dialog.LiveSetDialog;
import com.github.tvbox.osc.ui.dialog.RemoteDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class LiveFragment extends BaseLazyFragment {
    private TvRecyclerView mGridView;
    private LiveLocalAdapter liveLocalAdapter;

    public static LiveFragment newInstance() {
        return new LiveFragment().setArguments();
    }

    public LiveFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_live_grid;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        liveLocalAdapter = new LiveLocalAdapter();
        mGridView.setAdapter(liveLocalAdapter);
        mGridView.setLayoutManager(new V7GridLayoutManager(getContext(), 5));
        List<LiveChannel> localLiveList = new ArrayList<>();
        for (LiveChannel lc : ApiConfig.get().getChannelList()) {
            if (!lc.isInternal())
                localLiveList.add(lc);
        }
        localLiveList.add(LiveChannel.addNewBean);
        liveLocalAdapter.setNewData(localLiveList);
        liveLocalAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                LiveChannel bean = localLiveList.get(position);
                if (bean.isForAdd()) {
                    RemoteDialog dialog = new RemoteDialog().build(mContext);
                    dialog.show();
                } else {
                    LiveSetDialog dialog = new LiveSetDialog().OnChangeSrcListener(new LiveSetDialog.OnChangeListener() {
                        @Override
                        public void onDelete() {
                            liveLocalAdapter.remove(position);
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
            if (action.equals(CustomWebReceiver.REFRESH_LIVE)) {
                int len = liveLocalAdapter.getData().size();
                liveLocalAdapter.addData(len - 1, (LiveChannel) obj);
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