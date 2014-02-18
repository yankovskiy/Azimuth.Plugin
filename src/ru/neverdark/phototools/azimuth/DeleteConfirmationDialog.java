package ru.neverdark.phototools.azimuth;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Dialog for confirmation delete record action
 */
public class DeleteConfirmationDialog extends SherlockDialogFragment {
    /**
     * Class implements action for "Cancel" dialog button
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
     * The interface for processing the delete record action
     */
    public interface OnDeleteConfirmationListener {
        /**
         * Handler for processing delete record action
         * 
         * @param locationRecord
         *            the selected record for deletion
         */
        public void onDeleteConfirmationHandler(LocationRecord locationRecord);
    }

    /**
     * Class implements action for "Ok" dialog button
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
            if (mCallback != null) {
                mCallback.onDeleteConfirmationHandler(mLocationRecord);
            }
        }
    }

    private Context mContext;
    private AlertDialog.Builder mAlertDialog;
    private LocationRecord mLocationRecord;
    private OnDeleteConfirmationListener mCallback;

    /**
     * Dialog name for fragment manager
     */
    public static final String DIALOG_TAG = "deleteConfirmationDialog";

    /**
     * Creates new dialog
     * 
     * @param context
     *            application context
     * @return dialog object
     */
    public static DeleteConfirmationDialog getInstance(Context context) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        dialog.mContext = context;
        return dialog;
    }

    /**
     * Creates alert dialog
     */
    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setTitle(R.string.deleteConfirmationDialog_title);
        String message = String.format(Locale.US,
                getString(R.string.deleteConfirmationDialog_message),
                mLocationRecord.getLocationName());
        mAlertDialog.setMessage(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        createDialog();
        setClickListener();

        return mAlertDialog.create();
    }

    /**
     * Sets callback object for handling delete record action
     * 
     * @param callback
     *            object for handling delete record action
     */
    public void setCallback(OnDeleteConfirmationListener callback) {
        mCallback = callback;
    }

    /**
     * Sets click listeners for dialog buttons
     */
    private void setClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositiveClickListener());
        mAlertDialog.setNegativeButton(R.string.dialog_cancel,
                new NegativeClickListener());
    }

    /**
     * Sets selected record for deletion
     * 
     * @param locationRecord
     *            selected record for deletion
     */
    public void setLocationRecord(LocationRecord locationRecord) {
        mLocationRecord = locationRecord;
    }
}
