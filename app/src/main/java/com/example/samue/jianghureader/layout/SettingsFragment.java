package com.example.samue.jianghureader.layout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.samue.jianghureader.R;
import com.example.samue.jianghureader.ReadingActivity;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;

/**
 * Created by samuelsen on 7/8/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    Preference mTextSizePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_main);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mTextSizePreference = findPreference(getString(R.string.pref_text_size_key));
        mTextSizePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Set text size");
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_number_picker, (ViewGroup) getView(), false);
                final NumberPicker numberPickerInt = (NumberPicker) viewInflated.findViewById(R.id.numberPickerInteger);
                final NumberPicker numberPickerDeci = (NumberPicker) viewInflated.findViewById(R.id.numberPickerDecimal);
                float nPDeciValueTemp = Float.valueOf(sharedPreferences.getString(getString(R.string.pref_text_size_key), getString(R.string.pref_text_size_default)));
                int nPIntValue = (int) nPDeciValueTemp;
                Log.v("TEXT SIZE", "" + nPDeciValueTemp);
                int nPDeciValue = (int) Math.round((nPDeciValueTemp - nPIntValue)*10.0);
                //int nPDeciValue = (int) sharedPreferences.getFloat("@string/pref_text_size_key", getResources().getDimension(R.dimen.pref_text_size_default));
                Log.v("TEXT SIZE", nPIntValue + "." + nPDeciValue);
                numberPickerInt.setMaxValue(25);
                numberPickerInt.setMinValue(5);
                numberPickerInt.setWrapSelectorWheel(false);
                numberPickerInt.setValue(nPIntValue);

                numberPickerDeci.setMaxValue(9);
                numberPickerDeci.setMinValue(0);
                numberPickerDeci.setWrapSelectorWheel(false);
                numberPickerDeci.setValue(nPDeciValue);

                builder.setView(viewInflated);

                builder.setPositiveButton("Set text size", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float textSizeInt = numberPickerInt.getValue();
                        float textSizeDeci = (float) (numberPickerDeci.getValue()/10.0);
                        float textSize = textSizeInt + textSizeDeci;
                        Log.v("TEXT___SIZE", textSizeInt + " . " + textSizeDeci + " == " + textSize);

                        String textSizeString = String.valueOf(textSize);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.pref_text_size_key), textSizeString);
                        editor.apply();
                        Toast.makeText(getContext(), "Text size set to " + textSizeString, Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton(R.string.set_last_novel_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });
    }
}
