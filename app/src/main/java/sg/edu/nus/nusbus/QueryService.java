package sg.edu.nus.nusbus;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by quang on 11/7/16.
 */
public class QueryService extends IntentService {
    private final String LOG_TAG = QueryService.class.getSimpleName();

    public QueryService() {
        super("QueryService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "QueryService started");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String locationJsonStr = null;

        try {
            final String QUERY_URL = "http://118.70.170.167:8080/quang/nusbus/query.php";

            URL url = new URL(QUERY_URL);
            Log.d(LOG_TAG, "Query URL is " + url.toString());

            // Create the request to server, and open the connection
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
            locationJsonStr = buffer.toString();
            Log.d(LOG_TAG, "The json string is " + locationJsonStr);

            JSONArray busArray = new JSONArray(locationJsonStr);
            //loop through the array
            MapsActivity.handler.post(new Runnable() {
                @Override
                public void run() {
                    //clear all markers
                    MapsActivity.mMap.clear();
                }
            });
            for (int i = 0; i<busArray.length(); i++) {
                JSONObject bus = busArray.getJSONObject(i);
                String busName = bus.getString("bus");
                Double latitude = bus.getDouble("lat");
                Double longitude = bus.getDouble("longitude");
                Utility.getMarker(busName, latitude, longitude);
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

    }
}
