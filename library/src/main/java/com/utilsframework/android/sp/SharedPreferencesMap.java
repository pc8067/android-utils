package com.utilsframework.android.sp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.*;

/**
 * Created by CM on 7/26/2015.
 */
public class SharedPreferencesMap {
    private SharedPreferences preferences;

    public SharedPreferencesMap(Context context, String key) {
        preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public SharedPreferencesMap(Context context) {
        this(context, SharedPreferencesMap.class.getCanonicalName());
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public int getInt(String key) {
        if (!preferences.contains(key)) {
            throw new NoSuchElementException("No element with key " + key);
        }

        return preferences.getInt(key, 0);
    }

    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, new HashSet<String>());
    }

    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void putStringSet(String key, Set<String> strings) {
        preferences.edit().putStringSet(key, strings).apply();
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return preferences.getString(key, null);
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public boolean hasKey(String key) {
        return preferences.contains(key);
    }
}
