package ru.neverdark.phototools.azimuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import ru.neverdark.phototools.azimuth.utils.Log;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Dialog for time zone selection
 */
public class TimeZoneSelectionDialog extends SherlockDialogFragment {

    /**
     * Class implements clicks handler on time zone in the list
     */
    private class ItemClickListener implements OnItemClickListener {
        TimeZoneSelectionDialog mDialog;

        /**
         * Constructor
         * 
         * @param dialog
         *            dialog for closing after time zone selection
         */
        public ItemClickListener(TimeZoneSelectionDialog dialog) {
            mDialog = dialog;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * android.widget.AdapterView.OnItemClickListener#onItemClick(android
         * .widget.AdapterView, android.view.View, int, long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View itemClicked,
                int position, long id) {
            HashMap<String, Object> obj = (HashMap<String, Object>) mListView
                    .getItemAtPosition(position);
            TimeZone tz = TimeZone.getTimeZone(obj.get(KEY_ID).toString());
            mDialog.dismiss();
            if (mDialog.mCallback != null) {
                mDialog.mCallback.onTimeZoneSelectionHandler(tz);
            }
        }

    }

    /**
     * The interface for processing time zone selection
     */
    public interface OnTimeZoneSelectionListener {
        /**
         * Handler for processing time zone selection
         * 
         * @param timeZone
         *            selected time zone
         */
        public void onTimeZoneSelectionHandler(TimeZone timeZone);
    }

    /**
     * Dialog name for fragment manager
     */
    public static final String DIALOG_TAG = "timeZoneSelectionDialog";
    private static final int HOUR = 3600000;
    private static final String KEY_DISPLAYNAME = "name";
    private static final String KEY_GMT = "gmt";
    private static final String KEY_ID = "id";
    private static final String KEY_OFFSET = "offset";
    private static final String XMLTAG_TIMEZONE = "timezone";

    /**
     * Crates new dialog
     * 
     * @param context
     *            application context
     * @return dialog object
     */
    public static TimeZoneSelectionDialog getInstance(Context context) {
        TimeZoneSelectionDialog dialog = new TimeZoneSelectionDialog();
        dialog.mContext = context;
        return dialog;
    }

    private AlertDialog.Builder mAlertDialog;
    private OnTimeZoneSelectionListener mCallback;
    private Context mContext;
    private ListView mListView;
    private View mView;

    /**
     * Adds timezone to time zone list
     * 
     * @param data
     *            time zone list
     * @param id
     *            time zone id
     * @param displayName
     *            time zone name
     * @param date
     *            current date
     */
    private void addItem(List<HashMap<String, Object>> data, String id,
            String displayName, long date) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);

        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / HOUR);
        name.append(":");

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        data.add(map);
    }

    /**
     * Binds class objects to resources
     */
    private void bindObjectToResource() {
        mView = View.inflate(mContext, R.layout.time_zone_selection_dialog,
                null);
        mListView = (ListView) mView
                .findViewById(R.id.timeZoneSelectionDialog_list);
    }

    /**
     * Constructs an adapter with TimeZone list. Sorted by TimeZone in default.
     * 
     * @param sortedByName
     *            use Name for sorting the list.
     */
    private SimpleAdapter constructTimeZoneAdapter(Context context, int layoutId) {
        final String[] from = new String[] { KEY_DISPLAYNAME, KEY_GMT };
        final int[] to = new int[] { R.id.timeZone_label_displayName,
                R.id.timeZone_label_gmt };
        final List<HashMap<String, Object>> list = getZones(context);
        final SimpleAdapter adapter = new SimpleAdapter(context, list,
                layoutId, from, to);

        return adapter;
    }

    /**
     * Creates alert dialog
     */
    private void createDialog() {
        mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog.setView(mView);
        mAlertDialog.setTitle(R.string.timeZoneSelectionDialog_title);
        SimpleAdapter adapter = constructTimeZoneAdapter(getSherlockActivity(),
                R.layout.time_zone_row);
        mListView.setAdapter(adapter);
    }

    /**
     * Gets time zone list from xml
     * 
     * @param context
     *            application context
     * @return time zone list
     */
    private List<HashMap<String, Object>> getZones(Context context) {
        final List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        final long date = Calendar.getInstance().getTimeInMillis();

        try {
            XmlResourceParser xrp = context.getResources().getXml(
                    R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return data;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(data, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException e) {
            Log.message("XmlPullParserException exception");
        } catch (IOException e) {
            Log.message("IOException exception");
        }

        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bindObjectToResource();
        createDialog();
        setOnClickListener();

        return mAlertDialog.create();
    }

    /**
     * Sets callback object for handling time zone selection
     * 
     * @param callback
     *            callback object
     */
    public void setCallback(OnTimeZoneSelectionListener callback) {
        mCallback = callback;
    }

    /**
     * Sets click listeners for time zone list
     */
    private void setOnClickListener() {
        mListView.setOnItemClickListener(new ItemClickListener(this));
    }

}
