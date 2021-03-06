package com.javierd.nightlive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    public static String dateFormat = "dd/MM/yy";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
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

    /**
     * Returns a color which fits as text color for
     * a given background color
     * @param color The {@link int} the color
     * @return text color
     * */
    public static int getTextColor(int color) {
        // Get existing colors
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // If a < 0.5, the color is light
        // We take into account that human eye favors green color
        double a = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        if(a < 0.5){
            //We need to make the color darker
            red *= 0.25;
            blue *= 0.25;
            green *= 0.25;
        }else{
            red = Math.min(red*4, 255);
            blue = Math.min(blue*4, 255);
            green = Math.min(green*4, 255);
        }

        return Color.argb(255, red, green, blue);
    }

    /**
     * Returns the current state of the permissions needed.
     */
    public static boolean checkLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /*Checks whether the device has or no internet connection*/
    public static boolean isOnline(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
