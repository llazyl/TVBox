package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.AbsSortJson;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.bean.SourceBeanSpeed;
import com.github.tvbox.osc.ui.adapter.SpeedTestAdapter;
import com.github.tvbox.osc.util.OkGoHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SpeedTestDialog {
    private View rootView;
    private Dialog mDialog;
    private SpeedTestAdapter speedTestAdapter;
    private TvRecyclerView mGridView;
    private Handler mHandler = new Handler();
    private List<SourceBeanSpeed> speeds = new ArrayList<>();

    public SpeedTestDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_speed_test, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        init(context);
        return this;
    }

    private final ExecutorService speedExecutorService = Executors.newFixedThreadPool(5);
    private static final int maxThreadRun = 5;
    private int threadRunCount = 0;
    private final Object lockObj = new Object();
    private int allRunCount = 0;

    private void init(Context context) {
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OkGo.getInstance().cancelTag("speed_test");
            }
        });
        mGridView = findViewById(R.id.mGridView);
        speedTestAdapter = new SpeedTestAdapter();
        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new V7LinearLayoutManager(context, 1, false));
        mGridView.setAdapter(speedTestAdapter);

        speeds.clear();
        List<SourceBean> beans = ApiConfig.get().getSourceBeanList();
        for (SourceBean bean : beans) {
            if (!bean.isActive() || bean.isAddition())
                continue;
            speeds.add(new SourceBeanSpeed(bean));
        }
        speedTestAdapter.setNewData(speeds);
        allRunCount = speeds.size();
        for (int i = 0; i < speeds.size(); i++) {
            SourceBeanSpeed beanSpeed = speeds.get(i);
            final int position = i;
            speedExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        int tempCount = 0;
                        synchronized (lockObj) {
                            tempCount = threadRunCount;
                        }
                        if (tempCount >= maxThreadRun) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                    synchronized (lockObj) {
                        threadRunCount++;
                    }
                    int type = beanSpeed.getBean().getType();
                    long[] times = new long[2];
                    OkGo.<String>get(beanSpeed.getBean().getApi())
                            .tag("speed_test")
                            .client(OkGoHelper.getSpeedTestClient(5000))
                            .execute(new AbsCallback<String>() {
                                @Override
                                public void onStart(Request<String, ? extends Request> request) {
                                    super.onStart(request);
                                    times[0] = System.currentTimeMillis();
                                }

                                @Override
                                public void onFinish() {
                                    super.onFinish();
                                    times[1] = System.currentTimeMillis();
                                    if (beanSpeed.getSpeed() == 0)
                                        beanSpeed.setSpeed((int) (times[1] - times[0]));
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            speedTestAdapter.notifyItemChanged(position);
                                        }
                                    }, 100);
                                    synchronized (lockObj) {
                                        threadRunCount--;
                                        allRunCount--;
                                        if (allRunCount <= 0) {
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Collections.sort(speeds);
                                                    speedTestAdapter.notifyDataSetChanged();
                                                    mGridView.setSelection(0);
                                                }
                                            }, 100);
                                        }
                                    }
                                }

                                @Override
                                public String convertResponse(okhttp3.Response response) throws Throwable {
                                    if (response.body() != null) {
                                        return response.body().string();
                                    } else {
                                        throw new IllegalStateException("网络请求错误");
                                    }
                                }

                                @Override
                                public void onSuccess(Response<String> response) {
                                    try {
                                        if (type == 0) {
                                            String xml = response.body();
                                            sortXml(xml);
                                        } else if (type == 1) {
                                            String json = response.body();
                                            sortJson(json);
                                        }
                                    } catch (Throwable th) {
                                        beanSpeed.setSpeed(Integer.MAX_VALUE);
                                    }
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    beanSpeed.setSpeed(Integer.MAX_VALUE);
                                }


                                void sortJson(String json) {
                                    AbsSortJson sortJson = new Gson().fromJson(json, new TypeToken<AbsSortJson>() {
                                    }.getType());
                                    AbsSortXml data = sortJson.toAbsSortXml();
                                }

                                void sortXml(String xml) {
                                    XStream xstream = new XStream(new DomDriver());//创建Xstram对象
                                    xstream.autodetectAnnotations(true);
                                    xstream.processAnnotations(AbsSortXml.class);
                                    xstream.ignoreUnknownElements();
                                    AbsSortXml data = (AbsSortXml) xstream.fromXML(xml);
                                }
                            });

                }
            });
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
}