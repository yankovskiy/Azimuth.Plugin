package ru.neverdark.phototools.azimuth;

import java.util.Calendar;

import ru.neverdark.phototools.azimuth.utils.Log;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DateTimeDialog extends SherlockDialogFragment {

    public interface OnConfirmListener {
        public void onConfirmHandler(Calendar calendar);
    }

    private OnConfirmListener mCallback;

    private View mView;
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;
    private TabHost mTabHost;

    public final static String DIALOG_TAG = "dateTimeDialog";

    private AlertDialog.Builder mAlertDialog;

    private void bindObjectToResources() {
        mView = View.inflate(getSherlockActivity(), R.layout.date_time_dialog,
                null);
        mTimePicker = (TimePicker) mView.findViewById(R.id.dialog_timePicker);
        mDatePicker = (DatePicker) mView.findViewById(R.id.dialog_datePicker);
        
        mTabHost = (TabHost) mView.findViewById(android.R.id.tabhost);
    }

    @SuppressLint("NewApi")
    public void hideCalendar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mDatePicker.setCalendarViewShown(false);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bindObjectToResources();
        buildTabs();
        
        hideCalendar();
        
        createDialog();
        setOnClickListener();

        return mAlertDialog.create();
    }

    private void setOnClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // TODO заполнение календаря
                        try {
                            mCallback.onConfirmHandler(null);
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

    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(getSherlockActivity());
        mAlertDialog.setView(mView);
        mAlertDialog.setTitle(R.string.mapDateSelection);
    }

    public void setCallBack(OnConfirmListener callback) {
        mCallback = callback;
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
}
