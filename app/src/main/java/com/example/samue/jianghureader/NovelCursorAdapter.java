package com.example.samue.jianghureader;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.samue.jianghureader.data.NovelContract.NovelKeys;

/**
 * Created by samue on 12.04.2017.
 */

public class NovelCursorAdapter extends CursorAdapter {
    public NovelCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags*/);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.novel_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView novelNameTextView = (TextView) view.findViewById(R.id.novel_name);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_NAME);
        int linkColumnIndex = cursor.getColumnIndex(NovelKeys.COLUMN_NOVEL_LINK);

        // Read the pet attributes from the Cursor for the current pet
        String novelName  = cursor.getString(nameColumnIndex);
        String novelLink = cursor.getString(linkColumnIndex);

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(novelLink)) {
            return;
        }

        // Update the TextViews with the attributes for the current pet
        novelNameTextView.setText(novelName);
    }
}
