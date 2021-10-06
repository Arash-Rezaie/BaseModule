package com.arash.basemodule.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.arash.basemodule.BaseModule;

public class HwInfo {
    private static HwInfo hwInfoInstance;
    private static int displayWidth, displayHeight;
    private static String androidId;

    @SuppressLint("HardwareIds")
    public static void init(Activity activity) {
        if(isInitialized())
            return;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = activity.getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        androidId = Settings.Secure.getString(BaseModule.getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
    }

    public static boolean isInitialized() {
        return androidId != null;
    }

    public static int getDisplayWidth() {
        return displayWidth;
    }

    public static int getDisplayHeight() {
        return displayHeight;
    }

    public static String getAndroidId() {
        return androidId;
    }
}
