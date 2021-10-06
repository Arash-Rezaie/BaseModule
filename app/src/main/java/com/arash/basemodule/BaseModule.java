package com.arash.basemodule;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arash.basemodule.tools.Utils;
import com.arash.basemodule.tools.vmvglue.ListenerProvider;

import org.codejargon.feather.Feather;
import org.codejargon.feather.Provides;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseModule extends Application {
    private static Context appContext;
    private static Handler handler;
    public static Feather feather;

    @Override
    public void onCreate() {
        init(getApplicationContext(), getModuleInstances());
        super.onCreate();
    }

    public List<Object> getModuleInstances() {
        List<Object> modules = new LinkedList<>();
        modules.add(this);
        modules.add(new ListenerProvider());
        return modules;
    }

    public static void init(Context context, List<Object> modules) {
        if (appContext == null) {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

            appContext = context;
            handler = new Handler(context.getMainLooper());

            feather = Feather.with(modules.toArray());
        }
    }

    @Provides
    public static Context getAppContext() {
        return appContext;
    }

    @Provides
    public static Handler getHandler() {
        return handler;
    }

    private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            Utils.log(throwable, "uncaught exception");
        }
    }
}
