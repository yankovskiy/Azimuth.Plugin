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
     * Получает значение поля location_name
     * 
     * @return значение поля location_name
     */
    public String getLocationName() {
        return mLocationName;
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
     * Получает значение поля latitude
     * 
     * @return значение поля latitude
     */
    public double getLatitude() {
        return mLatitude;
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
     * Получает значение поля longitude
     * 
     * @return значение поля longitude
     */
    public double getLongitude() {
        return mLongitude;
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
     * Получает значение поля last_access
     * 
     * @return значение поля last_access
     */
    public long getLastAccess() {
        return mLastAccess;
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
     * Получает значение поля map_type
     * 
     * @return значение поля map_type
     */
    public int getMapType() {
        return mMapType;
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

    /**
     * Получает значение поля camera_zoom
     * 
     * @return значение поля camera_zoom
     */
    public float getCameraZoom() {
        return mCameraZoom;
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
     * Получает id записи
     * @return id записи
     */
    public long getId() {
        return mId;
    }

    /**
     * Сохраняет значение id записи
     * @param id записи
     */
    public void setId(long id) {
        this.mId = id;
    }
}
