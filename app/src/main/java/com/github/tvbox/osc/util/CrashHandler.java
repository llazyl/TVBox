package com.github.tvbox.osc.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author acer
 * @date 2018/9/10
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static volatile CrashHandler instance;
    private Context mContext;
    private PendingIntent restartIntent;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private String mExceptionInfo;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private DateFormat formatterFodder = new SimpleDateFormat("yyyyMMdd");
    private ConcurrentHashMap<String, String> mCrashInfo = new ConcurrentHashMap<>();

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    public void init(Context context, PendingIntent pendingIntent) {
        mContext = context;
        restartIntent = pendingIntent;
        //保存一份系统默认的CrashHandler
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //使用我们自定义的异常处理器替换程序默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!catchCrashException(e) && mDefaultHandler != null) {
            //没有自定义的CrashHandler的时候就调用系统默认的异常处理方式
            mDefaultHandler.uncaughtException(t, e);
        } else {
            //退出应用
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            // 退出程序并在2s后重启
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restartIntent);
            AppManager.getInstance().appExit(1);
        }
    }

    private boolean catchCrashException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        collectInfo(ex);
        //上传崩溃信息
        uploadCrashInfo();
        return true;
    }

    /**
     * 获取异常信息和设备参数信息
     */
    private void collectInfo(Throwable ex) {
        mExceptionInfo = collectExceptionInfo(ex);
        try {
            // 获得包管理器
            PackageManager mPackageManager = mContext.getPackageManager();
            // 得到该应用的信息，即主Activity
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            this.mCrashInfo.put("PackageName", mContext.getPackageName());
            if (mPackageInfo != null) {
                String versionName = mPackageInfo.versionName == null ? "null" : mPackageInfo.versionName;
                String versionCode = mPackageInfo.versionCode + "";
                this.mCrashInfo.put("VersionName", versionName);
                this.mCrashInfo.put("VersionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // 反射机制
        Field[] mFields = Build.class.getDeclaredFields();
        // 迭代Build的字段key-value 此处的信息主要是为了在服务器端手机各种版本手机报错的原因
        for (Field field : mFields) {
            try {
                field.setAccessible(true);
                mCrashInfo.put(field.getName(), field.get("").toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashInfo2File(StringBuilder sb) {
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = "/sdcard/movie/crash/" + formatterFodder.format(new Date()) + "/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取捕获异常的信息
     */
    private String collectExceptionInfo(Throwable ex) {
        Writer mWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mWriter);
        ex.printStackTrace(mPrintWriter);
        ex.printStackTrace();
        Throwable mThrowable = ex.getCause();
        // 迭代栈队列把所有的异常信息写入writer中
        while (mThrowable != null) {
            mThrowable.printStackTrace(mPrintWriter);
            // 换行 每个个异常栈之间换行
            mPrintWriter.append("\r\n");
            mThrowable = mThrowable.getCause();
        }
        // 记得关闭
        mPrintWriter.close();
        return mWriter.toString();
    }

    /**
     * 将HashMap遍历转换成StringBuffer
     */
    @NonNull
    private static StringBuilder getInfoStr(ConcurrentHashMap<String, String> info) {
        StringBuilder mStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            mStringBuilder.append(key + "=" + value + "\r\n");
        }
        return mStringBuilder;
    }

    private void uploadCrashInfo() {
        StringBuilder mStringBuilder = getInfoStr(mCrashInfo);
        mStringBuilder.append(mExceptionInfo);
        saveCrashInfo2File(mStringBuilder);
    }
}
