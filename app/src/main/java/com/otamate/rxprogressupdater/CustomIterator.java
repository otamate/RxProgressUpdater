package com.otamate.rxprogressupdater;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomIterator implements Iterable<Long> {
    private static final String TAG = CustomIterator.class.getSimpleName();
    private List<Long> numbersList = new ArrayList<>();

    public CustomIterator() {
        for (long i = 0; i < MainActivity.MAX_PROGRESS; i++) {
            numbersList.add(i + 1);
        }
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < numbersList.size() && numbersList.get(currentIndex) != null;
            }

            @Override
            public Long next() {

                // Never do this on UI thread!
                SystemClock.sleep(MainActivity.EMIT_DELAY_MS);
                Log.d(TAG, "next() " + numbersList.get(currentIndex));

                return numbersList.get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
