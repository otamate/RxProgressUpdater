package com.otamate.rxprogressupdater;
/*
    Show a progress indicator which continues after the device has been rotated
    using several different approaches.
 */
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Observable<Long> mObservable;
    private Subscriber<Long> mSubscriber;
    private PublishSubject<Long> mSubject;

    private RetainedFragment mRetainedFragment;
    private CustomAsyncTask mCustomAsyncTask;

    public final static int MAX_PROGRESS = 10;
    public final static int EMIT_DELAY_MS = 1000;

    public Spinner mModeSpinner;
    public TextView mTextView;
    public ProgressBar mProgressBar;
    public Button mStartButton;
    public Switch mTrackLeaksSwitch;
    private static final String RETAINED_FRAGMENT = "retained_fragment";
    public static final String BROADCAST_EVENT_NAME = "broadcast-event-name";
    private String mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mStartButton = (Button) findViewById(R.id.startButton);
        mModeSpinner = (Spinner) findViewById(R.id.modeSpinner);
        mTrackLeaksSwitch = (Switch) findViewById(R.id.trackLeaksSwitch);

        mStartButton.setOnClickListener(v -> {

            mMode = mModeSpinner.getSelectedItem().toString();
            mRetainedFragment.setMode(mMode);

            setBusy(true);

            if (mMode.equals(getString(R.string.async_task))) {
                handleAsyncClick();
            } else if (mMode.equals(getString(R.string.intent_service))) {
                handleIntentServiceClick();
            } else if (mMode.equals(getString(R.string.list))) {
                handleListClick();
            } else if (mMode.equals(getString(R.string.timed_emitter))) {
                handleTimedEmitterClick();
            } else if (mMode.equals(getString(R.string.cold_observable))) {
                handleColdObservableClick();
            } else if (mMode.equals(getString(R.string.custom_collection))) {
                handleCustomCollectionClick();
            } else if (mMode.equals(getString(R.string.custom_iterator))) {
                handleCustomIteratorClick();
            }
        });

        mProgressBar.setMax(MAX_PROGRESS);
        mModeSpinner.setEnabled(mStartButton.isEnabled());

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(BROADCAST_EVENT_NAME));

        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT);

        if (mRetainedFragment == null) {
            Log.d(TAG, "New RetainedFragment");

            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, RETAINED_FRAGMENT).commit();
        } else {
            Log.d(TAG, "Recovered RetainedFragment");

            mObservable = mRetainedFragment.getObservable();

            mMode = mRetainedFragment.getMode();

            if (mMode != null) {
                if (mMode.equals(getString(R.string.async_task))) {
                    mCustomAsyncTask = mRetainedFragment.getCustomAsyncTask();
                } else if (mMode.equals(getString(R.string.timed_emitter))) {
                    mSubscriber = createSubscriber();
                } else if (mMode.equals(getString(R.string.cold_observable))) {
                    mSubscriber = createSubscriber();
                } else if (mMode.equals(getString(R.string.custom_collection))) {
                    mSubscriber = createSubscriber();
                    mSubject = mRetainedFragment.getSubject();
                } else if (mMode.equals(getString(R.string.custom_iterator))) {
                    mSubscriber = createSubscriber();
                    mSubject = mRetainedFragment.getSubject();
                }
            }
        }

        mModeSpinner.post(() -> mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMode = (String) parent.getItemAtPosition(position);

                mRetainedFragment.setMode(mMode);

                Log.d(TAG, "onItemSelected() " + parent.getItemAtPosition(position));

                if (mMode.equals(getString(R.string.async_task))) {
                    Log.d(TAG, "onCreate() Mode: Async Task");

                    mCustomAsyncTask = mRetainedFragment.getCustomAsyncTask();
                }

                if (mMode.equals(getString(R.string.cold_observable))) {
                    Log.d(TAG, "onCreate() Mode: Cold Observable");
                }

                if (mMode.equals(getString(R.string.intent_service))) {
                    Log.d(TAG, "onCreate() Mode: Intent Service");
                }

                if (mMode.equals(getString(R.string.list))) {
                    Log.d(TAG, "onCreate() Mode: List");
                }

                if (mMode.equals(getString(R.string.timed_emitter))) {
                    Log.d(TAG, "onCreate() Mode: Timed Emitter");

                    mSubscriber = createSubscriber();
                }

                if (mMode.equals(getString(R.string.custom_collection))) {
                    Log.d(TAG, "onCreate() Mode: Custom Collection");

                    mSubscriber = createSubscriber();
                    mSubject = mRetainedFragment.getSubject();
                }

                if (mMode.equals(getString(R.string.custom_iterator))) {
                    Log.d(TAG, "onCreate() Mode: Custom Iterator");

                    mSubscriber = createSubscriber();
                    mSubject = mRetainedFragment.getSubject();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected()");
            }
        }));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(CustomService.KEY_EXTRA_BUSY)) {
                Log.d(TAG, "mMessageReceiver status: " + intent.getBooleanExtra(CustomService.KEY_EXTRA_BUSY, false));

                setBusy(intent.getBooleanExtra(CustomService.KEY_EXTRA_BUSY, false));
            } else if (intent.hasExtra(CustomService.KEY_EXTRA_PROGRESS)) {
                Log.d(TAG, "mMessageReceiver Progress: " + intent.getIntExtra(CustomService.KEY_EXTRA_PROGRESS, 0));

                mProgressBar.setProgress(intent.getIntExtra(CustomService.KEY_EXTRA_PROGRESS, 0));
                mTextView.setText("Progress: " + intent.getIntExtra(CustomService.KEY_EXTRA_PROGRESS, 0));
            }
        }
    };

    private void handleAsyncClick() {
        mCustomAsyncTask = new CustomAsyncTask();
        mCustomAsyncTask.setActivity(this);

        // Store in the retained fragment
        mRetainedFragment.setCustomAsyncTask(mCustomAsyncTask);
        mCustomAsyncTask.execute();
    }

    private void handleIntentServiceClick() {
        mTextView.setText("Starting Intent Service...");

        Intent intent = new Intent(this, CustomService.class);
        startService(intent);
    }

    private void handleListClick() {
        List<Long> numbersList = new ArrayList<>();

        for (long i = 0; i < MainActivity.MAX_PROGRESS; i++) {
            numbersList.add(i + 1);
        }

        mSubscriber = createSubscriber();
        mObservable = Observable.from(numbersList);

        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubscriber);
    }

    private void handleTimedEmitterClick() {
        mSubscriber = createSubscriber();
        mObservable = Observable.interval(1, TimeUnit.SECONDS);

        mTextView.setText("Starting Timed Emitter...");

        mObservable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(MAX_PROGRESS)
            .map(x -> x + 1)
            .subscribe(mSubscriber);

        // Store in the retained fragment
        mRetainedFragment.setObservable(mObservable);
    }

    private void handleColdObservableClick() {
        mSubscriber = createSubscriber();
        mObservable = createObservable();

        mTextView.setText("Starting Cold Observable...");

        mObservable.subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mSubscriber);

        // Store in the retained fragment
        mRetainedFragment.setObservable(mObservable);
    }

    private void handleCustomCollectionClick() {
        mObservable = createObservable();
        mSubscriber = createSubscriber();
        mSubject = PublishSubject.create();

        mTextView.setText("Starting Custom Collection...");

        // Store in the retained fragment
        mRetainedFragment.setObservable(mObservable);
        mRetainedFragment.setSubject(mSubject);

        mObservable = Observable.from(new CustomCollection());

        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mSubject);

        mSubject.subscribe(mSubscriber);
    }

    private void handleCustomIteratorClick() {
        mObservable = createObservable();
        mSubscriber = createSubscriber();
        mSubject = PublishSubject.create();

        mTextView.setText("Starting Custom Iterator...");

        // Store in the retained fragment
        mRetainedFragment.setObservable(mObservable);
        mRetainedFragment.setSubject(mSubject);

        mObservable = Observable.from(new CustomIterator());

        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mSubject);

        mSubject.subscribe(mSubscriber);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause()");

        if (mSubscriber != null) {
            mSubscriber.unsubscribe();
            Log.d(TAG, "onStop() UNSUBSCRIBED");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() Leak tracking enabled: " + mTrackLeaksSwitch.isChecked());

        if (mTrackLeaksSwitch.isChecked()) {
            LeakCanary.install(getApplication());
        }
        mMode = mRetainedFragment.getMode();

        Log.d(TAG, "onResume() Mode: " + mMode + " Button enabled: " + mStartButton.isEnabled() + " Label: " + mStartButton.getText() + " Text: " + mTextView.getText());

        if (mMode != null) {
            if (mMode.equals(getString(R.string.async_task))) {
                mCustomAsyncTask = mRetainedFragment.getCustomAsyncTask();

                if (mCustomAsyncTask != null) {
                    if (!mCustomAsyncTask.isCompleted()) {
                        mCustomAsyncTask.setActivity(this);
                    } else {
                        mRetainedFragment.setCustomAsyncTask(null);
                    }
                }
            } else {
                if (mObservable != null) {
                    if (mMode.equals(getString(R.string.timed_emitter))) {
                        mObservable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(MAX_PROGRESS)
                            .map(x -> x + 1)
                            .subscribe(mSubscriber);
                    } else if (mMode.equals(getString(R.string.cold_observable))) {
                        mObservable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(mSubscriber);
                    } else if (mMode.equals(getString(R.string.custom_collection))) {
                        mSubject.subscribe(mSubscriber);
                    } else if (mMode.equals(getString(R.string.custom_iterator))) {
                        mSubject.subscribe(mSubscriber);
                    }
                }
            }
        }

        setBusy(mRetainedFragment.isBusy());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private Observable<Long> createObservable() {
        return Observable.create (
            new Observable.OnSubscribe<Long>() {

                @Override
                public void call(Subscriber<? super Long> sub) {
                    for (long i = 1; i < MAX_PROGRESS + 1; i++) {

                        Log.d("Observable", i + " Hello, timed world! On UI Thread? :" + (Looper.myLooper() == Looper.getMainLooper()));

                        sub.onNext(i);
                        SystemClock.sleep(EMIT_DELAY_MS);
                    }
                    sub.onCompleted();
                }
            }
        );
    }

    private Subscriber<Long> createSubscriber() {
        return new Subscriber<Long>() {
            boolean hasReported = false;

            @Override
            public void onNext(Long val) {
                mTextView.setText("Progress: " + val);

                mProgressBar.setProgress(val.intValue());

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

                setBusy(false);

                // Store in the retained fragment
                mRetainedFragment.setObservable(null);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("Subscriber", "Error: " + e);

                setBusy(false);
                mTextView.setText("Error!");
                mObservable = null;

                // Store in the retained fragment
                mRetainedFragment.setObservable(null);
            }
        };
    }

    public void setBusy(boolean busy) {
        if (mProgressBar.getProgress() > 0 && mProgressBar.getProgress() != mProgressBar.getMax()) {
            mTextView.setText("Progress: " + mProgressBar.getProgress());
        } else {
            mTextView.setText(busy ? "Busy" : "Idle");
        }

        mStartButton.setText(busy ?  "Busy" : "Start");

        mStartButton.setEnabled(!busy);
        mModeSpinner.setEnabled(!busy);
        mRetainedFragment.setBusy(busy);
    }
}
