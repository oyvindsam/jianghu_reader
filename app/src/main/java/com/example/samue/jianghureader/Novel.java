package com.example.samue.jianghureader;

/**
 * Created by samue on 14.03.2017.
 */

public class Novel {
    private String novelName;
    private String novelLink;

    public Novel(String name, String link) {
        novelName = name;
        novelLink = link;
    }

    public String getNovelName() {
        return novelName;
    }

    public String getNovelLink() {
        return novelLink;
    }
}
