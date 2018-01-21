package com.javierd.nightlive;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    public static String dateFormat = "dd/MM/yy";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    /**
     *  Returns a date formatted as a string
     *  @param milis The {@link long} milliseconds of the date
     *  @param format The {@link String} format of the string date
     *  */
    public static String milisToDate(long milis, String format){
        //Convert milliseconds into a date
        /*Check the use of  Locale.getDefault() and what does it change*/
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(new Date(milis));
    }

    /**
     * Returns the negative of a given color
     * @param color The {@link int} the color
     * */
    public static int getNegativeColor(int color) {
        // Get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // Find compliments
        red = 255 - red;
        blue = 255 - blue;
        green = 255 - green;

        return Color.argb(alpha, red, green, blue);
    }

}
