package com.otamate.rxprogressupdater;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Observable<Integer> observable;
    private Subscriber<Integer> subscriber;
    public final static int MAX_PROGRESS = 10;
    private final static String PREF_NAME = "prefs";
    private final static String PREF_KEY_PROGRESS = "progress";
    private TextView textView;
    private ProgressBar progressBar;
    private Button button;
    private static final String RETAINED_FRAGMENT = "retained_fragment";
    private RetainedFragment mRetainedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                button.setText("Busy");
                button.setEnabled(false);

                observable = createObservable();
                subscriber = createSubscriber();

                // Store the observable in the retained fragment
                mRetainedFragment.setObservable(observable);

                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
            }
        });

        progressBar.setMax(MAX_PROGRESS);

        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT);

        if (mRetainedFragment == null) {
            Log.d("MainActivity", "New RetainedFragment");

            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, RETAINED_FRAGMENT).commit();
        } else {
            Log.d("MainActivity", "Recovered RetainedFragment");

            observable = mRetainedFragment.getObservable();
            subscriber = createSubscriber();

            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (subscriber != null) {
            subscriber.unsubscribe();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

/*        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Log.d("MainActivity", "Progress: " + prefs.getInt(PREF_KEY_PROGRESS, 0));

        progressBar.setMax(MAX_PROGRESS);
        progressBar.setProgress(prefs.getInt(PREF_KEY_PROGRESS, 0));

        textView.setText("Progress: " + prefs.getInt(PREF_KEY_PROGRESS, 0));*/
    }

    private Observable createObservable() {
        return Observable.create(
                new Observable.OnSubscribe<Integer>() {

                    @Override
                    public void call(Subscriber<? super Integer> sub) {

                        // Report if we are we on main UI thread
                        Log.d("Observable", "On main UI Thread: " + (Looper.myLooper() == Looper.getMainLooper()));

                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                        for (int i = 1; i < MAX_PROGRESS + 1; i++) {

                            Log.d("Observable", i + " Hello, timed world!");

                            prefs.edit().putInt(PREF_KEY_PROGRESS, i).apply();

                            sub.onNext(i);
                            SystemClock.sleep(1000);
                        }
                        sub.onCompleted();
                        prefs.edit().putInt(PREF_KEY_PROGRESS, 0).apply();
                    }
                }
        );
    }

    private Observable XcreateObservable() {
        return Observable.create(
            new Observable.OnSubscribe<Integer>() {

                @Override
                public void call(Subscriber<? super Integer> sub) {

                    // Report if we are we on main UI thread
                    Log.d("Observable", "On main UI Thread: " + (Looper.myLooper() == Looper.getMainLooper()));

                    SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                    for (int i = 1; i < MAX_PROGRESS + 1; i++) {

                        Log.d("Observable", i + " Hello, timed world!");

                        prefs.edit().putInt(PREF_KEY_PROGRESS, i).apply();

                        sub.onNext(i);
                        SystemClock.sleep(1000);
                    }
                    sub.onCompleted();
                    prefs.edit().putInt(PREF_KEY_PROGRESS, 0).apply();
                }
            }
        );
    }

    private Subscriber createSubscriber() {
        return new Subscriber<Integer>() {
            boolean hasReported = false;

            @Override
            public void onNext(Integer val) {
                textView.setText("Progress: " + val);

                progressBar.setProgress(val);

                // Report if we are we on main UI thread
                if (!hasReported) {
                    Log.d("Subscriber", "On main UI Thread: " + (Looper.myLooper() == Looper.getMainLooper()));
                    hasReported = true;
                }

                Log.d("Subscriber", "Loop " + val);
            }

            @Override
            public void onCompleted() {
                Log.d("Subscriber", "COMPLETED");
                textView.setText("Done!");
                button.setText("Idle");
                button.setEnabled(true);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("Subscriber", "Error: " + e);
            }
        };
    }
}
