package com.github.tvbox.osc.callback;

import com.kingja.loadsir.callback.Callback;

import com.github.tvbox.osc.R;

/**
 * @author pj567
 * @date :2020/12/24
 * @description:
 */
public class LoadingCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.loading_layout;
    }
}