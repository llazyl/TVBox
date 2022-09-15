/*
 *                       Copyright (C) of Avery
 *
 *                              _ooOoo_
 *                             o8888888o
 *                             88" . "88
 *                             (| -_- |)
 *                             O\  =  /O
 *                          ____/`- -'\____
 *                        .'  \\|     |//  `.
 *                       /  \\|||  :  |||//  \
 *                      /  _||||| -:- |||||-  \
 *                      |   | \\\  -  /// |   |
 *                      | \_|  ''\- -/''  |   |
 *                      \  .-\__  `-`  ___/-. /
 *                    ___`. .' /- -.- -\  `. . __
 *                 ."" '<  `.___\_<|>_/___.'  >'"".
 *                | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *                \  \ `-.   \_ __\ /__ _/   .-` /  /
 *           ======`-.____`-.___\_____/___.-`____.-'======
 *                              `=- -='
 *           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *              Buddha bless, there will never be bug!!!
 */

package com.github.tvbox.osc.subtitle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.Nullable;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.tvbox.osc.subtitle.DefaultSubtitleEngine;
import com.github.tvbox.osc.subtitle.SubtitleEngine;
import com.github.tvbox.osc.subtitle.model.Subtitle;

import java.util.List;

import xyz.doikki.videoplayer.player.AbstractPlayer;

/**
 * @author AveryZhong.
 */

@SuppressLint("AppCompatCustomView")
public class SimpleSubtitleView extends TextView
        implements SubtitleEngine, SubtitleEngine.OnSubtitleChangeListener,
        SubtitleEngine.OnSubtitlePreparedListener {

    private static final String EMPTY_TEXT = "";

    private SubtitleEngine mSubtitleEngine;

    public SimpleSubtitleView(final Context context) {
        super(context);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs,
                              final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSubtitleEngine = new DefaultSubtitleEngine();
        mSubtitleEngine.setOnSubtitlePreparedListener(this);
        mSubtitleEngine.setOnSubtitleChangeListener(this);
    }

    @Override
    public void onSubtitlePrepared(@Nullable final List<Subtitle> subtitles) {
        start();
    }

    @Override
    public void onSubtitleChanged(@Nullable final Subtitle subtitle) {
        if (subtitle == null) {
            setText(EMPTY_TEXT);
            return;
        }
        setText(Html.fromHtml(subtitle.content));
    }

    @Override
    public void setSubtitlePath(final String path) {
        mSubtitleEngine.setSubtitlePath(path);
    }

    @Override
    public void reset() {
        mSubtitleEngine.reset();
    }

    @Override
    public void start() {
        mSubtitleEngine.start();
    }

    @Override
    public void pause() {
        mSubtitleEngine.pause();
    }

    @Override
    public void resume() {
        mSubtitleEngine.resume();
    }

    @Override
    public void stop() {
        mSubtitleEngine.stop();
    }

    @Override
    public void destroy() {
        mSubtitleEngine.destroy();
    }

    @Override
    public void bindToMediaPlayer(AbstractPlayer mediaPlayer) {
        mSubtitleEngine.bindToMediaPlayer(mediaPlayer);
    }

    @Override
    public void setOnSubtitlePreparedListener(final OnSubtitlePreparedListener listener) {
        mSubtitleEngine.setOnSubtitlePreparedListener(listener);
    }

    @Override
    public void setOnSubtitleChangeListener(final OnSubtitleChangeListener listener) {
        mSubtitleEngine.setOnSubtitleChangeListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        destroy();
        super.onDetachedFromWindow();
    }
}
