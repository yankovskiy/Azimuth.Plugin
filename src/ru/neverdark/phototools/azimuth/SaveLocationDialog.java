/*******************************************************************************
 * Copyright (C) 2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
 * Dialog for saving locations
 */
public class SaveLocationDialog extends SherlockDialogFragment {

    /**
     * Class implements action for "cancel" dialog button
     */
    private class NegativeClickListener implements OnClickListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * android.content.DialogInterface.OnClickListener#onClick(android.content
         * .DialogInterface, int)
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    /**
     * The interface for processing the saving location information into
     * database
     */
    public interface OnSaveLocationListener {
        /**
         * Handler for processing the saving location information into database
         * 
         * @param data
         *            data for processing and saving
         */
        public void onSaveLocationHandler(SaveDialogData data);
    }

    /**
     * Class implements action for "ok" dialog button
     */
    private class PositiveClickListener implements OnClickListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * android.content.DialogInterface.OnClickListener#onClick(android.content
         * .DialogInterface, int)
         */
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

    /**
     * Class stores data for action
     */
    public static class SaveDialogData {
        private int mActionType;
        private LocationRecord mLocationRecord;

        /**
         * Gets action type
         * 
         * @return action type (ACTION_TYPE_EDIT, ACTION_TYPE_NEW)
         */
        public int getActionType() {
            return mActionType;
        }

        /**
         * Gets location record
         * 
         * @return object contains record from a database
         */
        public LocationRecord getLocationRecord() {
            return mLocationRecord;
        }

        /**
         * Sets action type
         * 
         * @param actionType
         *            action type (ACTION_TYPE_EDIT, ACTION_TYPE_NEW)
         */
        public void setActionType(int actionType) {
            mActionType = actionType;
        }

        /**
         * Sets location record
         * 
         * @param record
         *            object contains record from database
         */
        public void setLocationRecord(LocationRecord record) {
            mLocationRecord = record;
        }
    }

    /**
     * Action type - editing an existing record
     */
    public static final int ACTION_TYPE_EDIT = 1;
    /**
     * Action type - creating a new record
     */
    public static final int ACTION_TYPE_NEW = 0;

    /**
     * Dialog name for fragment manager
     */
    public static final String DIALOG_TAG = "saveLocationDialog";

    /**
     * Creates new dialog
     * 
     * @param context
     * @return dialog object
     */
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

    /**
     * Binds class object to resource
     */
    private void bindObjectToResource() {
        mView = View.inflate(mContext, R.layout.save_location_dialog, null);
        mEditTextLocationName = (EditText) mView
                .findViewById(R.id.saveLocationDialog_locationName);
    }

    /**
     * Creates alert dialog
     */
    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setView(mView);
        mAlertDialog.setTitle(R.string.saveLocationDialog_title);
        mAlertDialog.setMessage(R.string.saveLocationDialog_message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
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

    /**
     * Sets callback object for handling process the saving location information
     * into database
     * 
     * @param callback
     *            object for handling process
     */
    public void setCallback(OnSaveLocationListener callback) {
        mCallback = callback;
    }

    /**
     * Sets click listeners for dialog buttons
     */
    private void setOnClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositiveClickListener());
        mAlertDialog.setNegativeButton(R.string.dialog_cancel,
                new NegativeClickListener());
    }

    /**
     * Sets data for action
     * 
     * @param data
     *            data for action
     */
    public void setSaveDialogData(SaveDialogData data) {
        mData = data;
    }
}
