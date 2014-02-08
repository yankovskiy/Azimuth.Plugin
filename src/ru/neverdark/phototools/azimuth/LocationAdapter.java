package ru.neverdark.phototools.azimuth;

import java.util.ArrayList;
import java.util.List;

import ru.neverdark.phototools.azimuth.utils.Log;

import android.content.Context;
import android.database.SQLException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Адаптер для связки UI и БД
 */
public class LocationAdapter extends ArrayAdapter<LocationRecord> {
    private static final String EXCEPTION_MESSAGE = "Database is not open";
    
    /**
     * Интерфейс для обработки клика по кнопке "удалить"
     */
    public interface OnClickListener {
        public void onClick();
    }

    private static class RowHolder {
        private TextView mLocationName;
        private ImageView mLocationRemoveButton;
    }

    private OnClickListener mCallback;
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
        this(context, resource, new ArrayList<LocationRecord>());
    }

    /**
     * Устанавливает callback - объект реализующий интерфейс для обработки
     * кликов
     * 
     * @param callback
     *            объект
     */
    public void setCallback(OnClickListener callback) {
        mCallback = callback;
    }

    /**
     * Конструктор
     * 
     * @param context
     *            контекст активити
     * @param resource
     *            id ресурса содержащего разметку для одной записи списка
     * @param objects
     *            список объектов
     */
    private LocationAdapter(Context context, int resource,
            List<LocationRecord> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mObjects = objects;
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
        boolean deleteStatus = false;

        if (mDbAdapter.isOpen()) {
            remove(record);
            deleteStatus = mDbAdapter.deleteLocation(recordId);
        } else {
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();

        return deleteStatus;
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

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mResource, parent, false);
            holder = new RowHolder();
            holder.mLocationRemoveButton = (ImageView) row
                    .findViewById(R.id.locationRow_image_remove);
            holder.mLocationName = (TextView) row
                    .findViewById(R.id.locationRow_label);
            row.setTag(holder);
        } else {
            holder = (RowHolder) row.getTag();
        }

        LocationRecord record = mObjects.get(position);
        holder.mLocationName.setText(record.getLocationName());
        
        setClickListener(holder);

        return row;
    }

    /**
     * Устанавливает обработчик клика по кнопке "удалить"
     * 
     * @param holder
     *            запись - строчка
     */
    private void setClickListener(RowHolder holder) {
        holder.mLocationRemoveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mCallback.onClick();
                } catch (NullPointerException e) {
                    Log.message("Callback not seted");
                }
            }

        });
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
            throw new SQLException(EXCEPTION_MESSAGE);
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
            throw new SQLException(EXCEPTION_MESSAGE);
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
            throw new SQLException(EXCEPTION_MESSAGE);
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
            throw new SQLException(EXCEPTION_MESSAGE);
        }

        notifyDataSetChanged();
    }
}
