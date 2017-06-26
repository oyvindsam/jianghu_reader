package com.example.samue.jianghureader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;

/**
 * Created by samuelsen on 6/24/17.
 */

public class NovelDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "novels.db";
    public static final int DB_VERSION = 3;

    public static final String CREATE_TABLE = "CREATE TABLE " + NovelEntry.TABLE_NAME + " (" +
            NovelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NovelEntry.COLUMN_NOVEL_NAME + " TEXT NOT NULL, " +
            NovelEntry.COLUMN_NOVEL_TOC_LINK + " TEXT NOT NULL, " +
            NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK + " TEXT," +
            NovelEntry.COLUMN_NOVEL_IS_FAVORITE + " INTEGER DEFAULT 0);";

    public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + NovelEntry.TABLE_NAME;

    public NovelDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }
}
