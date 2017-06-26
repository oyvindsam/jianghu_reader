package com.example.samue.jianghureader.model;

/**
 * Created by samue on 14.03.2017.
 */

public class Chapter {

    private String chapterName;
    private String chapterLink;

    public Chapter(String chapterName, String chapterLink) {
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public String getChapterName() {
        return chapterName;
    }

    public String getChapterLink() {
        return chapterLink;
    }

}
