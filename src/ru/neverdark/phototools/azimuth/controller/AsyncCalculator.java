package ru.neverdark.phototools.azimuth.controller;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.android.gms.maps.model.LatLng;

import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.model.GoogleTimeZone;
import ru.neverdark.phototools.azimuth.model.SunCalculator;
import ru.neverdark.phototools.azimuth.utils.Constants;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class AsyncCalculator extends AsyncTask<Void, Void, Integer> {

    public interface OnCalculationResultListener {
        public void onGetResultFail();

        public void onGetResultSuccess(
                SunCalculator.CalculationResult calculationResult);
    }

    private SunCalculator.CalculationResult mCalcResult;
    private Calendar mCalendar;
    private OnCalculationResultListener mCallback;
    private Context mContext;
    private ProgressDialog mDialog;
    private GoogleTimeZone mGoogleTimeZone;
    private boolean mIsIternetTimeZone;;
    private LatLng mLocaiton;
    private TimeZone mTimeZone;

    public AsyncCalculator(Context context, OnCalculationResultListener callback) {
        mContext = context;
        mGoogleTimeZone = new GoogleTimeZone(context);
        mCallback = callback;
        mIsIternetTimeZone = false;
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

        int requestStatus = Constants.STATUS_FAIL;

        if (mIsIternetTimeZone) {
            mGoogleTimeZone.setCalendar(mCalendar);
            mGoogleTimeZone.setLocation(mLocaiton);

            requestStatus = mGoogleTimeZone.requestTimeZone();
            if (requestStatus == Constants.STATUS_SUCCESS) {
                mTimeZone = mGoogleTimeZone.getTimeZone();
            }
        } else {
            if (mTimeZone != null) {
                requestStatus = Constants.STATUS_SUCCESS;
            }
        }

        if (requestStatus == Constants.STATUS_SUCCESS) {
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

    public void setIsInternetTimeZone(boolean isInternetTimezone) {
        mIsIternetTimeZone = isInternetTimezone;
    }

    public void setLocation(LatLng location) {
        mLocaiton = location;
    }

    public void setTimeZone(TimeZone timeZone) {
        mTimeZone = timeZone;
    }

}
