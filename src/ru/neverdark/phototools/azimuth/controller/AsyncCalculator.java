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

/**
 * Class provides an asynchronous computation azimuth
 */
public class AsyncCalculator extends AsyncTask<Void, Void, Integer> {

    /**
     * The interface for calculation handling
     */
    public interface OnCalculationResultListener {
        /**
         * Handler for time zone not detected case
         */
        public void onGetResultFail();

        /**
         * Handler for successfully calculation
         * 
         * @param calculationResult
         *            calculation result data
         */
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

    /**
     * Constructor
     * 
     * @param context
     *            application context
     * @param callback
     *            callback object for processing calculation result
     */
    public AsyncCalculator(Context context, OnCalculationResultListener callback) {
        mContext = context;
        mGoogleTimeZone = new GoogleTimeZone(context);
        mCallback = callback;
        mIsIternetTimeZone = false;
    }

    /**
     * Creates and shows progress dialog
     */
    private void createDialog() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setTitle(R.string.progress_dialog_title);
        mDialog.setMessage(mContext.getString(R.string.progress_dialog_message));
        mDialog.show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Integer doInBackground(Void... params) {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        int requestStatus = Constants.STATUS_FAIL;

        // "Internet time zone" settings enabled
        if (mIsIternetTimeZone) {
            mGoogleTimeZone.setCalendar(mCalendar);
            mGoogleTimeZone.setLocation(mLocaiton);
            // gets time zone from Google servers
            requestStatus = mGoogleTimeZone.requestTimeZone();
            if (requestStatus == Constants.STATUS_SUCCESS) {
                mTimeZone = mGoogleTimeZone.getTimeZone();
            }
        } else {
            // if user have selected time zone
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

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Integer result) {
        mDialog.dismiss();

        if (result == Constants.STATUS_SUCCESS) {
            mCallback.onGetResultSuccess(mCalcResult);
        } else {
            mCallback.onGetResultFail();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        createDialog();
    }

    /**
     * Sets calendar with date for calculation
     * 
     * @param calendar
     *            calendar with date for calculation
     */
    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    /**
     * Notifies AsyncCalculator to use a time zone from the Internet
     * 
     * @param isInternetTimezone
     *            true for use time zone from the Internet
     */
    public void setIsInternetTimeZone(boolean isInternetTimezone) {
        mIsIternetTimeZone = isInternetTimezone;
    }

    /**
     * Sets location for calculation
     * 
     * @param location
     *            location for calculation
     */
    public void setLocation(LatLng location) {
        mLocaiton = location;
    }

    /**
     * Sets time zone for calculation. Must be called after notification
     * AsyncCalculator to don't use a time zone from Internet
     * 
     * @param timeZone
     *            time zone calculation
     */
    public void setTimeZone(TimeZone timeZone) {
        mTimeZone = timeZone;
    }

}
