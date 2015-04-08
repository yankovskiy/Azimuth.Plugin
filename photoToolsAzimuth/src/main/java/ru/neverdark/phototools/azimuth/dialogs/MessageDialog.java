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

import android.content.Context;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.phototools.azimuth.R;

public class MessageDialog extends UfoDialogFragment {

    public static final String DIALOG_ID = "messageDialog";

    private int mTitleId = 0;
    private int mMessageId = 0;
    private String mTitle = null;
    private String mMessage = null;

    public static MessageDialog getInstance(Context context) {
        MessageDialog dialog = new MessageDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void bindObjects() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new CancelClickListener());
    }

    @Override
    public void createDialog() {
        super.createDialog();
        if (mTitleId != 0) {
            getAlertDialog().setTitle(mTitleId);
            getAlertDialog().setMessage(mMessageId);
        } else {
            getAlertDialog().setTitle(mTitle);
            getAlertDialog().setMessage(mMessage);
        }
    }

    public void setMessages(int titleId, int messageId) {
        mTitleId = titleId;
        mMessageId = messageId;
    }

    public void setMessages(String title, String message) {
        mTitle = title;
        mMessage = message;
    }

}
