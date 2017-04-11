package com.example.samue.novelreader.data;

import android.provider.BaseColumns;

/**
 * Created by samue on 11.04.2017.
 */

public class NovelContract {

    private NovelContract(){}

    public static final class LastRead implements BaseColumns{

        public static final String TABLE_NAME = "novels";
        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_NOVEL_NAME = "name";
        public static final String COLUMN_NOVEL_LINK = "link";

    }
}
