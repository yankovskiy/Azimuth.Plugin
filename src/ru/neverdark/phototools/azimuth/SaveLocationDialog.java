package ru.neverdark.phototools.azimuth;

import ru.neverdark.phototools.azimuth.utils.Log;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 *
 */
public class SaveLocationDialog extends SherlockDialogFragment {

    private class NegativeClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    /**
     *
     */
    public interface OnSaveLocationListener {
        /**
         * 
         */
        public void onSaveLocationHandler(SaveDialogData data);
    }

    private class PositiveClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            try {
                mData.getLocationRecord().setLocationName(
                        mEditTextLocationName.getText().toString());
                Log.message("success");
                mCallback.onSaveLocationHandler(mData);
            } catch (NullPointerException e) {
                Log.message("No have callback");
            }
        }
    }
    public static class SaveDialogData {
        private int mActionType;
        private LocationRecord mLocationRecord;

        public int getActionType() {
            return mActionType;
        }

        public LocationRecord getLocationRecord() {
            return mLocationRecord;
        }

        public void setActionType(int actionType) {
            mActionType = actionType;
        }

        public void setLocationRecord(LocationRecord record) {
            mLocationRecord = record;
        }
    }
    public static final int ACTION_TYPE_EDIT = 1;
    public static final int ACTION_TYPE_NEW = 0;

    /**
     * 
     */
    public static final String DIALOG_TAG = "saveLocationDialog";
    public static SaveLocationDialog getInstance(Context context) {
        SaveLocationDialog dialog = new SaveLocationDialog();
        dialog.mContext = context;
        return dialog;
    }

    private AlertDialog.Builder mAlertDialog;

    private OnSaveLocationListener mCallback;

    private Context mContext;

    private SaveDialogData mData;
    private EditText mEditTextLocationName;

    private View mView;

    private void bindObjectToResource() {
        mView = View.inflate(mContext, R.layout.save_location_dialog, null);
        mEditTextLocationName = (EditText) mView
                .findViewById(R.id.saveLocationDialog_locationName);
    }

    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setView(mView);
        mAlertDialog.setTitle(R.string.saveLocationDialog_title);
        mAlertDialog.setMessage(R.string.saveLocationDialog_message);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bindObjectToResource();
        createDialog();
        setOnClickListener();

        if (mData != null && mData.getActionType() == ACTION_TYPE_EDIT) {
            mEditTextLocationName.setText(mData.getLocationRecord()
                    .getLocationName());
        }

        return mAlertDialog.create();
    }

    public void setCallback(OnSaveLocationListener callback) {
        mCallback = callback;
    }

    private void setOnClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositiveClickListener());
        mAlertDialog.setNegativeButton(R.string.dialog_cancel,
                new NegativeClickListener());
    }

    public void setSaveDialogData(SaveDialogData data) {
        mData = data;
    }
}
