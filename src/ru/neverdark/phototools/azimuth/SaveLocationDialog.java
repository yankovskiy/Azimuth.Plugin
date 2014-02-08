package ru.neverdark.phototools.azimuth;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 *
 */
public class SaveLocationDialog extends SherlockDialogFragment {
    
    public static final int ACTION_TYPE_NEW = 0;
    public static final int ACTION_TYPE_EDIT = 1;
    
    public class SaveDialogData {
        private int mActionType;
        private LocationRecord mLocationRecord;
        
        public void setActionType(int actionType) {
            mActionType = actionType;
        }
        
        public int getActionType() {
            return mActionType;
        }
        
        public void setLocationRecord(LocationRecord record) {
            mLocationRecord = record;
        }
        
        public LocationRecord getLocationRecord() {
            return mLocationRecord;
        }
    }
    /**
     * 
     */
    public static final String DIALOG_TAG = "saveLocationDialog";
    
    /**
     *
     */
    public interface OnSaveLocationListener {
        /**
         * 
         */
        public void onSaveLocationHandler(SaveDialogData data); 
    }
    
    private OnSaveLocationListener mCallback;
    private SaveDialogData mData;
    
    public void setCallback(OnSaveLocationListener callback) {
        mCallback = callback;
    }
    
    public void setSaveDialogData(SaveDialogData data) {
        mData = data;
    }
}
