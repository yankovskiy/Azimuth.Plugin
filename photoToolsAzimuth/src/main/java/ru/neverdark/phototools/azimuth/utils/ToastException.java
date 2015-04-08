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
package ru.neverdark.phototools.azimuth.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastException extends Exception {
    private static final long serialVersionUID = 1L;
    private int mMessageId;
    private String mMessage;

    public ToastException(int messageId) {
        mMessageId = messageId;
    }

    public ToastException(String error) {
        mMessage = error;
    }

    public void show(Context context) {
        if (mMessageId != 0) {
            Toast.makeText(context, mMessageId, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, mMessage, Toast.LENGTH_LONG).show();
        }
    }

}
