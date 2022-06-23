package com.github.tvbox.osc.spider;

import com.github.tvbox.osc.viewmodel.SourceViewModel;

public interface Spider {
    void init(SourceViewModel.SpiderCallback callback);

    void sort(SourceViewModel.SpiderCallback callback);

    void list(int t, int pg, SourceViewModel.SpiderCallback callback);

    void detail(String vid, SourceViewModel.SpiderCallback callback);

    void search(String wd, SourceViewModel.SpiderCallback callback);

    void play(String url, SourceViewModel.SpiderCallback callback);
}
