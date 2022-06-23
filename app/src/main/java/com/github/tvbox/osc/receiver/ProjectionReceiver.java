package com.github.tvbox.osc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.tvbox.osc.ui.activity.ProjectionPlayActivity;
import com.github.tvbox.osc.util.AppManager;

/**
 * @author pj567
 * @date :2021/3/5
 * @description:
 */
public class ProjectionReceiver extends BroadcastReceiver {
    public static String action = "android.content.movie.projection.Action";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (action.equals(intent.getAction()) && intent.getExtras() != null) {
            AppManager.getInstance().finishActivity(ProjectionPlayActivity.class);
            Intent newIntent = new Intent(context, ProjectionPlayActivity.class);
            newIntent.putExtra("html", intent.getExtras().getString("html"));
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(newIntent);
        }
    }
}