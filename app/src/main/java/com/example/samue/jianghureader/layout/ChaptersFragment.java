package com.example.samue.jianghureader.layout;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samue.jianghureader.Chapter;
import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.ChapterAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.ReadingActivity;
import com.example.samue.jianghureader.data.LastNovelDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;

/**
 * Created by samue on 11.04.2017.
 */

public class ChaptersFragment extends Fragment {

    private static final int CURSOR_NOVEL_NAME = 0;
    private static final int CURSOR_NOVEL_LINK = 1;

    private String novelLink, novelName;
    private GridView novelChaptersTextView;
    private ProgressBar progress;
    private ChapterAdapter adapter;
    private List<Chapter> chapterLinks;
    private LastNovelDbHelper mLastNovelDb;

    public ChaptersFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_chapter, container, false);

        mLastNovelDb = new LastNovelDbHelper(getContext());
        novelLink = getArguments().getString(EXTRA_NOVEL_LINK);
        novelName = getArguments().getString(EXTRA_NOVEL_NAME);

        progress = (ProgressBar) rootView.findViewById(R.id.loading_spinner_chapter);
        novelChaptersTextView = (GridView) rootView.findViewById(R.id.chapter_list);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> lastChapter = mLastNovelDb.getLastChapter(mLastNovelDb.getReadableDatabase(), novelName);
                if (lastChapter.size() > 0) {
                    Intent intent = new Intent(getContext(), ReadingActivity.class);
                    intent.putExtra(EXTRA_NOVEL_NAME, novelName);
                    intent.putExtra(EXTRA_NOVEL_LINK, lastChapter.get(CURSOR_NOVEL_LINK));
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No recent chapter found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chapterLinks = new ArrayList<>();
        adapter = new ChapterAdapter(rootView.getContext(), chapterLinks);
        novelChaptersTextView.setAdapter(adapter);
        novelChaptersTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ReadingActivity.class);
                intent.putExtra(EXTRA_NOVEL_NAME, novelName);
                intent.putExtra(EXTRA_NOVEL_LINK, chapterLinks.get(position).getChapterLink());
                startActivity(intent);
            }
        });

        progress.setVisibility(View.VISIBLE);

        WEBPARSE.parseChapterLinks(novelLink, (ChapterActivity) rootView.getContext(), progress);

        return rootView;
    }


    public void setChapterLinks(List<Chapter> newChapterLinks) {
        adapter.clear();
        adapter.addAll(newChapterLinks);
        progress.setVisibility(View.INVISIBLE);
    }

    public void reverseChapters() {
        Collections.reverse(chapterLinks);
        adapter.notifyDataSetChanged();
    }

    public void clearRecentChapter() {
        mLastNovelDb.delete(mLastNovelDb.getWritableDatabase(), novelName);
    }


}
