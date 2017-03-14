package com.example.samue.novelreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.samue.novelreader.MainActivity.EXTRA_LINK;
import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_NAME;

/**
 * Created by samue on 14.03.2017.
 * This displays the Novel Names with links to their page
 */

public class LinkAdapter extends ArrayAdapter<Novel> {


    public LinkAdapter(Context context, ArrayList<Novel> novelItems) {
        super(context, 0, novelItems);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.novel_item, parent, false);
        }

        final Novel currentNovel = getItem(position);

        TextView novelName = (TextView) listItemView.findViewById(R.id.novel_name);
        //novelName.setTextColor(Color.WHITE);
        novelName.setText(currentNovel.getNovelName());

        TextView novelNameChinese = (TextView) listItemView.findViewById(R.id.novel_chinese_name);
        novelNameChinese.setTextColor(Color.WHITE);
        novelNameChinese.setText(currentNovel.getChineseName());


        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChapterActivity.class);
                intent.putExtra(EXTRA_LINK, currentNovel.getNovelLink());
                intent.putExtra(EXTRA_NOVEL_NAME, currentNovel.getNovelName());
                getContext().startActivity(intent);
            }
        });

        return listItemView;
    }
}



