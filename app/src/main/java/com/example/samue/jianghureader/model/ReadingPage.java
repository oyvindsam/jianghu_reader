package com.example.samue.jianghureader.model;

/**
 * Created by samuelsen on 6/26/17.
 */

public class ReadingPage {


    private  String chapterLink, chapterHeader, chapterText, chapterNextLink, chapterPrevLink;

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

    public String getChapterText() {
        return chapterText;
    }

    public void setChapterText(String chapterText) {
        this.chapterText = chapterText;
    }

    public String getChapterNextLink() {
        return chapterNextLink;
    }

    public void setChapterNextLink(String chapterNextLink) {
        this.chapterNextLink = chapterNextLink;
    }

    public String getChapterPrevLink() {
        return chapterPrevLink;
    }

    public void setChapterPrevLink(String chapterPrevLink) {
        this.chapterPrevLink = chapterPrevLink;
    }

    public boolean illegalState() {
        return chapterLink == null || chapterHeader == null || chapterNextLink == null || chapterPrevLink == null;
    }
}
