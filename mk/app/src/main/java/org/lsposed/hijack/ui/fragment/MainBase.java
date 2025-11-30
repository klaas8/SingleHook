package org.lsposed.hijack.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import org.lsposed.hijack.ui.activity.MainActivity;
import org.lsposed.hijack.util.AppUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class MainBase extends Fragment {

    public ExecutorService executor =
            new ThreadPoolExecutor(1, 5, 30L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    public Handler main = new Handler(Looper.getMainLooper());

    public void makeText(Object msg) {
        AppUtil.makeText(requireContext(), msg);
    }

    public void show(Object msg) {
        AppUtil.makeText(requireContext(), msg);
    }
    
    public boolean isHideIcon() {
    	Activity activity = requireActivity();
        if (activity instanceof MainActivity) {
            boolean isShow = !((MainActivity) activity).isShowIcon();
            return isShow;
        }
        return false;
    }
    
    public void setShowIcon(boolean isShow) {
    	Activity activity = requireActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setShowIcon(isShow);
        }
    }

    public void showNavigation() {
        AppUtil.event.callShowNavigation(true);
    }

    public void hideNavigation() {
        AppUtil.event.callShowNavigation(false);
    }

    public int getH() {
        Activity a = requireActivity();
        if (a instanceof MainActivity) {
            return ((MainActivity) a).getH();
        }
        return 0;
    }

    public Map<String, ?> getAll() {
        return AppUtil.prefs.getAll();
    }

    public String getString(String key, String defValue) {
        return AppUtil.prefs.getString(key, defValue);
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        return AppUtil.prefs.getStringSet(key, defValues);
    }

    public int getInt(String key, int defValue) {
        return AppUtil.prefs.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return AppUtil.prefs.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return AppUtil.prefs.getFloat(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return AppUtil.prefs.getBoolean(key, defValue);
    }

    public boolean contains(String key) {
        return AppUtil.prefs.contains(key);
    }

    public void putString(String key, String value) {
        AppUtil.prefs.edit().putString(key, value).apply();
    }

    public void putStringSet(String key, Set<String> values) {
        AppUtil.prefs.edit().putStringSet(key, values).apply();
    }

    public void putInt(String key, int value) {
        AppUtil.prefs.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        AppUtil.prefs.edit().putLong(key, value).apply();
    }

    public void putFloat(String key, float value) {
        AppUtil.prefs.edit().putFloat(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        AppUtil.prefs.edit().putBoolean(key, value).apply();
    }

    public void remove(String key) {
        AppUtil.prefs.edit().remove(key).apply();
    }

    public void clear() {
        AppUtil.prefs.edit().clear().apply();
    }

    public boolean putStringE(String key, String value) {
        return AppUtil.prefs.edit().putString(key, value).commit();
    }

    public boolean putStringSetE(String key, Set<String> values) {
        return AppUtil.prefs.edit().putStringSet(key, values).commit();
    }

    public boolean putIntE(String key, int value) {
        return AppUtil.prefs.edit().putInt(key, value).commit();
    }

    public boolean putLongE(String key, long value) {
        return AppUtil.prefs.edit().putLong(key, value).commit();
    }

    public boolean putFloatE(String key, float value) {
        return AppUtil.prefs.edit().putFloat(key, value).commit();
    }

    public boolean putBooleanE(String key, boolean value) {
        return AppUtil.prefs.edit().putBoolean(key, value).commit();
    }

    public boolean removeE(String key) {
        return AppUtil.prefs.edit().remove(key).commit();
    }

    public boolean clearE() {
        return AppUtil.prefs.edit().clear().commit();
    }
}
