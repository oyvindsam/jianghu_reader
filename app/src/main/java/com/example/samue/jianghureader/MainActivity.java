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

import com.example.samue.jianghureader.data.SimpleFragmentPagerAdapter;
import com.example.samue.jianghureader.data.WebParse;

import java.util.List;

import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;
import com.example.samue.jianghureader.model.Novel;

public class MainActivity extends AppCompatActivity {

    public static final String APPLICATION_ID = "com.example.samue.novelreader";
    public static final String EXTRA_NOVEL_NAME = "com.example.samue.novelreader.NOVEL_NAME";
    public static final String EXTRA_NOVEL_LINK = "com.example.samue.novelreader.NOVEL_LINK";
    public static final String EXTRA_NOVEL_URI = "com.example.samue.novelreader.NOVEL_URI";
    public static final String WUXIAWORLD = "http://www.wuxiaworld.com/";
    private static final int NOVEL_LOADER_ID = 1;
    private static final int FAVORITE_FRAGMENT = 0;
    private static final int NOVELS_FRAGMENT = 1;

    public static WebParse WEBPARSE = new WebParse();
    public NovelsFragment novelsFragment;
    public FavoriteFragment favoriteFragment;
    public SimpleFragmentPagerAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    

    public NovelsFragment getNovelsFragment() {
        return (NovelsFragment) fragmentAdapter.getItem(NOVELS_FRAGMENT);
    }

}
