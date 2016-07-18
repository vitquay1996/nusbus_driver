package sg.edu.nus.nusbus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by quang on 14/7/16.
 */
public class UpdateService extends IntentService {

    private final String LOG_TAG = UpdateService.class.getSimpleName();
    public static final String LATITUDE_QUERY_EXTRA = "lat";
    public static final String LONGITUDE_QUERY_EXTRA = "longitude";
    public static final String STATUS_QUERY_EXTRA = "status";
    public UpdateService() {
        super("UpdateService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "UpdateService started");
        // get latitude
        double latitudeQuery = intent.getDoubleExtra(LATITUDE_QUERY_EXTRA, 0.00);
        String latitude = String.valueOf(latitudeQuery);

        // get longitude
        double longitudeQuery = intent.getDoubleExtra(LONGITUDE_QUERY_EXTRA, 0.00);
        String longitude = String.valueOf(longitudeQuery);

        // get status
        Boolean status = intent.getBooleanExtra(STATUS_QUERY_EXTRA, false);
        Log.d(LOG_TAG, "status is " + status);
        String statusString;
        if (status) {
            statusString = "1";
        }
        else {
            statusString = "0";
        }

        // get device unique ID
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        String uniqueID = tm.getDeviceId();

        //get bus name
        String bus = Utility.getPreferredBus(this);


        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            final String QUERY_URL = "http://118.70.170.167:8080/quang/nusbus/update.php?";
            final String QUERY_PHONEID = "phoneId";
            final String QUERY_BUS = "bus";
            final String QUERY_LATITUDE = "lat";
            final String QUERY_LONGITUDE = "long";
            final String QUERY_STATUS = "status";
            final String QUERY_CODE = "code";

            Uri builtUri = Uri.parse(QUERY_URL).buildUpon()
                    .appendQueryParameter(QUERY_PHONEID, uniqueID)
                    .appendQueryParameter(QUERY_BUS, bus)
                    .appendQueryParameter(QUERY_LATITUDE, latitude)
                    .appendQueryParameter(QUERY_LONGITUDE, longitude)
                    .appendQueryParameter(QUERY_STATUS, statusString)
                    .appendQueryParameter(QUERY_CODE, Code.getCode())
                    .build();

            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, "The URL is " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            String receivedString = buffer.toString();
            Log.d(LOG_TAG, "Response from server is " + receivedString);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
