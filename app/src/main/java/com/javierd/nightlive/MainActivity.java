package com.javierd.nightlive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.SphericalUtil;
import com.javierd.nightlive.GMapPlace.GMapPlace;
import com.javierd.nightlive.GMapPlace.GMapPlacePreviewAdapter;
import com.javierd.nightlive.RestUtils.Place;
import com.javierd.nightlive.RestUtils.Point;
import com.javierd.nightlive.RestUtils.Points;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends NetworkActivity  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private LocationReceiver mLocationReceiver;
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    //Tracks if the map is centered or not
    private boolean mapCentered = false;

    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mapLoaded;
    private PopupWindow mPopupWindow;

    List<Point> pointList = null;
    List<Circle> circleList = null;
    List<CircleOptions> circleOptionsList = null;

    private RecyclerView.Adapter placeAdapter;
    private Snackbar mNetworkSnackbar;

    // Monitors the state of the connection to the location update service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();

            if (!Utils.checkLocationPermissions(MainActivity.this)) {
                requestPermissions();
                Log.i("ERROR", "NO ENOUGH PERMISSIONS 1");
            } else {
                Log.i("PERFECT", "ENOUGH PERMISSIONS 1");
                mService.requestLocationUpdates();
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location == null) return;

            if(mapLoaded != null){
                //Northeast corner, southwest corner
                LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());

                LatLngBounds mapView = toBounds(userLoc, 400); //400m of radius as the viewport can be moved arround
                //mapLoaded.setLatLngBoundsForCameraTarget(mapView);

                if(!mapCentered){
                    mapCentered = true;
                    moveCamera(mapLoaded, location);
                }
            }

            String userName = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(LoginActivity.USER_NAME, "None");
            String userToken = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(LoginActivity.USER_TOKEN, "None");

            if(Utils.isOnline(MainActivity.this)){
                getUserLocationPoints(location.getLatitude(), location.getLongitude(), userName, userToken);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationReceiver = new LocationReceiver();

        boolean signedInUser = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(LoginActivity.SIGNED_IN_USER, false);
        if(!signedInUser){
            Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mIntent);

            finish();
        }

        if (!Utils.checkLocationPermissions(MainActivity.this)) {
            requestPermissions();
        }

        /*Connect to Google Api to display the map*/
        connectGoogleApiClient();

        /*Check whether user has enabled the GPS on high accuracy*/
        /*TODO*/
        /*Load the map*/
        mMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map_container, mMapFragment)
                .commit();

        mMapFragment.getMapAsync(this);

    }

    protected void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    protected void getUserLocationPoints(double latitude, double longitude, String userName, String userToken){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        Call<Points> call = apiService.getUserLocationMap(latitude, longitude, userName, userToken);
        call.enqueue(new Callback<Points>() {
            @Override
            public void onResponse(@NonNull Call<Points> call, @NonNull Response<Points> response) {
                int statusCode = response.code();
                switch(statusCode){
                    case 400:
                        //Bad request
                        return;
                    case 401:
                        //Unauthorized
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(LoginActivity.SIGNED_IN_USER, false).apply();
                        Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(mIntent);
                        finish();
                        break;
                }

                Points result = response.body();
                if(result == null) return;
                if(mapLoaded == null) return;

                updatePlacesPoints(result.getPoints());
            }

            @Override
            public void onFailure(@NonNull Call<Points> call, @NonNull Throwable t) {
                // Log error here since request failed
                Log.i(getString(R.string.error_receiving_map), String.valueOf(t));
                // Make sure the error is not due to internet connection
                if(Utils.isOnline(MainActivity.this)){
                    Toast.makeText(MainActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void updatePlacesPoints(List<Point> points){
        if(pointList != null) pointList.clear();

        pointList = points;

        if( circleList == null){
            circleList = new ArrayList<>();
        }else{
            for(Circle c: circleList){
                if(c != null){
                    c.remove();
                }
            }
            circleList.clear();
        }

        if( circleOptionsList == null ){
            circleOptionsList = new ArrayList<>();
        }else{
            circleOptionsList.clear();
        }

        for (int i = 0; i < pointList.size(); i++){
            Point mPoint = pointList.get(i);

            CircleOptions mCircleOptions = setMapCircleOptions(mapLoaded,
                    mPoint.getLatitude(),mPoint.getLongitude(), mPoint.getRadius());
            Circle mCircle = mapLoaded.addCircle(mCircleOptions);
            mCircle.setClickable(true);

            circleList.add(mCircle);
            circleOptionsList.add(mCircleOptions);
        }
    }

    private void getPlacePhotosTask(String placeId, final GMapPlace place, final int position) {

        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PhotoTask(500, 500) {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
                    place.changeImage(attributedPhoto.bitmap);
                    placeAdapter.notifyItemChanged(position);
                }else{
                    Bitmap bm = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                            R.drawable.place_image_not_found);
                    place.changeImage(bm);
                    placeAdapter.notifyItemChanged(position);
                }
            }
        }.execute(placeId);
    }

    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected PhotoTask.AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            PhotoTask.AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new PhotoTask.AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }

    protected void setUpDialog(Point point){
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.PlaceDialog);
        View sheetView = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_point_info, null);
        sheetView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));

        List<Place> placesList =  point.getPlaces();
        final List<GMapPlace> gMapsPlacesList = new ArrayList<>();

        RecyclerView placeRecycler = (RecyclerView) sheetView.findViewById(R.id.placeRecyclerView);
        RecyclerView.LayoutManager placeLManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        placeRecycler.setLayoutManager(placeLManager);

        placeAdapter = new GMapPlacePreviewAdapter(gMapsPlacesList);
        placeRecycler.setAdapter(placeAdapter);

        for(int i = 0; i < placesList.size(); i++){
            final String id = placesList.get(i).getId();
            final int position = i;

            Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                final com.google.android.gms.location.places.Place place = places.get(0);

                                String name = String.valueOf(place.getName());
                                int priceLevel = place.getPriceLevel();
                                String price = "Price not available";
                                float rating = place.getRating();
                                LatLng location = place.getLatLng();
                                String address = getResources().getString(R.string.address_not_available);
                                if(place.getAddress() != null){
                                    address = place.getAddress().toString();
                                }

                                String phoneNumber = getResources().getString(R.string.phone_not_available);
                                if(place.getPhoneNumber() != null){
                                    phoneNumber = place.getPhoneNumber().toString();
                                }

                                Uri websiteUri = place.getWebsiteUri();
                                if(priceLevel != -1){
                                    price = String.valueOf(priceLevel)  + "/4";
                                }

                                GMapPlace gMapPlace = new GMapPlace(id, name, price, rating, null, location, address, websiteUri, phoneNumber);

                                getPlacePhotosTask(id, gMapPlace, position);

                                gMapsPlacesList.add(gMapPlace);
                                placeAdapter.notifyItemInserted(position);
                            } else {
                                //TODO Let the user know
                                Log.e("ERROR", "Place not found");
                            }
                            places.release();
                        }
                    });
        }

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    protected void setUpPopupWindow(String text){

        // In case it is already created, just update the text if necessary
        if(mPopupWindow != null){
            TextView textView = (TextView) mPopupWindow.getContentView().findViewById(R.id.textView);
            if(text != textView.getText())
                textView.setText(text);
            return;
        }


        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        assert inflater != null;
        View customView = inflater.inflate(R.layout.popup_window_map,null);

        /*
           contentView : the popup's content
           width : the popup's width
           height : the popup's height
        */
        mPopupWindow = new PopupWindow(
                customView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        mPopupWindow.setElevation(6.0f);
        mPopupWindow.setAnimationStyle(R.style.style_popup_anim);

        // Get a reference for the custom view close button
        TextView textView = (TextView) customView.findViewById(R.id.textView);
        textView.setText(text);

        // Set a click listener for the popup window close button
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window

                if(mapLoaded != null){
                    String userName = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(LoginActivity.USER_NAME, "None");
                    String userToken = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(LoginActivity.USER_TOKEN, "None");

                    LatLng center = mapLoaded.getCameraPosition().target;
                    getUserLocationPoints(center.latitude, center.longitude, userName, userToken);
                }

                mPopupWindow.dismiss();
                mPopupWindow = null;
            }
        });

         /*
           parent : a parent view to get the getWindowToken() token from
           gravity : the gravity which controls the placement of the popup window
           x : the popup's x location offset
           y : the popup's y location offset
        */
        mPopupWindow.showAtLocation(mMapFragment.getView(), Gravity.BOTTOM,0,180);
    }

    /*Map utilities*/
    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    protected Marker setMapMarker(GoogleMap map, Location loc){
        //Return the marker so that we can remove ir later
        if(map == null || loc == null) return null;

        LatLng user = new LatLng(loc.getLatitude(), loc.getLongitude());
        Marker marker = map.addMarker(new MarkerOptions()
                .position(user));

        moveCamera(map, loc);

        return marker;
    }

    protected CircleOptions setMapCircleOptions(GoogleMap map, float latitude, float longitude, int radius){
        //Return the circle so that we can remove ir later
        if(map == null) return null;
        if(radius <= 0 ) return null;

        LatLng centre = new LatLng(latitude, longitude);

        return new CircleOptions()
                .clickable(true)
                .center(centre)
                .radius(radius)
                .strokeColor(ContextCompat.getColor(this, R.color.mapCircleStroke))
                .strokeWidth(4)
                .fillColor(ContextCompat.getColor(this, R.color.mapCircleFill));
    }

    protected void moveCamera(GoogleMap map, Location loc){
        if(map == null || loc == null) return;

        LatLng user = new LatLng(loc.getLatitude(), loc.getLongitude());
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(user)
                .zoom(15)
                .build();
        //15 is just the perfect zoom
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    /*App lifecycle*/
    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if(Utils.checkLocationPermissions(MainActivity.this)) {
            // Bind to the service. If the service is in foreground mode, this signals to the service
            // that since this activity is in the foreground, the service can exit foreground mode.
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LocationReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        if(mPopupWindow != null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        if(mPopupWindow != null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                        Context.BIND_AUTO_CREATE);
                //mService.requestLocationUpdates();
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();

                if(mapLoaded != null){
                    mapLoaded.setMyLocationEnabled(true);
                    mapLoaded.getUiSettings().setMyLocationButtonEnabled(true);
                }

            } else {
                // Permission denied.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapLoaded = googleMap;

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);

        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(false);

        mapLoaded.setMinZoomPreference(14.6f);
        mapLoaded.setMaxZoomPreference(18.0f);

        if (Utils.checkLocationPermissions(MainActivity.this)) {
            googleMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);

        } else {
            requestPermissions();
        }

        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json_clear));
        //googleMap.setBuildingsEnabled (false);
        googleMap.setIndoorEnabled(false);
        //Listener which receives the user movements
        googleMap.setOnCameraMoveStartedListener(this);


        mapLoaded.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {

                int index = circleList.indexOf(circle);
                if(index != -1){
                    setUpDialog(pointList.get(index));
                }
            }
        });

        /*googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(40.3839, -100.9565), 2));*/

    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            // The user gestured on the map.
            setUpPopupWindow(getResources().getString(R.string.search_this_area));
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            // "The user tapped something on the map.
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            // The app moved the camera.
        }
    }

    /*Google API methods*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!Utils.checkLocationPermissions(MainActivity.this)){
            requestPermissions();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onNetworkChange(){
        if(Utils.isOnline(MainActivity.this)){
            Log.i("NETWORK", "Connected");
            if(mNetworkSnackbar != null && mNetworkSnackbar.isShown()){
                mNetworkSnackbar.dismiss();
            }
        }else{
            //TODO Look for better options
            if(mNetworkSnackbar != null && mNetworkSnackbar.isShown()){
                mNetworkSnackbar.dismiss();
            }

            mNetworkSnackbar = Snackbar.make(findViewById(R.id.map_container),
                    R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
            mNetworkSnackbar.show();
            Log.i("NETWORK", "Disconnected");
        }
    }
}