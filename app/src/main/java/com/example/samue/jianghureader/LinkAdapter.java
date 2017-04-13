package com.example.samue.jianghureader;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;

/**
 * Created by samue on 14.03.2017.
 * This displays the Novel Names with links to their page
 */

public class LinkAdapter extends ArrayAdapter<Novel> {

    Fragment currentFragment;

    public LinkAdapter(Context context, List<Novel> novelItems, Fragment fragment) {
        super(context, 0, novelItems);
        currentFragment = fragment;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.novel_item, parent, false);
        }
        ImageView btnAdd = (ImageView) listItemView.findViewById(R.id.btn_add_novel);
        final Novel currentNovel = getItem(position);

        TextView novelName = (TextView) listItemView.findViewById(R.id.novel_name);
        novelName.setText(currentNovel.getNovelName());
        if (currentFragment instanceof NovelsFragment) {
            btnAdd.setImageResource(R.drawable.ic_add_circle_outline_white_24dp);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((NovelsFragment) currentFragment).removeNovel(position);
                }
            });
        } else {
            btnAdd.setImageResource(R.drawable.ic_remove_circle_outline_white_24dp);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FavoriteFragment) currentFragment).removeNovel(position);
                }
            });
        }
        return listItemView;
    }
}



