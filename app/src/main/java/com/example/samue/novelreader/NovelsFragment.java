package com.example.samue.novelreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import static com.example.samue.novelreader.MainActivity.WEBPARSE;
import static com.example.samue.novelreader.MainActivity.WUXIAWORLD;

/**
 * Created by samue on 11.04.2017.
 */

public class NovelsFragment extends Fragment {

    public NovelsFragment() {}

    GridView novelLinksTextView;
    LinkAdapter adapter;
    ProgressBar progress;
    List<Novel> novelNameList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        progress = (ProgressBar) rootView.findViewById(R.id.loading_spinner_main);
        novelLinksTextView = (GridView) rootView.findViewById(R.id.novel_list);
        novelNameList = new ArrayList<>();
        adapter = new LinkAdapter(rootView.getContext(), novelNameList);

        novelLinksTextView.setAdapter(adapter);

        WEBPARSE.parseNovelLinks(WUXIAWORLD, rootView.getContext(), progress);

        return rootView;
    }

    public void setNovelLinks(List<Novel> novelLinks) {
        progress.setVisibility(View.INVISIBLE);
        adapter.clear();
        adapter.addAll(novelLinks);
    }
}
