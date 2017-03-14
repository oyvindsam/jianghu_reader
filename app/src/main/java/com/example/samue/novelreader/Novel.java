package com.example.samue.novelreader;

/**
 * Created by samue on 14.03.2017.
 */

public class Novel {
    private String novelName;
    private String chineseName;
    private String novelLink;

    public Novel(String name, String chinese, String link) {
        novelName = name;
        chineseName = chinese;
        novelLink = link;
    }

    public String getNovelName() {
        return novelName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getNovelLink() {
        return novelLink;
    }
}
