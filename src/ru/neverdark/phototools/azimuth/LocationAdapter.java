package ru.neverdark.phototools.azimuth;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.internal.db;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Адаптер для связки UI и БДs
 */
public class LocationAdapter extends ArrayAdapter<LocationRecord> {

    private class RowHolder {
        private TextView mLocationName;
        private ImageView mLocationChangeButton;
        private ImageView mLocationRemoveButton;
    }
    private List<LocationRecord> mObjects;
    private final int mResource;
    private final Context mContext;

    private LocationsDbAdapter mDbAdapter;

    /**
     * Конструктор
     * 
     * @param context
     *            контекст активити
     * @param resource
     *            id ресурса содержащего разметку для одной записи списка
     */
    public LocationAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mResource = resource;
    }

    /**
     * Закрывает соединиение с базой данных
     */
    public void closeDb() {
        if (mDbAdapter != null) {
            mDbAdapter.close();
            mDbAdapter = null;
        }
    }

    /**
     * Добавляет новое место в базу
     * 
     * @param locationName
     *            название местоположения
     * @param latitude
     *            широта
     * @param longitude
     *            долгота
     * @param mapType
     *            тип карты
     * @param cameraZoom
     *            зум камеры
     */
    public void createLocation(String locationName, double latitude,
            double longitude, int mapType, float cameraZoom) {
        if (mDbAdapter.isOpen()) {
            mDbAdapter.createLocation(locationName, latitude, longitude,
                    mapType, cameraZoom);
            mObjects.clear();
            mObjects = mDbAdapter.fetchAllLocations();
        } else {
            // бросить исключение
        }

        notifyDataSetChanged();
    }

    /**
     * Удаляет выбранную запись из локального списка и из базы данных
     * 
     * @param position
     *            позиция выбранного элемента в списке
     * @return true в случае успешного удаления записи
     */
    public boolean deleteLocation(final int position) {
        LocationRecord record = getItem(position);
        long recordId = record.getId();
        boolean deleteSuccessful = false;

        if (mDbAdapter.isOpen()) {
            remove(record);
            deleteSuccessful = mDbAdapter.deleteLocation(recordId);
        } else {
            // TODO бросить исключение
        }

        notifyDataSetChanged();

        return deleteSuccessful;
    }

    /**
     * Получает Id записи в базе по позиции элемента в списке
     * 
     * @param position
     *            позиция элемента в списке
     * @return id записи в базе
     */
    private long getIdByPosition(final int position) {
        LocationRecord record = getItem(position);
        return record.getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder = null;
        
        // TODO реализовать функционал
        if (row == null) {

        } else {

        }

        return row;
    }

    /**
     * Проверяет существование местоположения с указанным именем
     * 
     * @param locationName
     *            название местоположения для проверки
     * @return true если местоположение существует
     */
    public boolean isLocationExists(String locationName) {
        boolean exist = false;
        if (mDbAdapter.isOpen()) {
            exist = mDbAdapter.isLocationExists(locationName);
        } else {
            // TODO бросить исключение
        }

        return exist;
    }

    /**
     * Загружает данные с базы данных
     */
    public void loadData() {
        if (mDbAdapter.isOpen()) {
            mObjects = mDbAdapter.fetchAllLocations();
        } else {
            // TODO бросить исключение
        }
    }

    /**
     * Открывает соединение с базой данных
     */
    public void openDb() {
        mDbAdapter = new LocationsDbAdapter(mContext);
        mDbAdapter.open();
    }

    /**
     * Обновляет время доступа к записи, передвигая новые наверх списка
     * 
     * @param position
     *            позиция выбранного элемента в списке
     */
    public void updateLastAccessTime(final int position) {
        long recordId = getIdByPosition(position);

        if (mDbAdapter.isOpen()) {
            mDbAdapter.udateLastAccessTime(recordId);
            mObjects.clear();
            mObjects = mDbAdapter.fetchAllLocations();
        } else {
            // TODO бросить исключение
        }

        notifyDataSetChanged();
    }

    /**
     * Изменяет сохраненное место в базе данных
     * 
     * @param position
     *            позиция записи в списке
     * @param locationName
     *            название местоположения
     * @param latitude
     *            широта
     * @param longitude
     *            долгота
     * @param mapType
     *            тип карты
     * @param cameraZoom
     *            зум камеры
     */
    public void updateLocation(final int position, String locationName,
            double latitude, double longitude, int mapType, float cameraZoom) {
        if (mDbAdapter.isOpen()) {
            long recordId = getIdByPosition(position);
            mDbAdapter.updateLocation(recordId, locationName, latitude,
                    longitude, mapType, cameraZoom);
            mObjects.clear();
            mObjects = mDbAdapter.fetchAllLocations();
        } else {
            // TODO бросить исключение
        }

        notifyDataSetChanged();
    }
}
