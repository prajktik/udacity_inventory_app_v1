package com.example.android.inventory;


import android.util.Log;

public final class LogUtil{

    private static final String APP_TAG = "InventoryApp :: ";

    public static void info(String logTag, String message){
        Log.i(APP_TAG + logTag, message);
    }

    public static void verbose(String logTag, String message){
        Log.v(APP_TAG + logTag, message);
    }

    public static void error(String logTag, String message){
        Log.e(APP_TAG + logTag, message);
    }
}

