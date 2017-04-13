package com.example.samue.jianghureader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.List;

import com.example.samue.jianghureader.layout.ChaptersFragment;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;

public class ChapterActivity extends AppCompatActivity {

    private String novelLink, novelName;
    private GridView novelChaptersTextView;
    private ProgressBar progress;
    private ChapterAdapter adapter;
    private List<Chapter> chapterLinks;
    public ChaptersFragment chaptersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_chapters);

        Intent intent = getIntent();
        String novelName = intent.getStringExtra(EXTRA_NOVEL_NAME);
        String novelLink = intent.getStringExtra(EXTRA_NOVEL_LINK);

        Bundle args = new Bundle();
        args.putString(EXTRA_NOVEL_NAME, novelName);
        args.putString(EXTRA_NOVEL_LINK, novelLink);

        chaptersFragment = new ChaptersFragment();
        chaptersFragment.setArguments(args);

        setTitle(novelName);

        if (findViewById(R.id.fragment_container_chapters) != null) {
            if(savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_chapters, chaptersFragment).commit();
        }
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
                chaptersFragment.reverseChapters();
                return true;
            case R.id.action_clear_last_chapter:
                chaptersFragment.clearRecentChapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
