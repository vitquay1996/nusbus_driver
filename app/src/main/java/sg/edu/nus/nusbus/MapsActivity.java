package sg.edu.nus.nusbus;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Marker userMarker;
    public static Handler handler;
    public static GoogleMap mMap;
    private final int MY_PERMISSION_REQUEST_LOCATION = 100;
    private final int REQUEST_CHECK_SETTINGS = 0x1;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private final String LOG_TAG = MapsActivity.class.getSimpleName();
    private LatLng currentLocation;
    private String bus;
    private Boolean status;
    String uniqueID;
    LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Get preference from settings
        bus = Utility.getPreferredBus(this);

        status = Utility.getPreferredStatus(this);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        handler = new Handler();

        // Prevent phone from sleeping
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //stop receiving location
        mGoogleApiClient.disconnect();
        //change status of bus on server
        Intent lastIntent = new Intent(this, UpdateService.class);
        lastIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, 0.00);
        lastIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, 0.00);
        lastIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, false);
        Log.d(LOG_TAG, "Starting service to upload location to server");
        startService(lastIntent);
        super.onStop();
    }

    @Override
    protected void onResume() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        super.onResume();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_LOCATION);
        }
        // Add a marker in Sydney and move the camera
//        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//        mMap.addMarker(new MarkerOptions().position(currentLocation).title("This is me"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // Getting request for location update
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // get current location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        // check if settings satisfy
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d(LOG_TAG, "All location settings are satisfied");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.d(LOG_TAG, "Opening dialog to check settings");
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } finally {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.d(LOG_TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                        break;
                }
            }
        });

        // Get mLastLocation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                // Move camera to current location
                currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
            }
        }

        // Receiving update location
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        // Start service to update other buses' location
        Intent intent = new Intent(this, QueryService.class);
        Log.d(LOG_TAG, "Starting service to update buses");
        startService(intent);

        // Start service to update our location to server
        if (mLastLocation != null) {
            if (status) {
                Intent updateIntent = new Intent(this, UpdateService.class);
                updateIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, mLastLocation.getLatitude());
                updateIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, mLastLocation.getLongitude());
                updateIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, status);
                Log.d(LOG_TAG, "Starting service to upload location to server");
                startService(updateIntent);
            } else {
                //change status of bus on server
                Intent lastIntent = new Intent(this, UpdateService.class);
                lastIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, 0.00);
                lastIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, 0.00);
                lastIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, false);
                Log.d(LOG_TAG, "Starting service to upload location to server");
                startService(lastIntent);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    // Receiving update location
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(LOG_TAG, "performing functions");
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if (mLastLocation != null) {
                            // Move camera to current location
                            currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    }
                    // Start service to update other buses' location
                    Intent intent = new Intent(this, QueryService.class);
                    Log.d(LOG_TAG, "Starting service to update buses");
                    startService(intent);

                    // Start service to update our location to server
                    if (mLastLocation != null) {
                        if (status) {
                            Intent updateIntent = new Intent(this, UpdateService.class);
                            updateIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, mLastLocation.getLatitude());
                            updateIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, mLastLocation.getLongitude());
                            updateIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, status);
                            Log.d(LOG_TAG, "Starting service to upload location to server");
                            startService(updateIntent);
                        } else {
                            //change status of bus on server
                            Intent lastIntent = new Intent(this, UpdateService.class);
                            lastIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, 0.00);
                            lastIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, 0.00);
                            lastIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, false);
                            Log.d(LOG_TAG, "Starting service to upload location to server");
                            startService(lastIntent);
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location changed");

        status = Utility.getPreferredStatus(this);

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
//        userMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("This is me"mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));));

        // Start service to update other buses' location
        Intent intent = new Intent(this, QueryService.class);
        Log.d(LOG_TAG, "Starting service to update buses");
        startService(intent);

        // Start service to update our location to server
        if (status) {
            Intent updateIntent = new Intent(this, UpdateService.class);
            updateIntent.putExtra(UpdateService.LATITUDE_QUERY_EXTRA, location.getLatitude());
            updateIntent.putExtra(UpdateService.LONGITUDE_QUERY_EXTRA, location.getLongitude());
            updateIntent.putExtra(UpdateService.STATUS_QUERY_EXTRA, status);
            Log.d(LOG_TAG, "Starting service to upload location to server");
            startService(updateIntent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
