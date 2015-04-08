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
package ru.neverdark.phototools.azimuth.db;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.neverdark.phototools.azimuth.R;
import ru.neverdark.phototools.azimuth.utils.Log;

public class LocationsAdapter extends ArrayAdapter<LocationRecord> {
    private final List<LocationRecord> mObjects;
    private final Context mContext;
    private final int mResource;
    private final LocationsDbAdapter mDb;

    public LocationsAdapter(Context context) {
        this(context, R.layout.location_row, new ArrayList<LocationRecord>());
    }

    private LocationsAdapter(Context context, int resource, List<LocationRecord> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
        mDb = new LocationsDbAdapter(mContext);
        mDb.open();
        mDb.fetchAllLocations(mObjects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.enter();
        View row = convertView;
        RowHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.location_name = (TextView) row.findViewById(R.id.location_name);
            holder.location_coordinates = (TextView) row.findViewById(R.id.location_coordinates);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        LocationRecord record = getItem(position);
        String coord = String.format(Locale.US, "%f, %f", record.getLatitude(), record.getLongitude());
        holder.location_coordinates.setText(coord);
        holder.location_name.setText(record.getLocationName());

        return row;
    }

    @Override
    public long getItemId(int position) {
        return mObjects.get(position).getId();
    }

    public void openDb() {
        if (!mDb.isOpen()) {
            mDb.open();
        }
    }

    public void closeDb() {
        if (mDb.isOpen()) {
            mDb.close();
        }
    }

    public void removeItem(LocationRecord record) {
        Log.enter();
        mDb.deleteLocation(record.getId());
        remove(record);
        notifyDataSetChanged();
    }

    public void addItem(LocationRecord record) {
        Log.enter();
        long id = mDb.createLocation(record);
        record.setId(id);
        insert(record, 0);
        notifyDataSetChanged();
    }

    public void updateItem(LocationRecord record) {
        Log.enter();
        mDb.updateLocation(record);
        moveItemToFirstPosition(record);
        notifyDataSetChanged();
    }

    private void moveItemToFirstPosition(LocationRecord item) {
        for (int i = mObjects.size() - 1; i >= 0; i--) {
            if (mObjects.get(i).getId() == item.getId()) {
                mObjects.remove(i);
                break;
            }
        }
        insert(item, 0);
    }

    public boolean isLocationExists(LocationRecord data) {
        for (int i = mObjects.size() - 1; i >= 0; i--) {
            if (mObjects.get(i).getId() == data.getId()) {
                return true;
            }
        }

        return false;
    }

    public void updateAccessTime(LocationRecord record) {
        Log.enter();
        mDb.udateLastAccessTime(record.getId());
        moveItemToFirstPosition(record);
        notifyDataSetChanged();
    }

    private static class RowHolder {
        private TextView location_name;
        private TextView location_coordinates;
    }

}
