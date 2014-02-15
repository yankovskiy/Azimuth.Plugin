package ru.neverdark.phototools.azimuth;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DeleteConfirmationDialog extends SherlockDialogFragment {
    private class NegativeClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    public interface OnDeleteConfirmationListener {
        public void onDeleteConfirmationHandler(LocationRecord locationRecord);
    }

    private class PositiveClickListener implements OnClickListener {
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

    public static final String DIALOG_TAG = "deleteConfirmationDialog";

    public static DeleteConfirmationDialog getInstance(Context context) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        dialog.mContext = context;
        return dialog;
    }

    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setTitle(R.string.deleteConfirmationDialog_title);
        String message = String.format(Locale.US,
                getString(R.string.deleteConfirmationDialog_message),
                mLocationRecord.getLocationName());
        mAlertDialog.setMessage(message);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        createDialog();
        setClickListener();

        return mAlertDialog.create();
    }

    public void setCallback(OnDeleteConfirmationListener callback) {
        mCallback = callback;
    }

    private void setClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositiveClickListener());
        mAlertDialog.setNegativeButton(R.string.dialog_cancel,
                new NegativeClickListener());
    }

    public void setLocationRecord(LocationRecord locationRecord) {
        mLocationRecord = locationRecord;
    }
}
