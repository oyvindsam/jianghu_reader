package com.example.samue.jianghureader.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.samue.jianghureader.data.NovelContract.NovelEntry;

/**
 * Created by samuelsen on 6/24/17.
 */

public class NovelProvider extends ContentProvider {

    public static final String LOG_TAG = NovelProvider.class.getSimpleName();

    private static final int NOVELS = 100;
    private static final int NOVEL_ID = 101;

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(NovelContract.CONTENT_AUTHORITY, NovelContract.PATH_NOVELS, NOVELS);
        sUriMatcher.addURI(NovelContract.CONTENT_AUTHORITY, NovelContract.PATH_NOVELS + "/#", NOVEL_ID);
    }

    public NovelDbHelper mNovelDbHelper;


    @Override
    public boolean onCreate() {
        mNovelDbHelper = new NovelDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mNovelDbHelper.getReadableDatabase();
        Cursor cursor;
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case NOVELS:
                cursor = db.query(
                        NovelEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case NOVEL_ID:
                selection = NovelEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(
                        NovelEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), NovelEntry.CONTENT_URI);
        return cursor;
    }



    @Nullable
    @Override
    public Uri insert( Uri uri,  ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case NOVELS:
                return insertNovel(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for unknown uri " + uri);
        }
    }

    private Uri insertNovel(Uri uri, ContentValues values) {
        String novelName = values.getAsString(NovelEntry.COLUMN_NOVEL_NAME);
        String novelTocLink = values.getAsString(NovelEntry.COLUMN_NOVEL_TOC_LINK);
        String novelLastLink = values.getAsString(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK);

        if (TextUtils.isEmpty(novelName)) {
            throw new IllegalArgumentException("Novel requires a name");
        } else if (TextUtils.isEmpty(novelTocLink)) {
            throw new IllegalArgumentException("Novel requires a ToC link");
        }

        SQLiteDatabase db = mNovelDbHelper.getWritableDatabase();
        long id = db.insert(
                NovelEntry.TABLE_NAME,
                null,
                values
        );

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case NOVELS:
                return updateNovel(uri, values, selection, selectionArgs);
            case NOVEL_ID:
                selection = NovelEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNovel(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateNovel(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(NovelEntry.COLUMN_NOVEL_NAME)) {
            String novelName = values.getAsString(NovelEntry.COLUMN_NOVEL_NAME);
            if (TextUtils.isEmpty(novelName)) {
                throw new IllegalArgumentException("Novel name can't be NULL");
            }
        }
        if (values.containsKey(NovelEntry.COLUMN_NOVEL_TOC_LINK)) {
            String novelTocLink = values.getAsString(NovelEntry.COLUMN_NOVEL_TOC_LINK);
            if (TextUtils.isEmpty(novelTocLink)) {
                throw new IllegalArgumentException("Novel ToC link can't be NULL");
            }
        }
        if (values.containsKey(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK)) {
            String novelLastLink = values.getAsString(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK);
            if (TextUtils.isEmpty(novelLastLink)) {
                throw new IllegalArgumentException("Novel last read link can't be NULL");
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        SQLiteDatabase db = mNovelDbHelper.getWritableDatabase();
        return db.update(
                NovelEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case NOVELS:
                break;
            case NOVEL_ID:
                selection = "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        SQLiteDatabase db = mNovelDbHelper.getWritableDatabase();

        return db.delete(
                NovelEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
    }



    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOVELS:
                return NovelEntry.CONTENT_LIST_TYPE;
            case NOVEL_ID:
                return NovelEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
