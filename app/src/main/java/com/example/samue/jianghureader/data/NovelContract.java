package com.example.samue.jianghureader.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by samue on 11.04.2017.
 */

public class NovelContract {

    public static final String CONTENT_AUTHORITY = "com.example.samue.novelreader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NOVELS = "novels";

    private NovelContract(){}

    public static final class NovelKeys implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOVELS);


        public static final String TABLE_NAME = "novels";
        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_NOVEL_NAME = "name";
        public static final String COLUMN_NOVEL_LINK = "link";

    }
}
