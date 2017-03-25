package com.example.samue.novelreader;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_NAME;

/**
 * Created by samue on 14.03.2017.
 * This displays the Novel chapters and links to the chapter
 */

public class ChapterAdapter extends ArrayAdapter<Chapter> {


    public ChapterAdapter(Context context, ArrayList<Chapter> chapterItems) {
        super(context, 0, chapterItems);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.chapter_item, parent, false);
        }

        final Chapter currentChapter = getItem(position);

        TextView chapterName = (TextView) listItemView.findViewById(R.id.chapter_name);
        chapterName.setText(currentChapter.getChapterName());


        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ReadingActivity.class);
                intent.putExtra(EXTRA_NOVEL_LINK, currentChapter.getChapterLink());
                getContext().startActivity(intent);
            }
        });

        return listItemView;
    }
}



