package com.example.samue.jianghureader.layout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.example.samue.jianghureader.data.NovelContract.NovelKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.util.Log.v;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;
import static com.example.samue.jianghureader.MainActivity.WUXIAWORLD;

/**
 * Created by samue on 11.04.2017.
 */

public class NovelsFragment extends Fragment {

    public NovelsFragment() {}

    ListView novelLinksTextView;
    LinkAdapter linkAdapter;
    ProgressBar progress;
    List<Novel> novelList;
    View rootView;
    LastNovelDbHelper mLastNovelDb;
    FavoriteNovelDbHelper mFavoriteNovelDb;
    MainActivity context;
    private Button btnWW, btnTN, btnYx;

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

        rootView = inflater.inflate(R.layout.activity_main, container, false);
        final Context context = rootView.getContext();

        mLastNovelDb = new LastNovelDbHelper(rootView.getContext());
        mFavoriteNovelDb = new FavoriteNovelDbHelper((rootView.getContext()));

        progress = (ProgressBar) rootView.findViewById(R.id.loading_spinner_main);
        novelLinksTextView = (ListView) rootView.findViewById(R.id.novel_list);
        novelList = new ArrayList<>();
        linkAdapter = new LinkAdapter(rootView.getContext(), novelList, this);

        novelLinksTextView.setAdapter(linkAdapter);

        novelLinksTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String novelName = novelList.get(position).getNovelName();
                String novelLink = novelList.get(position).getNovelLink();

                Intent intent = new Intent(getContext(), ChapterActivity.class);
                intent.putExtra(EXTRA_NOVEL_NAME, novelName);
                intent.putExtra(EXTRA_NOVEL_LINK, novelLink);
                startActivity(intent);
            }
        });

        btnWW = (Button) rootView.findViewById(R.id.btn_wuxiaworld);
        btnWW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Updating", Toast.LENGTH_SHORT).show();
                reloadNovelLinks();
            }
        });

        WEBPARSE.parseNovelLinks(WUXIAWORLD, rootView.getContext(), progress);

        return rootView;
    }

    public void removeNovel(int position) {

        String novelName = novelList.get(position).getNovelName();
        String novelLink = novelList.get(position).getNovelLink();

        ContentValues values = new ContentValues();
        values.put(NovelKeys.COLUMN_NOVEL_NAME, novelName);
        values.put(NovelKeys.COLUMN_NOVEL_LINK, novelLink);

        SQLiteDatabase database = mFavoriteNovelDb.getWritableDatabase();

        long idDb = database.insert(NovelKeys.TABLE_NAME, null, values);
        database.close();
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (idDb == -1) {
            Log.e("NovelsFragment", "Failed to insert row for " + novelName);
            return;
        }
        Toast.makeText(rootView.getContext(), "Novel added.", Toast.LENGTH_SHORT).show();
        linkAdapter.remove(novelList.get(position));
        novelList.remove(position);
        context.updateFavoriteFragment();
    }

    public void addNovel(Novel novel) {
        if (!novelList.contains(novel)) {
            Log.v("Adding novel", novel.getNovelName());
            novelList.add(novel);
            Collections.sort(novelList, new NovelComparator());
            linkAdapter.clear();
            linkAdapter.addAll(novelList);
        }
    }

    public Fragment getNovelFragment() {
        return this;
    }


    public void setNovelLinks(List<Novel> novelLinks) {
        progress.setVisibility(View.INVISIBLE);
        novelList = new ArrayList<>(returnDiff(novelLinks, mFavoriteNovelDb.getFavorites(mFavoriteNovelDb.getReadableDatabase())));
        mFavoriteNovelDb.close();
        Collections.sort(novelList, new NovelComparator());
        linkAdapter.clear();
        linkAdapter.addAll(novelList);
    }

    private List<Novel> returnDiff(List<Novel> all, List<Novel> favorites) {
        List<Novel> newList = new ArrayList<>(all);
        for (Novel n1 : all) {
            for (Novel n2 : favorites) {
                if (n1.getNovelName().equals(n2.getNovelName())) {
                    newList.remove(n1);
                    Log.v("Removed----: ", n1.getNovelName());
                    break;
                }
            }
        }
        Log.v("Diff---!", newList.toString());
        return newList;
    }

    public void reloadNovelLinks() {
        linkAdapter.clear();
        WEBPARSE.parseNovelLinks(WUXIAWORLD, getContext(), progress);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_reload).setVisible(false);
    }
}
