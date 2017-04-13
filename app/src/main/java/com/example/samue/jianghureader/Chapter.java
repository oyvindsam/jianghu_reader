package com.example.samue.jianghureader;

/**
 * Created by samue on 14.03.2017.
 */

public class Chapter {

    private String novelName;
    private String chapterName;
    private String chapterLink;

    public Chapter(String novelName, String chapterName, String chapterLink) {
        this.novelName = novelName;
        this.chapterName = chapterName;
        this.chapterLink = chapterLink;
    }

    public String getNovelName() {
        return novelName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public String getChapterLink() {
        return chapterLink;
    }

}
