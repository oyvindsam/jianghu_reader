package com.example.samue.jianghureader.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
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

import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.MainActivity;
import com.example.samue.jianghureader.SettingsActivity;
import com.example.samue.jianghureader.model.Chapter;
import com.example.samue.jianghureader.ChapterAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.ReadingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;

import com.example.samue.jianghureader.data.NovelContract.NovelEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by samue on 11.04.2017.
 */

public class ChaptersFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int WEBPARSE_CHAPTER_LOADER = 3;
    private static final String LOG_ID = ChaptersFragment.class.getSimpleName();


    private String mNovelName, mNovelTocLink, mNovelLastChapterLink;
    private ProgressBar mProgressBar;
    private ChapterAdapter mAdapter;
    private List<Chapter> mChapterList;
    private Uri mUri;
    private boolean mListChaptersAsc = true;
    private ChapterActivity mContext;

    public ChaptersFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_chapter, container, false);
        mContext = (ChapterActivity) getContext();
        setHasOptionsMenu(true);

        if (!MainActivity.hasInternetConnection(mContext)) {
            mContext.finish();
            return null;
        }

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_spinner_chapter);
        GridView novelChaptersGridView = (GridView) rootView.findViewById(R.id.chapter_list);

        // Get URI, get cursor, extract data from cursor
        Intent intent = getActivity().getIntent(); // get intent from chapterActivity
        mUri = intent.getData();

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

        if (cursor != null) {
            try {
                cursor.moveToFirst();
                int novelNameIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_NAME);
                int novelToCLinkIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_TOC_LINK);
                int novelLastChapterLinkIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK);

                mNovelName = cursor.getString(novelNameIndex);
                mNovelTocLink = cursor.getString(novelToCLinkIndex);
                mNovelLastChapterLink = cursor.getString(novelLastChapterLinkIndex);
            } catch (Exception e) {
                e.printStackTrace();
                mContext.finish();
                return null;
            } finally {
                cursor.close();
            }
        } else {
            mContext.finish();
            return null;
        }


        getActivity().setTitle(mNovelName);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if (!TextUtils.isEmpty(mNovelLastChapterLink)) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getContext(), ReadingActivity.class);
                    intent.putExtra(EXTRA_NOVEL_LINK, mNovelLastChapterLink);
                    intent.setData(mUri);
                    startActivity(intent);
                }
            });
        }

        mChapterList = new ArrayList<>();
        mAdapter = new ChapterAdapter(rootView.getContext(), mChapterList);
        novelChaptersGridView.setAdapter(mAdapter);

        novelChaptersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ReadingActivity.class);
                // link to chapter
                intent.putExtra(EXTRA_NOVEL_LINK, mChapterList.get(position).getChapterLink());
                // uri so we can save last chapter read in database
                intent.setData(mUri);
                startActivity(intent);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("link", mNovelTocLink);
        mContext.getSupportLoaderManager().initLoader(WEBPARSE_CHAPTER_LOADER, bundle, webParseChapterLoader);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mListChaptersAsc = sharedPreferences.getBoolean(getString(R.string.pref_list_asc_key),
                getResources().getBoolean(R.bool.pref_list_default));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        return rootView;
    }

    private LoaderManager.LoaderCallbacks<List<Chapter>> webParseChapterLoader = new LoaderManager.LoaderCallbacks<List<Chapter>>() {
        @Override
        public Loader<List<Chapter>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Chapter>>(mContext) {
                String novelLink = args.getString("link");
                List<Chapter> tempChapterList = null;

                @Override
                protected void onStartLoading() {
                    if (tempChapterList != null) {
                        deliverResult(tempChapterList);
                    } else {
                        mProgressBar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public List<Chapter> loadInBackground() {
                    List<Chapter> tempChapterList = new ArrayList<>();

                    try {
                        Document doc = Jsoup.connect(novelLink).get();

                        Elements elements = doc.select("div[itemprop=articleBody]"); // area where links are
                        Elements linkElements = elements.select("a[href]"); // all links
                        for (Element linkElement : linkElements) {
                            if (linkElement.text().contains("Chapter")) { // if link text contains chapter --> add
                                tempChapterList.add(new Chapter(linkElement.text(), linkElement.attr("href")));
                            }
                        }
                    } catch (IOException IOE) {
                    }
                    return tempChapterList;
                }

                @Override
                public void deliverResult(List<Chapter> data) {
                    tempChapterList = data;
                    super.deliverResult(tempChapterList);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Chapter>> loader, List<Chapter> data) {
            mProgressBar.setVisibility(View.GONE);
            setmChapterList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Chapter>> loader) {
            //mAdapter.clear();
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_list_asc_key))) {
            mListChaptersAsc = sharedPreferences.getBoolean(key,
                    getResources().getBoolean(R.bool.pref_list_default));
            reverseChapters();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void setmChapterList(List<Chapter> newChapterLinks) {
        mAdapter.clear();
        mAdapter.addAll(newChapterLinks);
        if (!mListChaptersAsc) {
            reverseChapters();
        }
    }

    public void reverseChapters() {
        Collections.reverse(mChapterList);
        mAdapter.notifyDataSetChanged();
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(mContext);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
                startActivity(settingsIntent);
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
                } else if (!novelIsSame(mNovelTocLink, chapterLink)){
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

    public static boolean novelIsSame(String link, String otherLink) {
        String temp1 = "";
        String temp2 = "";
        int indexStart = link.indexOf(".com/") +5;
        int indexEnd = link.substring(indexStart).indexOf('/') + indexStart;
        temp1 = link.substring(indexStart, indexEnd);


        int indexStart2 = otherLink.indexOf(".com/") + 5;
        int indexEnd2 = otherLink.substring(indexStart2).indexOf('/') + indexStart2;
        temp2 = otherLink.substring(indexStart2, indexEnd2);

        return temp1.equals(temp2);
    }


}
