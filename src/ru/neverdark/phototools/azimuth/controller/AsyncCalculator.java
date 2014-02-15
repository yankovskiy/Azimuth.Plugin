package ru.neverdark.phototools.azimuth.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.android.gms.maps.model.LatLng;

import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.model.GoogleTimeZone;
import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.utils.Constants;
import ru.neverdark.phototools.azimuth.utils.Log;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class AsyncCalculator extends AsyncTask<Void, Void, Integer> {

    public interface OnCalculationResultListener {
        public void onGetResultFail();

        public void onGetResultSuccess(
                SunCalculator.CalculationResult calculationResult);
    }

    private ProgressDialog mDialog;
    private Context mContext;
    private TimeZone mTimeZone;
    private Calendar mCalendar;
    private LatLng mLocaiton;
    SunCalculator.CalculationResult mCalcResult;

    private GoogleTimeZone mGoogleTimeZone;;

    private OnCalculationResultListener mCallback;

    public AsyncCalculator(Context context, OnCalculationResultListener callback) {
        mContext = context;
        mGoogleTimeZone = new GoogleTimeZone(context);
        mCallback = callback;
    }

    private void createDialog() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setTitle(R.string.progress_dialog_title);
        mDialog.setMessage(mContext.getString(R.string.progress_dialog_message));
        mDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        mGoogleTimeZone.setCalendar(mCalendar);
        mGoogleTimeZone.setLocation(mLocaiton);

        int requestStatus = mGoogleTimeZone.requestTimeZone();

        if (requestStatus == Constants.STATUS_SUCCESS) {
            mTimeZone = mGoogleTimeZone.getTimeZone();
            Calendar calendar = Calendar.getInstance(mTimeZone);
            calendar.set(year, month, day, hour, minute);

            SunCalculator sunCalc = new SunCalculator();
            mCalcResult = sunCalc.getPosition(calendar, mLocaiton);
        }

        return requestStatus;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mDialog.dismiss();

        if (result == Constants.STATUS_SUCCESS) {
            mCallback.onGetResultSuccess(mCalcResult);
        } else {
            mCallback.onGetResultFail();
        }
    }

    @Override
    protected void onPreExecute() {
        createDialog();
    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    public void setLocation(LatLng location) {
        mLocaiton = location;
    }

}
