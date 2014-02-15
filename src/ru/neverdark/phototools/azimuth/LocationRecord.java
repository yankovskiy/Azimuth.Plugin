package ru.neverdark.phototools.azimuth;

/**
 * Класс содержащий одну запись из таблицы locations
 */
public class LocationRecord {
    private String mLocationName;
    private long mId;
    private double mLatitude;
    private double mLongitude;
    private long mLastAccess;
    private int mMapType;
    private float mCameraZoom;

    /**
     * Получает значение поля camera_zoom
     * 
     * @return значение поля camera_zoom
     */
    public float getCameraZoom() {
        return mCameraZoom;
    }

    /**
     * Получает id записи
     * 
     * @return id записи
     */
    public long getId() {
        return mId;
    }

    /**
     * Получает значение поля last_access
     * 
     * @return значение поля last_access
     */
    public long getLastAccess() {
        return mLastAccess;
    }

    /**
     * Получает значение поля latitude
     * 
     * @return значение поля latitude
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Получает значение поля location_name
     * 
     * @return значение поля location_name
     */
    public String getLocationName() {
        return mLocationName;
    }

    /**
     * Получает значение поля longitude
     * 
     * @return значение поля longitude
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Получает значение поля map_type
     * 
     * @return значение поля map_type
     */
    public int getMapType() {
        return mMapType;
    }

    /**
     * Сохраняет значение поля camera_zoom
     * 
     * @param cameraZoom
     *            значение поля camera_zoom
     */
    public void setCameraZoom(float cameraZoom) {
        this.mCameraZoom = cameraZoom;
    }

    /**
     * Сохраняет значение id записи
     * 
     * @param id
     *            записи
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * Сохраняет значение поля last_access
     * 
     * @param lastAccess
     *            значение поля last_access
     */
    public void setLastAccess(long lastAccess) {
        this.mLastAccess = lastAccess;
    }

    /**
     * Сохраняет значение поля latitude
     * 
     * @param latitude
     *            значение поля latitude
     */
    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    /**
     * Сохраняет значение поля location_name
     * 
     * @param locationName
     *            значение поля location_name
     */
    public void setLocationName(String locationName) {
        this.mLocationName = locationName;
    }

    /**
     * Сохраняет значение поля longitude
     * 
     * @param longitude
     *            значение поля longitude
     */
    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    /**
     * Сохраняет значение поля map_type
     * 
     * @param mapType
     *            значение поля map_type
     */
    public void setMapType(int mapType) {
        this.mMapType = mapType;
    }
}
