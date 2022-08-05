package ru.krotarnya.diasync;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class Diasync extends Application {
    private static Diasync instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
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
