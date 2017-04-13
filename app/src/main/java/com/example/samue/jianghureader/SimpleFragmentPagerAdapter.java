package com.example.samue.jianghureader;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.samue.jianghureader.layout.FavoriteFragment;
import com.example.samue.jianghureader.layout.NovelsFragment;

/**
 * Created by samue on 12.04.2017.
 */

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    FavoriteFragment favoriteFragment;
    NovelsFragment novelsFragment;

    public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (favoriteFragment != null) {
                    return favoriteFragment;
                } else {
                    favoriteFragment = new FavoriteFragment();
                    return favoriteFragment;
                }
            case 1:
                if (novelsFragment != null) {
                    return novelsFragment;
                } else {
                    novelsFragment = new NovelsFragment();
                    return novelsFragment;
                }
            default:
                return null;
        }
    }




    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.favorites);
            case 1:
                return mContext.getString(R.string.novels);
            default:
                return null;
        }
    }
}
