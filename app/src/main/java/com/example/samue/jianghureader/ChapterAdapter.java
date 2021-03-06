package com.example.samue.jianghureader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.samue.jianghureader.model.Chapter;

import java.util.List;

/**
 * Created by samue on 14.03.2017.
 * This displays the Novel chapters and links to the chapter
 */

public class ChapterAdapter extends ArrayAdapter<Chapter> {

    public ChapterAdapter(Context context, List<Chapter> chapters) {
        super(context, 0, chapters);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.chapter_item, parent, false);
        }

        Chapter currentChapter = getItem(position);

        TextView chapterName = (TextView) listItemView.findViewById(R.id.chapter_name);
        chapterName.setText(currentChapter.getChapterName());

        return listItemView;
    }
}



