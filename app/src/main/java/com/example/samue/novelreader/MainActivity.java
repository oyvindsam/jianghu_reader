package com.example.samue.novelreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.example.samue.novelreader.data.LastNovelDbHelper;
import com.example.samue.novelreader.data.NovelContract.LastRead;

import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.N;

public class MainActivity extends AppCompatActivity {

    public static String APPLICATION_ID = "com.example.samue.novelreader";
    public static String EXTRA_NOVEL_NAME = "com.example.samue.novelreader.NOVEL_NAME";
    public static String EXTRA_NOVEL_LINK = "com.example.samue.novelreader.NOVEL_LINK";
    public static final String WUXIAWORLD = "http://www.wuxiaworld.com/";
    public static WebParse WEBPARSE = new WebParse();

    GridView novelLinksTextView;
    LinkAdapter adapter;
    ProgressBar progress;
    List<Novel> novelNameList;
    public LastNovelDbHelper mLastNovelDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_main);

        NovelsFragment novelsFragment = new NovelsFragment();

        if (findViewById(R.id.fragment_container) != null) {
            if(savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, novelsFragment).commit();
        }

        mLastNovelDbHelper = new LastNovelDbHelper(this);
    }

    private void displayDatabaseInfo() {
        SQLiteDatabase db = mLastNovelDbHelper.getReadableDatabase();

        String[] projection = {
                LastRead._ID,
                LastRead.COLUMN_NOVEL_NAME,
                LastRead.COLUMN_NOVEL_LINK };

        Cursor cursor = db.query(
                LastRead.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_reload:
                adapter.clear();
                WEBPARSE.parseNovelLinks(WUXIAWORLD, this, progress);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
