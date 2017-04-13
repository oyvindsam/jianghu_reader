package com.example.samue.jianghureader.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.samue.jianghureader.Chapter;
import com.example.samue.jianghureader.data.NovelContract.NovelKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by samue on 11.04.2017.
 */

public class LastNovelDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lastread.db";
    private static final int DATABASE_VERSION = 1;

    public LastNovelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + NovelKeys.TABLE_NAME + " ("
                + NovelKeys._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NovelKeys.COLUMN_NOVEL_NAME + " TEXT NOT NULL, "
                + NovelKeys.COLUMN_NOVEL_LINK + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when database layout updates on app update...
    }

    public List<String> getLastChapter(final SQLiteDatabase database, String currentNovelName) {
        final List<String> lastChapter = new ArrayList<>();
        final String novelName = currentNovelName;
        final SQLiteDatabase db = database;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projection = {
                        NovelKeys.COLUMN_NOVEL_NAME,
                        NovelKeys.COLUMN_NOVEL_LINK };

                Cursor  cursor = db.query(NovelKeys.TABLE_NAME,
                        projection,
                        NovelKeys.COLUMN_NOVEL_NAME + " =?",
                        new String[] {novelName},
                        null,
                        null,
                        null);
                Log.v("LastNovelDbHelper.", " cursor length " + cursor.getCount());
                Log.v("Name: ", novelName);
                try {
                    if (cursor.getCount() > 0) {


                        // Figure out the index of each column
                        int nameColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_NAME);
                        int linkColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_LINK);
                        // Iterate through all the returned rows in the cursor

                        Log.v("name, link", "" + ", " + nameColumnIndex + ", " + linkColumnIndex);
                        Log.v("Cursor length", cursor.toString());
                        cursor.moveToNext();
                        // Use that index to extract the String or Int value of the word
                        // at the current row the cursor is on.

                        String currentName = cursor.getString(nameColumnIndex);
                        String currentLink = cursor.getString(linkColumnIndex);

                        lastChapter.add(currentName);
                        lastChapter.add(currentLink);
                    }
                } finally {
                    cursor.close();
                }
            }
        }).run();

        return lastChapter;
    }

    public void updateLastRead(String name, SQLiteDatabase database, ContentValues contentValues) {
        final String novelName = name;
        final SQLiteDatabase db = database;
        final ContentValues values = contentValues;

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.update(NovelKeys.TABLE_NAME,
                        values,
                        NovelKeys.COLUMN_NOVEL_NAME + " =? ",
                        new String[] {novelName});

                Log.v("Updated table: ", novelName + " -- " + values.getAsString(NovelKeys.COLUMN_NOVEL_LINK));
                db.close();
            }
        }).run();
    }

    public void delete(SQLiteDatabase database, String name) {
        final String novelName = name;
        final SQLiteDatabase db = database;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db.delete(NovelKeys.TABLE_NAME, "name = ?", new String[] { novelName });
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    db.close();
                }

            }
        }).run();
    }
}
