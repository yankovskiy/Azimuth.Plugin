package ru.neverdark.phototools.azimuth.controller;

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

    private ProgressDialog mDialog;
    private Context mContext;
    private TimeZone mTimeZone;
    private GoogleTimeZone mGoogleTimeZone;
    
    public interface OnCalculationResultHandle {
        public void onGetResult(SunCalculator.CalculationResult calculationResult);
    };
    
    private OnCalculationResultHandle mCallback;
    
    public AsyncCalculator(Context context, OnCalculationResultHandle callback) {
        mContext = context;
        mGoogleTimeZone = new GoogleTimeZone(context);
        mCallback = callback;
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
        int requestStatus = mGoogleTimeZone.requestTimeZone();
        
        if (requestStatus != Constants.STATUS_FAIL) {
            mTimeZone = mGoogleTimeZone.getTimeZone();
            // TODO
        }
        
        return requestStatus;
    }
    @Override
    protected void onPreExecute() {
        createDialog();
        // TODO
    }
    
    @Override
    protected void onPostExecute(Integer result) {
        // TODO
        
        mDialog.dismiss();
    }
    
    private void createDialog() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setTitle(R.string.progress_dialog_title);
        mDialog.setMessage(mContext.getString(R.string.progress_dialog_message));
    }
    
    public void setDate(int year, int month, int day) {
        mGoogleTimeZone.setDate(year, month, day);
    }
    
    public void setLocation(LatLng location) {
        mGoogleTimeZone.setLocation(location);
    }
    
    
}
