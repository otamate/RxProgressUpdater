package com.otamate.rxprogressupdater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomCollection implements Iterable<Long> {
    private List<Long> numbersList = new ArrayList<>();

    public CustomCollection() {
        for (long i = 0; i < MainActivity.MAX_PROGRESS; i++) {
            numbersList.add(i + 1);
        }
    }

    @Override
    public Iterator<Long> iterator() {
        return numbersList.iterator();
    }
}
