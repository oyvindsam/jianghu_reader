package com.example.samue.jianghureader.layout;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.samue.jianghureader.R;

/**
 * Created by samuelsen on 7/8/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_main);
    }
}
