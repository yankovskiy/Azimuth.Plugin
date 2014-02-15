package ru.neverdark.phototools.azimuth;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
