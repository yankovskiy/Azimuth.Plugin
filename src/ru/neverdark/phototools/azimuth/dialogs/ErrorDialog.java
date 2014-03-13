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
package ru.neverdark.phototools.azimuth.dialogs;

import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.R.string;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Dialog for showing error message
 */
public class ErrorDialog extends SherlockDialogFragment {
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
        }
    }

    /**
     * Dialog name for fragment manager
     */
    public static final String DIALOG_TAG = "errorDialog";

    /**
     * Creates new dialog
     * 
     * @param context
     *            application context
     * @return dialog object
     */
    public static ErrorDialog getIntstance(Context context) {
        ErrorDialog dialog = new ErrorDialog();
        dialog.mContext = context;
        return dialog;
    }

    private AlertDialog.Builder mAlertDialog;
    private Context mContext;
    private int mResourceId;

    /**
     * Creates alert dialog
     */
    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setTitle(R.string.errorDialog_title);
        mAlertDialog.setMessage(mResourceId);
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
     * Sets click listener for dialog buttons
     */
    private void setClickListener() {
        mAlertDialog.setPositiveButton(R.string.dialog_ok,
                new PositiveClickListener());
    }

    /**
     * Sets error message for display in the dialog
     * 
     * @param resourceId
     *            resource ID containing the text with error
     */
    public void setErrorMessage(int resourceId) {
        mResourceId = resourceId;
    }
}
