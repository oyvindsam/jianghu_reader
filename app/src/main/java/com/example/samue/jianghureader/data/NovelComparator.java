package com.example.samue.jianghureader.data;

import com.example.samue.jianghureader.model.Novel;

import java.util.Comparator;

/**
 * Created by samue on 13.04.2017.
 */

public class NovelComparator implements Comparator<Novel> {
    @Override
    public int compare(Novel n1, Novel n2) {
        return n1.getNovelName().compareTo(n2.getNovelName());
    }
}
