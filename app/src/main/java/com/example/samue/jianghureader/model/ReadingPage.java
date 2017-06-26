package com.example.samue.jianghureader.model;

/**
 * Created by samuelsen on 6/26/17.
 */

public class ReadingPage {

    private  String chapterLink, chapterHeader, novelText, novelNextLink, novelPrevLink;

    public ReadingPage(){}


    public String getChapterLink() {
        return chapterLink;
    }

    public void setChapterLink(String chapterLink) {
        this.chapterLink = chapterLink;
    }

    public String getChapterHeader() {
        return chapterHeader;
    }

    public void setChapterHeader(String novelName) {
        this.chapterHeader = novelName;
    }

    public String getNovelText() {
        return novelText;
    }

    public void setNovelText(String novelText) {
        this.novelText = novelText;
    }

    public String getNovelNextLink() {
        return novelNextLink;
    }

    public void setNovelNextLink(String novelNextLink) {
        this.novelNextLink = novelNextLink;
    }

    public String getNovelPrevLink() {
        return novelPrevLink;
    }

    public void setNovelPrevLink(String novelPrevLink) {
        this.novelPrevLink = novelPrevLink;
    }

    public boolean illegalState() {
        return chapterLink == null || chapterHeader == null || novelNextLink == null || novelPrevLink == null;
    }
}
