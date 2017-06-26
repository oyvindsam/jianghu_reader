package com.example.samue.jianghureader.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samue.jianghureader.data.WebParsingInterface;
import com.example.samue.jianghureader.model.Chapter;
import com.example.samue.jianghureader.ChapterAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.ReadingActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;

/**
 * Created by samue on 11.04.2017.
 */

public class ChaptersFragment extends Fragment implements WebParsingInterface<Chapter> {

    private static final int CURSOR_NOVEL_NAME = 0;
    private static final int CURSOR_NOVEL_LINK = 1;
    private static final String LOG_ID = ChaptersFragment.class.getSimpleName();


    private String novelName, novelToCLink, novelLastChapterLink;
    private GridView novelChaptersGridView;
    private ProgressBar progress;
    private ChapterAdapter adapter;
    private List<Chapter> chapterLinks;
    private Uri mUri;

    public ChaptersFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_chapter, container, false);
        setHasOptionsMenu(true);

        progress = (ProgressBar) rootView.findViewById(R.id.loading_spinner_chapter);
        novelChaptersGridView = (GridView) rootView.findViewById(R.id.chapter_list);

        // Get URI, get cursor, extract data from cursor
        Intent intent = getActivity().getIntent(); // get intent from chapterActivity
        mUri = intent.getData();

        Log.v(LOG_ID, mUri.toString());

        String[] projection = {
                NovelEntry.COLUMN_NOVEL_NAME,
                NovelEntry.COLUMN_NOVEL_TOC_LINK,
                NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK
        };
        Cursor cursor = getActivity().getContentResolver().query(
                mUri,
                projection,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int novelNameIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_NAME);
        int novelToCLinkIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_TOC_LINK);
        int novelLastChapterLinkIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK);

        novelName = cursor.getString(novelNameIndex);
        novelToCLink = cursor.getString(novelToCLinkIndex);
        novelLastChapterLink = cursor.getString(novelLastChapterLinkIndex);

        getActivity().setTitle(novelName);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(novelLastChapterLink)) {
                    Intent intent = new Intent(getContext(), ReadingActivity.class);
                    intent.putExtra(EXTRA_NOVEL_LINK, novelLastChapterLink);
                    intent.setData(mUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No recent chapter found", Toast.LENGTH_SHORT).show();
                }
            }
        });


        chapterLinks = new ArrayList<>();
        adapter = new ChapterAdapter(rootView.getContext(), chapterLinks);
        novelChaptersGridView.setAdapter(adapter);

        novelChaptersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ReadingActivity.class);
                // link to chapter
                intent.putExtra(EXTRA_NOVEL_LINK, chapterLinks.get(position).getChapterLink());
                // uri so we can save last chapter read in database
                intent.setData(mUri);
                Log.v(LOG_ID, mUri.toString());
                startActivity(intent);
            }
        });

        progress.setVisibility(View.VISIBLE);

        WEBPARSE.parseChapterLinks(novelToCLink, this);

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


    @Override
    public void startLoading() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishedLoading(List<Chapter> chapters) {
        setChapterLinks(chapters);
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_reverse_chapter_order:
                reverseChapters();
                return true;
            case R.id.action_set_last_chapter:
                showSetLastChapterDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSetLastChapterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.set_last_novel);

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_chapter, (ViewGroup) getView(), false);
        final EditText editText = (EditText) viewInflated.findViewById(R.id.input_set_chapter);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.set_last_novel_load, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String chapterLink = editText.getText().toString().trim();
                if (TextUtils.isEmpty(chapterLink)) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Toast.makeText(getContext(), "No link found", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!novelIsSame(chapterLink)){
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Link does not match current novel", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), ReadingActivity.class);
                // link to chapter
                intent.putExtra(EXTRA_NOVEL_LINK, chapterLink);
                // uri so we can save last chapter read in database
                intent.setData(mUri);
                Log.v(LOG_ID, mUri.toString());
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.set_last_novel_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean novelIsSame(String otherLink) {
        String temp1 = "";
        String temp2 = "";
        int indexStart = novelToCLink.indexOf(".com/") +5;
        int indexEnd = novelToCLink.substring(indexStart).indexOf('/') + indexStart;
        temp1 = novelToCLink.substring(indexStart, indexEnd);
        Log.v(LOG_ID, "temp1: " + temp1);


        int indexStart2 = otherLink.indexOf(".com/") + 5;
        int indexEnd2 = otherLink.substring(indexStart2).indexOf('/') + indexStart2;
        temp2 = otherLink.substring(indexStart2, indexEnd2);

        Log.v(LOG_ID, temp1 + " / " + temp2);
        return temp1.equals(temp2);
    }
}
