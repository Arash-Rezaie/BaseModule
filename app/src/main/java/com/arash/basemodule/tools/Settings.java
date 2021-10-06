package com.arash.basemodule.tools;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.arash.basemodule.BaseModule;
import com.arash.basemodule.contracts.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Settings {
    private final static HashMap<String, Observable<?>> CONTAINER = new HashMap<>();

    private static SharedPreferences getSharedPreferences() {
        return BaseModule.getAppContext().getSharedPreferences("Memory", 0);
    }

    public static void load() {
        Map<String, ?> map = getSharedPreferences().getAll();
        Set<String> keySet = map.keySet();
        for (String s : keySet) {
            CONTAINER.put(s, new ObservableImp<>(map.get(s)));
        }
    }

    public static void persist() {
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        Set<String> keySet = CONTAINER.keySet();
        for (String s : keySet) {
            Object o = CONTAINER.get(s).getValue();
            if (o != null) {
                if (o instanceof String)
                    editor.putString(s, (String) o);
                else if (o instanceof Boolean)
                    editor.putBoolean(s, (Boolean) o);
                else if (o instanceof Integer)
                    editor.putInt(s, (Integer) o);
                else if (o instanceof Long)
                    editor.putLong(s, (Long) o);
                else if (o instanceof Float)
                    editor.putFloat(s, (Float) o);
                else
                    throw new RuntimeException("Setting value must be one of types: String, Boolean, Integer, Long or Float");
            } else {
                editor.remove(s);
            }
        }
    }

    private static Observable<?> getItem(String itemName) {
        Observable<?> observable = CONTAINER.get(itemName);
        if (observable == null) {
            synchronized (CONTAINER) {
                while (observable == null) {
                    observable = new ObservableImp<>();
                    CONTAINER.put(itemName, observable);
                }
            }
        }
        return observable;
    }

    public static Observable<Boolean> getFileLoggerEnabledObservable() {
        return (Observable<Boolean>) getItem("fileLoggerEnabled");
    }

    public static void setFileLoggerEnabled(Boolean enabled) {
        getFileLoggerEnabledObservable().setValue(enabled);
    }

    public static Observable<Boolean> getNetworkLoggerEnabledObservable() {
        return (Observable<Boolean>) getItem("networkLoggerEnabled");
    }

    public static void setNetworkLoggerEnabled(Boolean enabled) {
        getNetworkLoggerEnabledObservable().setValue(enabled);
    }
}
