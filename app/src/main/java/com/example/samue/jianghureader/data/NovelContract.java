package com.example.samue.jianghureader.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by samue on 11.04.2017.
 */

public class NovelContract {

    public static final String CONTENT_AUTHORITY = "com.example.samue.jianghureader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NOVELS = "novels";

    private NovelContract(){}

    public static final class NovelEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOVELS);

        //MIME type
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOVELS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOVELS;


        public static final String TABLE_NAME = "novels";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NOVEL_NAME = "name";
        public static final String COLUMN_NOVEL_TOC_LINK = "toc_link"; // link to Toc
        public static final String COLUMN_NOVEL_LAST_CHAPTER_LINK = "last_chapter_link"; // link to last chapter
        public static final String COLUMN_NOVEL_IS_FAVORITE = "favorite";

        public static final int IS_FAVORITE = 1;
        public static final int NOT_FAVORITE = 0;

    }
}
