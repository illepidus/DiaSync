package ru.krotarnya.diasync;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class Diasync extends Application {
    private static final String TAG = "Diasync";
    private static Diasync instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static PowerManager.WakeLock getWakeLock(final String name, int millis) {
        final PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, name);
        wl.acquire(millis);
        Log.d(TAG, "getWakeLock: " + name + " " + wl + "in context [" + getContext() + "]");
        return wl;
    }

    public static synchronized void releaseWakeLock(final PowerManager.WakeLock wl) {
        Log.d(TAG, "releaseWakeLock: " + wl.toString());
        if (wl.isHeld()) {
            try {
                wl.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing wakelock: " + e);
            }
        }
    }

    public static void clearDataForceClose() {
        ((ActivityManager)getContext().getSystemService(ACTIVITY_SERVICE))
                .clearApplicationUserData();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(homeIntent);
   }
}
