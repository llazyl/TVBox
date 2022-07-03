package com.github.tvbox.osc.player.controller;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.tvbox.osc.R;

import java.util.Map;

import xyz.doikki.videoplayer.controller.GestureVideoController;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.controller.IGestureComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 直播/点播控制器
 * 注意：此控制器仅做一个参考，如果想定制ui，你可以直接继承GestureVideoController或者BaseVideoController实现
 * 你自己的控制器
 * Created by dueeeke on 2017/4/7.
 */

public class LiveVideoController extends GestureVideoController implements View.OnClickListener {

    protected ProgressBar mLoadingProgress;

    public LiveVideoController(@NonNull Context context) {
        this(context, null);
        VideoViewManager.instance().setPlayOnMobileNetwork(true);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.box_standard_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        mLoadingProgress = findViewById(R.id.loading);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
    }

    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mControlWrapper.isFullScreen()) {
            if (isVisible) {
            } else {
            }
        }
    }

    private OnPlayStateChangedListener playStateChangedListener = null;

    public interface OnPlayStateChangedListener {
        void playStateChanged(int playState);
    }

    public void setPlayStateChangedListener(OnPlayStateChangedListener playStateChangedListener) {
        this.playStateChangedListener = playStateChangedListener;
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        switch (playState) {
            //调用release方法会回到此状态
            case VideoView.STATE_IDLE:
                mLoadingProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERED:
                mLoadingProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                mLoadingProgress.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mLoadingProgress.setVisibility(GONE);
                break;
        }
        if (playStateChangedListener != null)
            playStateChangedListener.playStateChanged(playState);
    }

    private OnScreenTapListener screenTapListener;

    public interface OnScreenTapListener {
        void tap();
    }

    public void setScreenTapListener(OnScreenTapListener screenTapListener) {
        this.screenTapListener = screenTapListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (screenTapListener != null)
            screenTapListener.tap();
        return super.onSingleTapConfirmed(e);
    }

    private OnScreenLongPressListener screenLongPressListener = null;

    public interface OnScreenLongPressListener {
        void longPress();
    }

    public void setScreenLongPressListener(OnScreenLongPressListener screenLongPressListener) {
        this.screenLongPressListener = screenLongPressListener;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (screenLongPressListener != null)
            screenLongPressListener.longPress();
        super.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
