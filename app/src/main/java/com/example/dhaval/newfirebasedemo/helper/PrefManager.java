package com.example.dhaval.newfirebasedemo.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefManager {
    /**
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */


    public static String getSharedPref(Context context, String key,
                                       String defaultValue) {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        return pref.getString(key, defaultValue);
    }

    public static void setSharedPref(Context context, String key, String value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static boolean getSharedPref(Context context, String key,
                                        boolean defaultValue) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return pref.getBoolean(key, defaultValue);
    }


    public static void setSharedPref(Context context, String key, boolean value) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();

    }

    public static void delSharedPref(Context context) {

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        settings.edit().clear().apply();

    }

}
