package com.example.samue.jianghureader.layout;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.samue.jianghureader.ChapterActivity;
import com.example.samue.jianghureader.LinkAdapter;
import com.example.samue.jianghureader.MainActivity;
import com.example.samue.jianghureader.Novel;
import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.data.FavoriteNovelDbHelper;
import com.example.samue.jianghureader.data.LastNovelDbHelper;
import com.example.samue.jianghureader.data.NovelComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FavoriteFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final int NOVEL_LOADER = 1;


    LastNovelDbHelper mLastNovelDb;
    FavoriteNovelDbHelper mFavoriteNovelDb;
    private View rootView;
    private ListView favoriteListView;
    LinkAdapter favoriteAdapter;
    List<Novel> favoriteNovelList;
    MainActivity context;

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
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = (MainActivity) getContext();

        mLastNovelDb = new LastNovelDbHelper(getContext());
        mFavoriteNovelDb = new FavoriteNovelDbHelper(getContext());
        favoriteNovelList = new ArrayList<>();

        favoriteAdapter = new LinkAdapter(rootView.getContext(), favoriteNovelList, this);
        favoriteListView = (ListView) rootView.findViewById(R.id.novel_list_favorite);
        favoriteListView.setAdapter(favoriteAdapter);

        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChapterActivity.class);
                intent.putExtra(MainActivity.EXTRA_NOVEL_NAME, favoriteNovelList.get(position).getNovelName());
                intent.putExtra(MainActivity.EXTRA_NOVEL_LINK, favoriteNovelList.get(position).getNovelLink());
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void removeNovel(int position) {

        String novelName = favoriteNovelList.get(position).getNovelName();
        String novelLink = favoriteNovelList.get(position).getNovelLink();

        context.getNovelsFragment().addNovel(new Novel(novelName, novelLink));

        favoriteNovelList.remove(position);

        boolean removedSuccessful = mFavoriteNovelDb.delete(mFavoriteNovelDb.getReadableDatabase(), novelName);
        if (removedSuccessful) {
            Toast.makeText(rootView.getContext(), "Novel removed.", Toast.LENGTH_SHORT).show();
            displayDatabaseInfo();
        } else {
            Toast.makeText(rootView.getContext(), "Error deleting novel", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        displayDatabaseInfo();
        super.onResume();
    }

    public void displayDatabaseInfo() {
        favoriteNovelList = new ArrayList<>(mFavoriteNovelDb.getFavorites(mFavoriteNovelDb.getReadableDatabase()));
        Collections.sort(favoriteNovelList, new NovelComparator());
        favoriteAdapter.clear();
        favoriteAdapter.addAll(favoriteNovelList);
        favoriteAdapter.notifyDataSetChanged();
    }

    public List<Novel> getFavoriteList() {
        return favoriteNovelList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_reload).setVisible(true);
    }

}
