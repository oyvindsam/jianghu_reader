package com.example.samue.jianghureader.data;

import java.util.List;

/**
 * Created by samuelsen on 6/25/17.
 */

public interface WebParsingInterface<E> {

    public void startLoading();
    public void finishedLoading(List<E> data);
}
