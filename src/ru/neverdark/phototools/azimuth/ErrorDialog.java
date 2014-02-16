package ru.neverdark.phototools.azimuth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorDialog extends SherlockDialogFragment {
    private class PositeiveClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    public static final String DIALOG_TAG = "errorDialog";

    public static ErrorDialog getIntstance(Context context) {
        ErrorDialog dialog = new ErrorDialog();
        dialog.mContext = context;
        return dialog;
    }
    private AlertDialog.Builder mAlertDialog;
    private Context mContext;
    private int mResourceId;

    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setTitle(R.string.errorDialog_title);
        mAlertDialog.setMessage(mResourceId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        createDialog();
        setClickListener();

        return mAlertDialog.create();
    }

    private void setClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositeiveClickListener());
    }

    public void setErrorMessage(int resourceId) {
        mResourceId = resourceId;
    }
}
