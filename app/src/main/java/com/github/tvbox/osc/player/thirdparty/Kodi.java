package com.github.tvbox.osc.player.thirdparty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.github.tvbox.osc.base.App;

import java.util.HashMap;

public class Kodi {
    public static final String TAG = "ThirdParty.Kodi";

    private static final String PACKAGE_NAME = "org.xbmc.kodi";
    private static final String PLAYBACK_ACTIVITY = "org.xbmc.kodi.Splash";

    private static class KodiPackageInfo {
        final String packageName;
        final String activityName;

        KodiPackageInfo(String packageName, String activityName) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final KodiPackageInfo[] PACKAGES = {
            new KodiPackageInfo(PACKAGE_NAME, PLAYBACK_ACTIVITY),
    };

    /**
     * @return null if any Kodi packages not exist.
     */
    public static KodiPackageInfo getPackageInfo() {
        for (KodiPackageInfo pkg : PACKAGES) {
            try {
                ApplicationInfo info = App.getInstance().getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if (info.enabled)
                    return pkg;
                else
                    Log.v(TAG, "Kodi package `" + pkg.packageName + "` is disabled.");
            } catch (PackageManager.NameNotFoundException ex) {
                Log.v(TAG, "Kodi package `" + pkg.packageName + "` does not exist.");
            }
        }
        return null;
    }

    private static class Subtitle {
        final Uri uri;
        String name;
        String filename;

        Subtitle(Uri uri) {
            if (uri.getScheme() == null)
                throw new IllegalStateException("Scheme is missed for subtitle URI " + uri);

            this.uri = uri;
        }

        Subtitle(String uriStr) {
            this(Uri.parse(uriStr));
        }
    }


    public static boolean run(Activity activity, String url, String title, String subtitle, HashMap<String, String> headers) {
        KodiPackageInfo packageInfo = getPackageInfo();
        if (packageInfo == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageInfo.packageName);
        intent.setClassName(packageInfo.packageName, packageInfo.activityName);

        intent.setData(Uri.parse(url));
        intent.putExtra("title", title);
        intent.putExtra("name", title);
        if (headers != null && headers.size() > 0) {
            String[] hds = new String[headers.size() * 2];
            int idx = 0;
            for (String hk : headers.keySet()) {
                hds[idx] = hk;
                hds[idx + 1] = headers.get(hk).trim();
                idx += 2;
            }
            intent.putExtra("headers", headers);
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            intent.putExtra("subs", subtitle);
        }

        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Can't run Kodi", ex);
            return false;
        }
    }
}