package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.callback.EmptyCallback;
import com.github.tvbox.osc.callback.LoadingCallback;
import com.github.tvbox.osc.ui.adapter.SourceTidSortAdapter;
import com.github.tvbox.osc.ui.fragment.SourceSettingFragment;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SourceTidSortDialog {
    private SourceSettingFragment mFragment;
    private View rootView;
    private Dialog mDialog;
    private SourceBean source;
    private SourceTidSortAdapter sourceTidSortAdapter;
    private TvRecyclerView mGridView;
    private SourceViewModel sourceViewModel;

    private LoadService mLoadService;

    private List<MovieSort.SortData> sortDataList = new ArrayList<>();

    private MovieSort.SortData lockSort = null;

    public SourceTidSortDialog() {

    }

    public SourceTidSortDialog build(SourceSettingFragment fragment) {
        mFragment = fragment;
        rootView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.dialog_source_tid_sort, null);
        mDialog = new Dialog(fragment.getContext(), R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(fragment);
        return this;
    }

    private void init(SourceSettingFragment fragment) {
        if (mLoadService == null) {
            mLoadService = LoadSir.getDefault().register(rootView, new Callback.OnReloadListener() {
                @Override
                public void onReload(View v) {
                }
            });
        }
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // 保存
                HashMap<String, Integer> sorts = new HashMap<String, Integer>();
                for (int i = 0; i < sortDataList.size(); i++) {
                    sorts.put(sortDataList.get(i).id, i + 1);
                }
                source.setTidSort(sorts);
            }
        });
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (lockSort != null) {
                    boolean skipKey = false;
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        int currentIndex = sortDataList.indexOf(lockSort);
                        int newIndex = -1;
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_UP:
                                newIndex = currentIndex - 3;
                                skipKey = true;
                                break;
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                newIndex = currentIndex + 3;
                                skipKey = true;
                                break;
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                                newIndex = currentIndex - 1;
                                skipKey = true;
                                break;
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                                newIndex = currentIndex + 1;
                                skipKey = true;
                                break;
                        }
                        if (newIndex >= 0 && newIndex < sortDataList.size() && newIndex != currentIndex) {
                            sortDataList.remove(currentIndex);
                            sortDataList.add(newIndex, lockSort);
                            sourceTidSortAdapter.notifyItemMoved(currentIndex, newIndex);
                        }
                    }
                    return skipKey;
                }
                return false;
            }
        });
        mGridView = findViewById(R.id.mGridView);
        sourceTidSortAdapter = new SourceTidSortAdapter();
        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new V7GridLayoutManager(fragment.getContext(), 3));
        mGridView.setAdapter(sourceTidSortAdapter);
        sourceTidSortAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MovieSort.SortData currentSelect = sortDataList.get(position);
                if (lockSort != null) {
                    if (currentSelect == lockSort) {
                        lockSort.select = false;
                        sourceTidSortAdapter.notifyItemChanged(position);
                        lockSort = null;
                    }
                } else {
                    lockSort = currentSelect;
                    lockSort.select = true;
                    sourceTidSortAdapter.notifyItemChanged(position);
                }
            }
        });
        sourceViewModel = new ViewModelProvider(fragment).get(SourceViewModel.class);
        sourceViewModel.sortResult.observe(fragment, new Observer<AbsSortXml>() {
            @Override
            public void onChanged(AbsSortXml absXml) {
                if (absXml == null) {
                    showEmpty();
                } else {
                    showSuccess();
                    sortDataList.clear();
                    sortDataList.addAll(DefaultConfig.adjustSort(source.getKey(), absXml.movieSort.sortList, false));
                    sourceTidSortAdapter.setNewData(sortDataList);
                    sourceTidSortAdapter.notifyDataSetChanged();
                }
            }
        });
        sourceViewModel.getSort(source.getKey());
        showLoading();
    }

    protected void showLoading() {
        if (mLoadService != null) {
            mLoadService.showCallback(LoadingCallback.class);
        }
    }

    protected void showEmpty() {
        if (null != mLoadService) {
            mLoadService.showCallback(EmptyCallback.class);
        }
    }

    protected void showSuccess() {
        if (null != mLoadService) {
            mLoadService.showSuccess();
        }
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

    public SourceTidSortDialog bean(SourceBean bean) {
        source = bean;
        return this;
    }
}