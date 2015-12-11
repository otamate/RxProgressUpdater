package com.otamate.rxprogressupdater;

import android.app.Fragment;
import android.os.Bundle;

import rx.Observable;

public class RetainedFragment extends Fragment {
    private Observable observable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    public Observable getObservable() {
        return observable;
    }

    public void setObservable(Observable observable) {
        this.observable = observable;
    }
}
