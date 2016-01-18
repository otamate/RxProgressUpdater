package com.otamate.rxprogressupdater;

import android.app.IntentService;
import android.content.Intent;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CustomService extends IntentService {
    private static final String TAG = CustomService.class.getSimpleName();
    public static final String KEY_EXTRA_BUSY = "busy";
    public static final String KEY_EXTRA_PROGRESS = "progress";

    public CustomService() {
        super(CustomService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() On UI Thread? :" + (Looper.myLooper() == Looper.getMainLooper()));

        Intent broadcastIntent = new Intent(MainActivity.BROADCAST_EVENT_NAME);

        broadcastIntent.putExtra(KEY_EXTRA_BUSY, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        for (int i = 1; i < MainActivity.MAX_PROGRESS + 1; i++) {
            broadcastIntent = new Intent(MainActivity.BROADCAST_EVENT_NAME);

            broadcastIntent.putExtra(KEY_EXTRA_PROGRESS, i);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            SystemClock.sleep(MainActivity.EMIT_DELAY_MS);
        }
        broadcastIntent = new Intent(MainActivity.BROADCAST_EVENT_NAME);

        broadcastIntent.putExtra(KEY_EXTRA_BUSY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

}
