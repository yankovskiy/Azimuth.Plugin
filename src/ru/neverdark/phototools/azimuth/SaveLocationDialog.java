package ru.neverdark.phototools.azimuth;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 *
 */
public class SaveLocationDialog extends SherlockDialogFragment {
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
        public void onSaveLocationHandler();
    }
}
