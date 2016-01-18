package com.otamate.rxprogressupdater;

import android.app.Fragment;
import android.os.Bundle;

import rx.Observable;
import rx.subjects.PublishSubject;

public class RetainedFragment extends Fragment {
    private CustomAsyncTask mCustomAsyncTask;
    private Observable<Long> mObservable;
    private PublishSubject<Long> mSubject;
    private String mMode;
    private boolean mBusy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    public CustomAsyncTask getCustomAsyncTask() {
        return mCustomAsyncTask;
    }

    public void setCustomAsyncTask(CustomAsyncTask customAsyncTask) {
        mCustomAsyncTask = customAsyncTask;
    }

    public PublishSubject<Long> getSubject() {
        return mSubject;
    }

    public void setSubject(PublishSubject<Long> subject) {
        mSubject = subject;
    }

    public Observable<Long> getObservable() {
        return mObservable;
    }

    public void setObservable(Observable<Long> mObservable) {
        this.mObservable = mObservable;
    }

    public String getMode() {
        return mMode;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public boolean isBusy() {
        return mBusy;
    }

    public void setBusy(boolean busy) {
        mBusy = busy;
    }
}
