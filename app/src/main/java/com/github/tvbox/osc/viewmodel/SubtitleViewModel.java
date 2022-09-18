package com.github.tvbox.osc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.tvbox.osc.bean.Subtitle;
import com.github.tvbox.osc.bean.SubtitleData;
import com.github.tvbox.osc.ui.dialog.SearchSubtitleDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SubtitleViewModel extends ViewModel {

    public MutableLiveData<SubtitleData> searchResult;

    public SubtitleViewModel() {
        searchResult = new MutableLiveData<>();
    }

    public void searchResult(String title, int page) {
        searchResultFromAssrt(title, page);
    }

    public void getSearchResultSubtitleUrls(Subtitle subtitle) {
        getSearchResultSubtitleUrlsFromAssrt(subtitle);
    }

    public void getSubtitleUrl(Subtitle subtitle, SearchSubtitleDialog.SubtitleLoader subtitleLoader) {
        getSubtitleUrlFromAssrt(subtitle, subtitleLoader);
    }

    private void setSearchListData(List<Subtitle> data, boolean isNew, boolean isZip) {
        try {
            SubtitleData subtitleData = new SubtitleData();
            subtitleData.setSubtitleList(data);
            subtitleData.setIsNew(isNew);
            subtitleData.setIsZip(isZip);
            searchResult.postValue(subtitleData);
        } catch (Throwable e) {
            e.printStackTrace();
            searchResult.postValue(null);
        }
    }

    private void searchResultFromAssrt(String title, int page) {
        try {
            String searchApiUrl = "https://secure.assrt.net/sub/";
            OkGo.<String>get(searchApiUrl)
                    .params("searchword", title)
                    .params("page", page)
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                            try {
                                String content = response.body();
                                Document doc = Jsoup.parse(content);
                                Elements items = doc.select("div.resultcard div.subitem");

                                List<Subtitle> data = new ArrayList<>();
                                for (int i=0; i<items.size(); i++) {
                                    Element item = items.get(i);
                                    Element titleElement = item.selectFirst("a.introtitle");
                                    if (titleElement == null) {
                                        continue;
                                    }
                                    String title = titleElement.attr("title");
                                    String zipUrl = titleElement.attr("href");
                                    zipUrl = "https://secure.assrt.net" + zipUrl;
                                    Subtitle one = new Subtitle();
                                    one.setName(title);
                                    one.setUrl(zipUrl);
                                    one.setIsZip(true);
                                    data.add(one);
                                }
                                if (page > 1) {
                                    setSearchListData(data, false, true);
                                } else {
                                    setSearchListData(data, true, true);
                                }
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }

                        @Override
                        public String convertResponse(Response response) throws Throwable {
                            return response.body().string();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected Pattern regexShooterFileOnclick = Pattern.compile("onthefly\\(\"(\\d+)\",\"(\\d+)\",\"(\\S+)\"\\)");

    private void getSearchResultSubtitleUrlsFromAssrt(Subtitle subtitle) {
        try {
            String url = subtitle.getUrl();
            OkGo.<String>get(url)
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                            try {

                                String content = response.body();
                                List<Subtitle> data = new ArrayList<>();
                                Document doc = Jsoup.parse(content);
                                Element fileslistEle = doc.selectFirst("span#detail-filelist");
                                Elements files = fileslistEle.select("div");
                                for(int i=0; i<files.size(); i++) {
                                    Element item = files.get(i);
                                    String onclick = item.attr("onclick");
                                    Element titleEle = item.selectFirst("span#filelist-name");
                                    Matcher matcher = regexShooterFileOnclick.matcher(onclick);
                                    String fileUrl = "";
                                    if (matcher.find()) {
                                        fileUrl = String.format("https://secure.assrt.net/download/%s/-/%s/%s", matcher.group(1), matcher.group(2), matcher.group(3));
                                        Subtitle one = new Subtitle();
                                        one.setName(titleEle.text());
                                        one.setUrl(fileUrl);
                                        one.setIsZip(false);
                                        data.add(one);
                                    }
                                    setSearchListData(data, true, false);
                                }
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }

                        @Override
                        public String convertResponse(Response response) throws Throwable {
                            return response.body().string();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSubtitleUrlFromAssrt(Subtitle subtitle, SearchSubtitleDialog.SubtitleLoader subtitleLoader) {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36";
        Request request = new Request.Builder()
                .url(subtitle.getUrl())
                .get()
                .addHeader("Referer", "https://secure.assrt.net")
                .addHeader("User-Agent", ua)
                .build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(true);
        OkHttpClient client = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                subtitle.setUrl(response.header("location"));;
                subtitleLoader.loadSubtitle(subtitle);
            }
        });
    }

}
