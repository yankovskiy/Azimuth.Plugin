/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ru.neverdark.phototools.azimuth.dialogs;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.utils.Log;

public class ZonePickerDialog extends UfoDialogFragment {
    public static final String DIALOG_TAG = "zonePickerDialog";
    private static final int HOUR = 3600000;
    private static final String KEY_DISPLAYNAME = "name";
    private static final String KEY_GMT = "gmt";
    private static final String KEY_ID = "id";
    private static final String KEY_OFFSET = "offset";
    private static final String XMLTAG_TIMEZONE = "timezone";
    private ListView mListView;

    public static ZonePickerDialog getInstance(Context context) {
        ZonePickerDialog dialog = new ZonePickerDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.time_zone_selection_dialog, null));
        mListView = (ListView) getDialogView().findViewById(R.id.timeZoneSelectionDialog_list);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
        mListView.setOnItemClickListener(new TZListClickListener());
    }

    @Override
    public void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.timeZoneSelectionDialog_title);
        initList();
    }

    private void initList() {
        SimpleAdapter adapter = constructTimeZoneAdapter(getContext(),
                R.layout.time_zone_row);
        mListView.setAdapter(adapter);
    }

    /**
     * Gets time zone list from xml
     *
     * @param context application context
     * @return time zone list
     */
    private List<HashMap<String, Object>> getZones(Context context) {
        final List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        final long date = Calendar.getInstance().getTimeInMillis();

        try {
            XmlResourceParser xrp = context.getResources().getXml(
                    R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return data;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(data, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException e) {
            Log.message("XmlPullParserException exception");
        } catch (IOException e) {
            Log.message("IOException exception");
        }

        return data;
    }

    /**
     * Constructs an adapter with TimeZone list. Sorted by TimeZone in default.
     */
    private SimpleAdapter constructTimeZoneAdapter(Context context, int layoutId) {
        final String[] from = new String[]{KEY_DISPLAYNAME, KEY_GMT};
        final int[] to = new int[]{R.id.timeZone_label_displayName,
                R.id.timeZone_label_gmt};
        final List<HashMap<String, Object>> list = getZones(context);
        final SimpleAdapter adapter = new SimpleAdapter(context, list,
                layoutId, from, to);

        return adapter;
    }

    /**
     * Adds timezone to time zone list
     *
     * @param data        time zone list
     * @param id          time zone id
     * @param displayName time zone name
     * @param date        current date
     */
    private void addItem(List<HashMap<String, Object>> data, String id,
                         String displayName, long date) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);

        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / HOUR);
        name.append(":");

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        data.add(map);
    }

    public interface OnPositiveClickListener {
        public void onPositiveClick(TimeZone tz);
    }

    private class TZListClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String, Object> obj = (HashMap<String, Object>) parent.getItemAtPosition(position);
            TimeZone tz = TimeZone.getTimeZone(obj.get(KEY_ID).toString());
            getDialog().dismiss();

            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClick(tz);
            }
        }
    }
}
