package com.example.samue.novelreader;

/**
 * Created by samue on 14.03.2017.
 */

public class Chapter {

    private String novelName;
    private String chapterNr;
    private String chapterName;
    private String chapterLink;

    public Chapter(String novelName, String chapterNr, String chapterName, String chapterLink) {
        this.novelName = novelName;
        this.chapterNr = chapterNr;
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public String getChapterNr() {
        return chapterNr;
    }

    public String getChapterName() {
        return chapterName;
    }

    public String getChapterLink() {
        return chapterLink;
    }

    public String getNovelName() {
        return novelName;
    }
}
