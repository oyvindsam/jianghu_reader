package com.example.samue.novelreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.samue.novelreader.data.NovelContract.LastRead;

import static android.R.attr.version;

/**
 * Created by samue on 11.04.2017.
 */

public class LastNovelDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "last_read.db";
    private static final int DATABSE_VERSION = 1;

    public LastNovelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_LAST_NOVEL_TABLE = "CREATE TABLE " + LastRead.TABLE_NAME + " ("
                + LastRead._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LastRead.COLUMN_NOVEL_NAME + " TEXT NOT NULL, "
                + LastRead.COLUMN_NOVEL_LINK + " TEXT NOT NULL;";

        db.execSQL(SQL_CREATE_LAST_NOVEL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when database layout updates on app update...
    }
}
