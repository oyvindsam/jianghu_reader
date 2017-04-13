package com.example.samue.jianghureader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.samue.jianghureader.data.WebParse;

import java.util.List;

import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;

public class MainActivity extends AppCompatActivity {

    public static final String APPLICATION_ID = "com.example.samue.novelreader";
    public static final String EXTRA_NOVEL_NAME = "com.example.samue.novelreader.NOVEL_NAME";
    public static final String EXTRA_NOVEL_LINK = "com.example.samue.novelreader.NOVEL_LINK";
    public static final String WUXIAWORLD = "http://www.wuxiaworld.com/";
    private static final int NOVEL_LOADER_ID = 1;
    public static WebParse WEBPARSE = new WebParse();
    public NovelsFragment novelsFragment;
    public FavoriteFragment favoriteFragment;
    public SimpleFragmentPagerAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_main);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast toast = new Toast(this);
            toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(fragmentAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        getSupportActionBar().setElevation(0);
        tabLayout.setupWithViewPager(viewPager);

    }

    public FavoriteFragment getFavoriteFragment() {
        return (FavoriteFragment) fragmentAdapter.getItem(0);
    }

    public NovelsFragment getNovelsFragment() {
        return (NovelsFragment) fragmentAdapter.getItem(1);
    }

    public void updateNovelsFragment(List<Novel> novelLinks) {
        novelsFragment = (NovelsFragment) fragmentAdapter.getItem(1); //NovelsFragment
        novelsFragment.setNovelLinks(novelLinks);
    }

    public void updateFavoriteFragment() {
        favoriteFragment = (FavoriteFragment) fragmentAdapter.getItem(0); //NovelsFragment
        favoriteFragment.displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                getFavoriteFragment().displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
