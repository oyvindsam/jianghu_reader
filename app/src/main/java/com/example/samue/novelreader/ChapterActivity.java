package com.example.samue.novelreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.R.id.list;
import static com.example.samue.novelreader.MainActivity.WEBPARSE;

import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_NAME;

public class ChapterActivity extends AppCompatActivity {

    private String novelLink, novelName;
    private GridView novelChaptersTextView;
    private TextView novelHeader;
    private ProgressBar progress;
    private ChapterAdapter adapter;
    private List<Chapter> chapterLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        Intent intent = getIntent();
        novelLink = intent.getStringExtra(EXTRA_NOVEL_LINK);
        novelName = intent.getStringExtra(EXTRA_NOVEL_NAME);

        progress = (ProgressBar) findViewById(R.id.loading_spinner_chapter);
        novelChaptersTextView = (GridView) findViewById(R.id.chapter_list);

        setTitle(novelName);
        chapterLinks = new ArrayList<>();
        adapter = new ChapterAdapter(this, chapterLinks);
        novelChaptersTextView.setAdapter(adapter);
        progress.setVisibility(View.VISIBLE);

        WEBPARSE.parseChapterLinks(novelLink, this, progress);
    }

    public void setChapterLinks(List<Chapter> newChapterLinks) {
        adapter.clear();
        adapter.addAll(newChapterLinks);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_chapter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_reverse_chapter_order:
                reverseChapters();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reverseChapters() {
        Collections.reverse(chapterLinks);
        adapter.notifyDataSetChanged();
    }

}
