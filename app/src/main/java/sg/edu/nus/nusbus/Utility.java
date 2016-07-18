package sg.edu.nus.nusbus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by quang on 11/7/16.
 */
public class Utility {
    public static void getMarker(String bus, double lat, double longitude) {
        final String LOG_TAG = Utility.class.getSimpleName();
        final double LAT = lat;
        final double LONGITUDE = longitude;
        final String NAME = bus;
        MapsActivity.handler.post(new Runnable() {
            @Override
            public void run() {
                LatLng currentLocation = new LatLng(LAT, LONGITUDE);
                if (NAME.equals("a2")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.a2));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("a1")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.a1));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("b")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.b));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("c")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.c));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("d1")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.d1));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("d2")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.d2));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("bt")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.bt));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("us")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.us));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }
                else if (NAME.equals("ul")) {
                    MarkerOptions marker = new MarkerOptions().position(currentLocation).title(NAME).icon(BitmapDescriptorFactory.fromResource(R.drawable.ul));
                    Marker mk = MapsActivity.mMap.addMarker(marker);
                }

            }
        });

    }

    public static String getPreferredBus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_bus_key),
                context.getString(R.string.pref_bus_default));
    }

    public static Boolean getPreferredStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_status_key),false);
    }
}
