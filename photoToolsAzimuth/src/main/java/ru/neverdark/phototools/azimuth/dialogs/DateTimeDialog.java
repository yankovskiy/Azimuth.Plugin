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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import java.util.Calendar;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.utils.Settings;

public class DateTimeDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "dateTimeDialog";
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;
    private TabHost mTabHost;
    private Calendar mCalendar;

    public static DateTimeDialog getInstance(Context context, Calendar calendar) {
        DateTimeDialog dialog = new DateTimeDialog();
        dialog.setContext(context);
        dialog.mCalendar = calendar;
        return dialog;
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.date_time_dialog, null));
        mTimePicker = (TimePicker) getDialogView().findViewById(R.id.dialog_timePicker);
        mDatePicker = (DatePicker) getDialogView().findViewById(R.id.dialog_datePicker);
        mTabHost = (TabHost) getDialogView().findViewById(android.R.id.tabhost);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    public void createDialog() {
        super.createDialog();
        set24HourMode(Settings.is24HourMode(getContext()));
        hideCalendar();
        initDateTime();

        buildTabs();
    }

    private void buildTabs() {
        mTabHost.setup();

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec("time");
        tabSpec.setContent(R.id.dialog_timePicker);
        tabSpec.setIndicator(getString(R.string.mapTimeSelection));
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec("date");
        tabSpec.setContent(R.id.dialog_datePicker);
        tabSpec.setIndicator(getString(R.string.mapDateSelection));
        mTabHost.addTab(tabSpec);
    }

    private void initDateTime() {
        mTimePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));

        mDatePicker.updateDate(
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void set24HourMode(boolean is24HourMode) {
        mTimePicker.setIs24HourView(is24HourMode);
    }

    @SuppressLint("NewApi")
    private void hideCalendar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mDatePicker.setCalendarViewShown(false);
        }
    }

    public interface OnPositiveClickListener {
        public void onPositiveClick(Calendar calendar);
    }

    private class PositiveClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mTimePicker.clearFocus();
            mDatePicker.clearFocus();

            int year = mDatePicker.getYear();
            int month = mDatePicker.getMonth();
            int day = mDatePicker.getDayOfMonth();
            int hour = mTimePicker.getCurrentHour();
            int minute = mTimePicker.getCurrentMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);

            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClick(calendar);
            }
        }
    }
}
