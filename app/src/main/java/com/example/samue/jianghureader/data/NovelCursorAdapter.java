package com.example.samue.jianghureader.data;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;
import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;

/**
 * Created by samue on 12.04.2017.
 */

public class NovelRVCursorAdapter extends
        RecyclerView.Adapter<NovelRVCursorAdapter.NovelRVCursorAdapterViewHolder> {

    private final Context mContext;
    private int position = 0;

    // --------------------------------------------------------------------
    @Override
    public NovelRVCursorAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(NovelRVCursorAdapterViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    // ---------------------------------------------------------------------

    public NovelCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags*/);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        position = cursor.getPosition();
        return LayoutInflater.from(context).inflate(R.layout.novel_item, parent, false);

    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView novelNameTextView = (TextView) view.findViewById(R.id.novel_name);
        ImageView btnAdd = (ImageView) view.findViewById(R.id.btn_add_novel);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_NAME);
        int linkColumnIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_TOC_LINK);
        int favoriteColumnIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_IS_FAVORITE);

        // Read the pet attributes from the Cursor for the current pet
        String novelName  = cursor.getString(nameColumnIndex);
        String novelTocLink = cursor.getString(linkColumnIndex);
        int novelIsFavorite = cursor.getInt(favoriteColumnIndex);

        // Link not found, skipping this entry..
        if (TextUtils.isEmpty(novelTocLink)) {
            return;
        }
        novelNameTextView.setText(novelName);

        position = cursor.getPosition();
        final Uri uri = ContentUris.withAppendedId(NovelEntry.CONTENT_URI, getItemId(position));

        if (novelIsFavorite == 1) { // is favorite
            btnAdd.setImageResource(R.drawable.ic_remove_circle_outline_white_24dp); // remove btn
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();
                    values.put(NovelEntry.COLUMN_NOVEL_IS_FAVORITE, NovelEntry.NOT_FAVORITE);
                    mContext.getContentResolver().update(
                            uri,
                            values,
                            null,
                            null
                    );
                }
            });

        } else {
            btnAdd.setImageResource(R.drawable.ic_add_circle_outline_white_24dp); // add btn, since not fav
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();
                    values.put(NovelEntry.COLUMN_NOVEL_IS_FAVORITE, NovelEntry.IS_FAVORITE);
                    mContext.getContentResolver().update(
                            uri,
                            values,
                            null,
                            null
                    );
                }
            });

        }


    }

    public class NovelRVCursorAdapterViewHolder extends RecyclerView.ViewHolder {

        public NovelRVCursorAdapterViewHolder(View view) {
            super(view);


        }

    }





}
