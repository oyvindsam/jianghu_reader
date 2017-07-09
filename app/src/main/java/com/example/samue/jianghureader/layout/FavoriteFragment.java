package com.example.samue.jianghureader.layout;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.MainActivity;
import com.example.samue.jianghureader.data.NovelCursorAdapter;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.data.NovelContract;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;


public class FavoriteFragment extends Fragment {

    private NovelCursorAdapter mCursorAdapter;
    private static int CURSOR_FAVORITE_LOADER_ID = 4;

    private MainActivity mContext;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_favorite_list, container, false);
        mContext = (MainActivity) getContext();

        mContext.getSupportLoaderManager().initLoader(CURSOR_FAVORITE_LOADER_ID, null, cursorFavoriteNovelsListener);

        ListView favoriteListView = (ListView) rootView.findViewById(R.id.novel_list_favorite);
        mCursorAdapter = new NovelCursorAdapter(getContext(), null);
        favoriteListView.setAdapter(mCursorAdapter);

        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChapterActivity.class);
                Uri uri = ContentUris.withAppendedId(NovelEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // menu.findItem(R.id.action_reset).setVisible(false);
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorFavoriteNovelsListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {
                    NovelEntry._ID,
                    NovelEntry.COLUMN_NOVEL_NAME,
                    NovelEntry.COLUMN_NOVEL_TOC_LINK,
                    NovelEntry.COLUMN_NOVEL_IS_FAVORITE
            };
            String selection = NovelEntry.COLUMN_NOVEL_IS_FAVORITE + "=?";
            String[] selectionArgs = { "1" };

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

}
