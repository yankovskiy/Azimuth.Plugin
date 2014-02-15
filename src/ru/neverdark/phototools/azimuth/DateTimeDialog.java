package ru.neverdark.phototools.azimuth;

import java.util.Calendar;

import ru.neverdark.phototools.azimuth.utils.Log;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 *
 */
public class DateTimeDialog extends SherlockDialogFragment {

    /**
     *
     */
    public interface OnConfirmDateTimeListener {
        /**
         * @param calendar
         */
        public void onConfirmDateTimeHandler(Calendar calendar);
    }

    public static DateTimeDialog getInstance(Context context) {
        DateTimeDialog dialog = new DateTimeDialog();
        dialog.mContext = context;
        return dialog;
    }

    private OnConfirmDateTimeListener mCallback;
    private View mView;
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;
    private TabHost mTabHost;
    private Calendar mCalendar;

    private Context mContext;

    /**
     * 
     */
    public final static String DIALOG_TAG = "dateTimeDialog";

    private AlertDialog.Builder mAlertDialog;

    private void bindObjectToResources() {
        mView = View.inflate(getSherlockActivity(), R.layout.date_time_dialog,
                null);
        mTimePicker = (TimePicker) mView.findViewById(R.id.dialog_timePicker);
        mDatePicker = (DatePicker) mView.findViewById(R.id.dialog_datePicker);

        mTabHost = (TabHost) mView.findViewById(android.R.id.tabhost);
    }

    private void buildTabs() {
        mTabHost.setup();

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.tabTimeChoose);
        tabSpec.setIndicator(getString(R.string.mapTimeSelection));
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tabDateChoose);
        tabSpec.setIndicator(getString(R.string.mapDateSelection));
        mTabHost.addTab(tabSpec);
    }

    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(getSherlockActivity());
        mAlertDialog.setView(mView);
        mAlertDialog.setTitle(R.string.mapDateSelection);
    }

    /**
     * 
     */
    @SuppressLint("NewApi")
    public void hideCalendar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mDatePicker.setCalendarViewShown(false);
        }
    }

    private void initDateTime() {
        mTimePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));

        mDatePicker.updateDate(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.enter();

        bindObjectToResources();
        buildTabs();

        hideCalendar();
        initDateTime();

        set24HourMode(Settings.is24HourMode(mContext));

        createDialog();
        setOnClickListener();

        return mAlertDialog.create();
    }

    private void set24HourMode(boolean is24HourMode) {
        mTimePicker.setIs24HourView(is24HourMode);
    }

    /**
     * @param calendar
     */
    public void setCalendar(Calendar calendar) {
        Log.enter();
        mCalendar = calendar;
    }

    /**
     * @param callback
     */
    public void setCallBack(OnConfirmDateTimeListener callback) {
        mCallback = callback;
    }

    private void setOnClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        int hour = mTimePicker.getCurrentHour();
                        int minute = mTimePicker.getCurrentMinute();

                        mCalendar.set(year, month, day, hour, minute);
                        try {
                            mCallback.onConfirmDateTimeHandler(mCalendar);
                        } catch (NullPointerException e) {
                            Log.message("No have callback");
                        }
                    }
                });

        mAlertDialog.setNegativeButton(R.string.dialog_cancel,
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
    }
}
