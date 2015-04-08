/*******************************************************************************
 * Copyright (C) 2014-2015 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ru.neverdark.phototools.azimuth.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.db.LocationRecord;
import ru.neverdark.phototools.azimuth.utils.ToastException;

public class SaveLocationDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "saveLocationDialog";
    private LocationRecord mData;
    private EditText mLocationName;

    public static SaveLocationDialog getInstance(Context context, LocationRecord data) {
        SaveLocationDialog dialog = new SaveLocationDialog();
        dialog.setContext(context);
        dialog.mData = data;
        return dialog;
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.save_location_dialog, null));
        mLocationName = (EditText) getDialogView().findViewById(R.id.saveLocationDialog_locationName);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
        getAlertDialog().setPositiveButton(R.string.dialog_ok, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new PositiveClickListener());
    }

    @Override
    public void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.saveLocationDialog_title);
        mLocationName.setText(mData.getLocationName());
    }

    public interface OnPositiveClickListener {
        public void onPositiveClick(LocationRecord data);
    }

    private class PositiveClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                String location = mLocationName.getText().toString().trim();
                if (location.length() == 0) {
                    mLocationName.requestFocus();
                    throw new ToastException(R.string.error_empty_location_name);
                }

                OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
                LocationRecord data = new LocationRecord();
                data.setId(mData.getId());
                data.setLocationName(mLocationName.getText().toString());
                data.setCameraZoom(mData.getCameraZoom());
                data.setMapType(mData.getMapType());
                data.setLatitude(mData.getLatitude());
                data.setLongitude(mData.getLongitude());

                if (callback != null) {
                    callback.onPositiveClick(data);
                }

                getDialog().dismiss();
            } catch (ToastException e) {
                e.show(getContext());
            }
        }
    }
}
