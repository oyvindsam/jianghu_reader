package com.example.samue.jianghureader.layout;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.MainActivity;
import com.example.samue.jianghureader.SettingsActivity;
import com.example.samue.jianghureader.data.WebParse;
import com.example.samue.jianghureader.data.WebParsingInterface;
import com.example.samue.jianghureader.model.Novel;
import com.example.samue.jianghureader.data.NovelCursorAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.data.NovelContract;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;
import com.example.samue.jianghureader.model.ReadingPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.v;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;
import static com.example.samue.jianghureader.MainActivity.WUXIAWORLD;

/**
 * Created by samue on 11.04.2017.
 */

// hoved novelle siden, fragment nr 2
public class NovelsFragment extends Fragment implements
        /*LoaderManager.LoaderCallbacks<Cursor>,*/
        WebParsingInterface<Novel> {

    private static final String LOG_ID = NovelsFragment.class.getSimpleName();

    ListView novelList;
    View rootView;
    ImageView imgBtnAdd;
    private NovelCursorAdapter mCursorAdapter;
    private static int CURSOR_LOADER_ID = 1;
    private static int WEBPARSE_LOADER_ID = 2;

    MainActivity context;
    ProgressBar progress;

    public NovelsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            Log.v("InstaReturn", "rootview");
            return rootView;
        }
        context = (MainActivity) getContext();
        rootView = inflater.inflate(R.layout.frag_novels, container, false);


        mCursorAdapter = new NovelCursorAdapter(getContext(), null);

        novelList = (ListView) rootView.findViewById(R.id.novel_list);
        imgBtnAdd = (ImageView) rootView.findViewById(R.id.btn_add_novel);

        progress = (ProgressBar) rootView.findViewById(R.id.loading_spinner_novels);
        progress.setVisibility(View.GONE);

        novelList.setAdapter(mCursorAdapter);

        novelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChapterActivity.class);
                Uri uri = ContentUris.withAppendedId(NovelEntry.CONTENT_URI, id);
                intent.setData(uri);
                Log.v(LOG_ID, uri.toString());
                startActivity(intent);
            }
        });


        Cursor cursor = getActivity().getContentResolver().query(
                NovelEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        context.getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, cursorLoaderListener);
        //context.getSupportLoaderManager().initLoader(WEBPARSE_LOADER_ID, null, webParserLoader);

        if (cursor != null && cursor.getCount() < 1) { // null elements in database, start loading
            //WEBPARSE.parseNovelLinks(WUXIAWORLD, this);
            restartWebParseLoader();
        }

        return rootView;
    }

    private LoaderManager.LoaderCallbacks<List<Novel>> webParserLoader = new LoaderManager.LoaderCallbacks<List<Novel>>() {
        @Override
        public Loader<List<Novel>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Novel>>(context) {
                String mLink = args.getString("link");
                List<Novel> mNovelInfo = null;

                @Override
                protected void onStartLoading() {
                    if (mNovelInfo != null) {
                        deliverResult(mNovelInfo);
                    } else {
                        //startLoading();
                        progress.setVisibility(View.VISIBLE);
                        context.getContentResolver().delete(
                                NovelEntry.CONTENT_URI,
                                null,
                                null
                        );
                        forceLoad();
                    }
                }

                @Override
                public List<Novel> loadInBackground() {
                    List<Novel> tempNovelNames = new ArrayList<>();

                    try {
                        Document doc = Jsoup.connect(mLink).get();
                        Elements elements = doc.select("li[id=menu-item-2165]"); // select menu item
                        Elements linkElements = elements.select("a[href]"); // get all links i an array
                        linkElements.remove(0); // first link redundant

                        for (Element linkElement : linkElements) {
                            if (linkElement.text().contains("(")) { // chinese name inside brackets ()
                                String[] nameSplit = linkElement.text().split("[(]"); // nameSlipt = { "english", "chinese"}
                                tempNovelNames.add(new Novel(nameSplit[0].trim(), linkElement.attr("href"))); // english name, link
                            } else { // if it does not have a chinese name in header
                                tempNovelNames.add(new Novel(linkElement.text(), linkElement.attr("href")));
                            }
                        }
                    } catch (IOException IOE) {
                        Log.e("MainActivity -IOE- ", "" + IOE);
                        return null; // pass null to onPostExecute, so calling activity can handle error loading
                    }
                    return tempNovelNames;
                }

                @Override
                public void deliverResult(List<Novel> data) {
                    mNovelInfo = data;
                    super.deliverResult(mNovelInfo);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Novel>> loader, List<Novel> data) {
            progress.setVisibility(View.INVISIBLE);
            finishedLoading(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Novel>> loader) {

        }
    };

    public void restartWebParseLoader() {
        Bundle bundle = new Bundle();
        bundle.putString("link", WUXIAWORLD);
        context.getSupportLoaderManager().initLoader(WEBPARSE_LOADER_ID, bundle, webParserLoader);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_reset:
                //WEBPARSE.parseNovelLinks(WUXIAWORLD, this);
                restartWebParseLoader();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    NovelEntry._ID,
                    NovelEntry.COLUMN_NOVEL_NAME,
                    NovelEntry.COLUMN_NOVEL_TOC_LINK,
                    NovelEntry.COLUMN_NOVEL_IS_FAVORITE
            };
            String selection = NovelEntry.COLUMN_NOVEL_IS_FAVORITE + "=?";
            String[] selectionArgs = { "0" };

            return new CursorLoader(getContext(),
                    NovelContract.NovelEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mCursorAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mCursorAdapter.swapCursor(null);
        }

    };

    @Override
    public void startLoading() {
        progress.setVisibility(View.VISIBLE); // add context to webparser
        context.getContentResolver().delete(
                NovelEntry.CONTENT_URI,
                null,
                null
        );
    }

    @Override
    public void finishedLoading(final List<Novel> novels) {
        if (novels == null) { // check if error loading data
            errorLoading();
            return;
        }

        /*
        context.getContentResolver().delete(
                NovelEntry.CONTENT_URI,
                null,
                null
        ); */
        Thread thread = new Thread() { // fix for skipping frames
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                for (Novel n : novels) {
                    values.put(NovelEntry.COLUMN_NOVEL_NAME, n.getNovelName());
                    values.put(NovelEntry.COLUMN_NOVEL_TOC_LINK, n.getNovelLink());

                    context.getContentResolver().insert(
                            NovelEntry.CONTENT_URI,
                            values
                    );
                }
            }
        };
        thread.start();

        progress.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Reset complete", Toast.LENGTH_SHORT).show();

    }

    private void errorLoading() {
        Toast.makeText(context, "Error loading chapter", Toast.LENGTH_SHORT).show();
    }
}
