package com.example.samue.jianghureader.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.samue.jianghureader.Novel;
import com.example.samue.jianghureader.data.NovelContract.NovelKeys;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samue on 11.04.2017.
 */

public class FavoriteNovelDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABSE_VERSION = 1;

    public FavoriteNovelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
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

    public boolean delete(SQLiteDatabase db, String novelName) {
        boolean successful = true;
        try {
            db.delete(NovelKeys.TABLE_NAME, "name = ?", new String[] { novelName });
        }
        catch(Exception e)
        {
            e.printStackTrace();
            successful = false;
        }
        finally
        {
            db.close();
        }
        return successful;
    }

    public List<Novel> getFavorites(SQLiteDatabase db) {
        List<Novel> favoriteNovels = new ArrayList<>();
        String[] projection = {
                NovelKeys._ID,
                NovelKeys.COLUMN_NOVEL_NAME,
                NovelKeys.COLUMN_NOVEL_LINK };

        Cursor cursor = db.query(
                NovelKeys.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        try {

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(NovelKeys._ID);
            int nameColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_NAME);
            int linkColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_LINK);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentLink = cursor.getString(linkColumnIndex);


                favoriteNovels.add(new Novel(currentName, currentLink));
            }
        } finally {
            cursor.close();
        }

        return favoriteNovels;
    }
}
