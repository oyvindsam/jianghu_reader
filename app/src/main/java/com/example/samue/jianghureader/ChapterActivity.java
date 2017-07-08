package com.example.samue.jianghureader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.List;

import com.example.samue.jianghureader.data.NovelDbHelper;
import com.example.samue.jianghureader.layout.ChaptersFragment;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;


import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_URI;

public class ChapterActivity extends AppCompatActivity {

    public ChaptersFragment chaptersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_chapters);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        chaptersFragment = new ChaptersFragment();

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
        getMenuInflater().inflate(R.menu.menu_chapter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
