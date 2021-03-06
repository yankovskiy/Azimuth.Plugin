/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package ru.neverdark.phototools.azimuth;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;

import ru.neverdark.phototools.azimuth.utils.Common;
import ru.neverdark.phototools.azimuth.utils.Constants;

/**
 * Settings activity
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private Context mContext;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.pref);
        enablePaidPrefs();
        mContext = this;
    }

    private void enablePaidPrefs() {
        if (!Constants.PAID) {
            findPreference(getString(R.string.pref_sunColor)).setOnPreferenceClickListener(new OnlyPaidClickListener());
            findPreference(getString(R.string.pref_sunColor)).setOnPreferenceClickListener(new OnlyPaidClickListener());
            findPreference(getString(R.string.pref_sunsetColor)).setOnPreferenceClickListener(new OnlyPaidClickListener());
            findPreference(getString(R.string.pref_sunriseColor)).setOnPreferenceClickListener(new OnlyPaidClickListener());
            findPreference(getString(R.string.pref_color_title)).setTitle(R.string.settings_colors_title_disabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class OnlyPaidClickListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Common.openPaidMarketUrl(mContext);
            return false;
        }
    }
}