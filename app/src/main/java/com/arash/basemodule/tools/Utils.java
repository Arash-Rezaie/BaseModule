package com.arash.basemodule.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.arash.basemodule.BaseModule;
import com.arash.basemodule.controllers.PermissionActivity;
import com.arash.basemodule.models.nonentities.PermissionPackage;
import com.arash.basemodule.tools.sessionmanager.SessionRepository;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utils {
    public final static String LOG_TAG = "arash_app";

    static {
        Settings.getFileLoggerEnabledObservable().observe(b -> {
            try {
                if (b)
                    initFileLogger();
            } catch (Exception e) {
                Log.e(LOG_TAG, "preparing file logger failed", e);
            }
            fileLoggerEnabled = b;
        });
    }

    public static String normalizeDigits(String str) {
        if (str == null || str.length() == 0)
            return null;
        int l = str.length(), s, e, k, g;
        char c;
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < l; i++) {
            c = str.charAt(i);
            if (c > '9' && Character.isDigit(c)) {
                k = 0;
                s = i++;
                for (; i < l && c > '9' && Character.isDigit(str.charAt(i)) && k < 16; i++, k++) ;
                e = i--;
                String t = String.valueOf(Long.parseLong(str.substring(s, e)));
                k = t.length() - (e - s);
                if (k < 0) {
                    for (; k < 0; k++)
                        sb.setCharAt(s++, '0');
                }
                sb.replace(s, e, t);
            }
        }
        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String formatDate(JalaliCalendar calendar) {
        return String.format("%04d/%02d/%02d", calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static List<PermissionPackage> getUngrantedPermissions(Activity activity, PermissionPackage... perms) {
        List<PermissionPackage> lst = new LinkedList<>();
        for (PermissionPackage p : perms)
            if (activity.checkSelfPermission(p.getPermission()) != PackageManager.PERMISSION_GRANTED)
                lst.add(p);
        return lst;
    }

    public static void checkPermissions(Activity activity, String sessionName, String onSuccessSignalName, PermissionPackage... perms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<PermissionPackage> lst = getUngrantedPermissions(activity, perms);
            if (lst.size() > 0) {
                Intent intent = new Intent(activity, PermissionActivity.class);
                intent.putExtra("data", new Object[]{sessionName, onSuccessSignalName, lst});
                activity.startActivity(intent);
            }
        } else {
            SessionRepository.getSession(sessionName).put(onSuccessSignalName, true);
        }
    }

    public static String getString(int resId, Object... formatArgs) {
        return BaseModule.getAppContext().getString(resId, formatArgs);
    }

    public static int getColor(int colorRes) {
        return BaseModule.getAppContext().getResources().getColor(colorRes);
    }

    public static float getDimen(int dimenRes) {
        return BaseModule.getAppContext().getResources().getDimension(dimenRes);
    }

    public static <T> T requireNonNull(T obj, T defaultObj) {
        return obj == null ? defaultObj : obj;
    }

    public static void showToast(String msg) {
        BaseModule.getHandler().post(() -> Toast.makeText(BaseModule.getAppContext(), msg, Toast.LENGTH_LONG).show());
    }

    public static void showToast(int msgRes) {
        showToast(getString(msgRes));
    }

    // --- logging ----------------------------------------

    public static void log(String msg) {
        Log.d(LOG_TAG, msg);
        logOnMedia(msg, null);
    }

    public static void log(Throwable throwable) {
        log(throwable, null);
    }

    public static void log(Throwable throwable, String msg) {
        Log.e(LOG_TAG, msg, throwable);
        logOnMedia(msg, throwable);
    }

    private static void logOnMedia(String msg, Throwable throwable) {
        if (fileLoggerEnabled)
            logOnFile(msg, throwable);
    }

    // --- file logger things -----------------------------
    private static boolean fileLoggerEnabled;

    private static void initFileLogger() throws IOException {
        Logger logger = getFileLogger();
        logger.setUseParentHandlers(false);
        File path = BaseModule.getAppContext().getExternalFilesDir(null);
        path = new File(path, "logs.txt");
        FileHandler fh = new FileHandler(path.getAbsolutePath(),true);
        fh.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                StringBuilder sb = new StringBuilder();
                sb.append(new Date(logRecord.getMillis()))
                        .append(" (")
                        .append(logRecord.getLevel())
                        .append(") ")
                        .append(logRecord.getMessage())
                        .append("\n\n");
                return sb.toString();
            }
        });
        logger.addHandler(fh);
    }

    private static void logOnFile(String msg, Throwable throwable) {
        Logger logger = getFileLogger();
        if (msg != null && msg.length() > 0) {
            logger.log(Level.INFO, msg);
        }
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            logger.log(Level.SEVERE, sw.toString());
        }
    }

    public static Logger getFileLogger() {
        return Logger.getLogger("FileWriterLogger");
    }
}
