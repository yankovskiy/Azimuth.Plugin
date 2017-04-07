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
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import java.lang.reflect.Field;
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
        getAlertDialog().setNeutralButton(R.string.reset, null);
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    public void createDialog() {
        super.createDialog();
        set24HourMode(Settings.is24HourMode(getContext()));
        hideCalendar();
        initDateTime(mCalendar);

        buildTabs();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new ResetDateTimeClickListener());
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

    private void initDateTime(Calendar calendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }
        mDatePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void set24HourMode(boolean is24HourMode) {
        mTimePicker.setIs24HourView(is24HourMode);
    }

    private void hideCalendar() {
        mDatePicker.setCalendarViewShown(false);
    }

    public interface OnPositiveClickListener {
        void onPositiveClick(Calendar calendar);
    }

    private class PositiveClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mTimePicker.clearFocus();
            mDatePicker.clearFocus();

            int year = mDatePicker.getYear();
            int month = mDatePicker.getMonth();
            int day = mDatePicker.getDayOfMonth();
            int hour;
            int minute;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = mTimePicker.getHour();
                minute = mTimePicker.getMinute();
            } else {
                hour = mTimePicker.getCurrentHour();
                minute = mTimePicker.getCurrentMinute();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);

            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClick(calendar);
            }
        }
    }

    private class ResetDateTimeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            fixValues();
            initDateTime(Calendar.getInstance());
        }

        private void fixValues() {
            // bug is not reproducible in APIs 24 and above
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) return;
            try {
                int hour, minute;
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    hour = mTimePicker.getHour();
                    minute = mTimePicker.getMinute();
                } else {
                    hour = mTimePicker.getCurrentHour();
                    minute = mTimePicker.getCurrentMinute();
                }

                Field mDelegateField = mTimePicker.getClass().getDeclaredField("mDelegate");
                mDelegateField.setAccessible(true);
                Class<?> clazz;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    clazz = Class.forName("android.widget.TimePickerClockDelegate");
                } else {
                    clazz = Class.forName("android.widget.TimePickerSpinnerDelegate");
                }
                Field mInitialHourOfDayField = clazz.getDeclaredField("mInitialHourOfDay");
                Field mInitialMinuteField = clazz.getDeclaredField("mInitialMinute");
                mInitialHourOfDayField.setAccessible(true);
                mInitialMinuteField.setAccessible(true);
                mInitialHourOfDayField.setInt(mDelegateField.get(mTimePicker), hour);
                mInitialMinuteField.setInt(mDelegateField.get(mTimePicker), minute);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
