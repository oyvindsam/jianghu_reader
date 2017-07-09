package com.example.samue.jianghureader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.samue.jianghureader.data.SimpleFragmentPagerAdapter;

import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_NOVEL_LINK = "com.example.samue.novelreader.NOVEL_LINK";
    public static final String EXTRA_NOVEL_URI = "com.example.samue.novelreader.NOVEL_URI";
    public static final String WUXIAWORLD = "http://www.wuxiaworld.com/";
    private static final int NOVELS_FRAGMENT = 1;

    public SimpleFragmentPagerAdapter mFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mFragmentPagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(mFragmentPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }
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
            case R.id.action_reset:
                getNovelsFragment().restartWebParseLoader();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public NovelsFragment getNovelsFragment() {
        return (NovelsFragment) mFragmentPagerAdapter.getItem(NOVELS_FRAGMENT);
    }

}
