package com.example.samue.jianghureader.layout;

import android.content.ContentUris;
import android.content.ContentValues;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.MainActivity;
import com.example.samue.jianghureader.model.Novel;
import com.example.samue.jianghureader.data.NovelCursorAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.data.NovelContract;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.v;
import static com.example.samue.jianghureader.MainActivity.WUXIAWORLD;

/**
 * Created by samue on 11.04.2017.
 */

// hoved novelle siden, fragment nr 2
public class NovelsFragment extends Fragment {

    private static final String LOG_ID = NovelsFragment.class.getSimpleName();
    private static final int CURSOR_NOVELS_LOADER_ID = 1;
    private static final int WEBPARSE_NOVEL_LOADER = 2;

    private View mRootView;
    private NovelCursorAdapter mCursorAdapter;
    private MainActivity mContext;
    private ProgressBar mProgressBar;

    public NovelsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView != null) {
            Log.v("InstaReturn", "rootview");
            return mRootView;
        }
        mContext = (MainActivity) getContext();
        mRootView = inflater.inflate(R.layout.frag_novels, container, false);
        mCursorAdapter = new NovelCursorAdapter(getContext(), null);
        ListView novelList = (ListView) mRootView.findViewById(R.id.novel_list);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.loading_spinner_novels);

        mProgressBar.setVisibility(View.GONE);
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

        mContext.getSupportLoaderManager().initLoader(CURSOR_NOVELS_LOADER_ID, null, cursorNovelsLoaderListener);

        if (cursor != null && cursor.getCount() < 1) { // null elements in database, start loading
            restartWebParseLoader();
        }

        return mRootView;
    }

    private LoaderManager.LoaderCallbacks<List<Novel>> webParseNovelLoader = new LoaderManager.LoaderCallbacks<List<Novel>>() {
        @Override
        public Loader<List<Novel>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Novel>>(mContext) {
                String siteLink = args.getString("link"); // wuxiaworld.com
                List<Novel> mNovelInfo = null;

                @Override
                protected void onStartLoading() {
                    if (mNovelInfo != null) {
                        deliverResult(mNovelInfo);
                    } else {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mContext.getContentResolver().delete(
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
                        Document doc = Jsoup.connect(siteLink).get();
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
            mProgressBar.setVisibility(View.INVISIBLE);
            finishedLoading(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Novel>> loader) {

        }
    };

    public void restartWebParseLoader() {
        Bundle bundle = new Bundle();
        bundle.putString("link", WUXIAWORLD);
        mContext.getSupportLoaderManager().restartLoader(WEBPARSE_NOVEL_LOADER, bundle, webParseNovelLoader);
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
                restartWebParseLoader();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorNovelsLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
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

    private void finishedLoading(final List<Novel> novels) {
        if (novels == null) { // check if error loading data
            errorLoading();
            return;
        }

        Thread thread = new Thread() { // fix for skipping frames
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                for (Novel n : novels) {
                    values.put(NovelEntry.COLUMN_NOVEL_NAME, n.getNovelName());
                    values.put(NovelEntry.COLUMN_NOVEL_TOC_LINK, n.getNovelLink());

                    mContext.getContentResolver().insert(
                            NovelEntry.CONTENT_URI,
                            values
                    );
                }
            }
        };
        thread.start();

        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Reset complete", Toast.LENGTH_SHORT).show();

    }

    private void errorLoading() {
        Toast.makeText(mContext, "Error loading chapter", Toast.LENGTH_SHORT).show();
    }
}
