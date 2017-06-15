package com.example.nhat.mapexample.samples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhat.mapexample.R;
import com.example.nhat.mapexample.UseCaseHandler;
import com.example.nhat.mapexample.helpers.GooglePlayServiceHelper;
import com.example.nhat.mapexample.helpers.Injection;
import com.example.nhat.mapexample.others.SearchActivity;
import com.example.nhat.mapexample.samples.domain.model.Sample;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.support.design.widget.Snackbar.make;
import static com.example.nhat.mapexample.R.id.map;
import static com.google.common.base.Preconditions.checkNotNull;

public class SamplesActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SamplesContract.View {

    public static final int SEARCH_REQUEST_CODE = 888;

    public static final String ACTION_VIEW_SAMPLE = "view_sample";
    public static final String ACTION_CREATE_SAMPLE = "create_sample";
    public static final int INVALID_SAMPLE_ID = -1;

    private static final String LOCATION_KEY = "location_key";
    public static final String SAMPLE_ID_KEY = "sample_id_key";

    private final int SAMPLE_ID_RANGE = 999999;

    private final float DEFAULT_ZOOM = 17;

    public static final String UPDATED_ADDRESS_ACTION = "updated_address_action";

    private SamplesContract.Presenter mPresenter;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Map<String, Marker> mSampleIdMarkerMap;
    private float mCurrentZoom;

    private View mMapRoot;
    private EditText mSearchEditTxt;
    /*private View mAddSampleBtn;
    private View mSearchSampleBtn;*/
    private Snackbar mSnackbar;
    private TextView mInputAction;
    private View mInputActionBar;
    private FloatingActionButton fab;

    private List<Sample> mSamples;
    private Marker mCurrentMarker;

    private Random random;
    private String mSampleId;
    private String mAddedSampleId;
    private boolean inSearchMode = false;

    private InputMethodManager mInputMethodManager;

    private final BroadcastReceiver mGeocodingAddressReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mPresenter.loadSamples(false);
        }
    };

    private IntentFilter mGeocodingAddressFilter;

    private View.OnClickListener mControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean oldMode = inSearchMode;

            switch (v.getId()) {
                case R.id.search_visible_btn :
                    inSearchMode = true;
                    break;

                case R.id.add_visible_btn:
                    inSearchMode = false;
                    break;
                default: return;
            }

            if(mInputActionBar.getVisibility() != View.VISIBLE) {
                mInputActionBar.setVisibility(View.VISIBLE);
            } else {
                if(oldMode == inSearchMode) {
                    mInputActionBar.setVisibility(View.INVISIBLE);
                    return;
                }
            }
            if(inSearchMode) {
                mSearchEditTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
                mInputAction.setText("Tìm");
            } else {
                mSearchEditTxt.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                mInputAction.setText("Thêm");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (GooglePlayServiceHelper.isGooglePlayServicesAvailable(this)) {
            mSearchEditTxt = (EditText) findViewById(R.id.input_editText);

            fab = (FloatingActionButton) findViewById(R.id.currentLocationBtn);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAddSampleActive(false);
                    startLocationUpdates();
                }
            });

            mMapRoot = findViewById(R.id.map_root);


            mInputActionBar = findViewById(R.id.input_action_bar);
            mInputAction = (TextView) findViewById(R.id.input_action);

            mInputAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inSearchMode) {
                        String query = mSearchEditTxt.getText().toString().trim();
                        try {
                            Long.parseLong(query);
                        } catch (NumberFormatException e) {
                            showAdvanceSearchSuggestion(query);
                            return ;
                        }
                        if (!TextUtils.isEmpty(query)) {
                            mPresenter.searchSample(query, SamplesSearchType.ID);
                        }
                    } else {
                        storeSample();
                    }

                    View view = getCurrentFocus();
                    if (view != null) {
                        view.clearFocus();
                        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    mSearchEditTxt.setText("");
                }
            });

            findViewById(R.id.search_visible_btn).setOnClickListener(mControlListener);
            findViewById(R.id.add_visible_btn).setOnClickListener(mControlListener);

            mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            Intent intent = getIntent();
            if (intent != null) {
                mSampleId = intent.getStringExtra(SAMPLE_ID_KEY);
            }

            if (savedInstanceState != null) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (mGoogleApiClient == null) {
                // Create an instance of GoogleAPIClient.
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(30000);
                mLocationRequest.setFastestInterval(20000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

                // **************************
                builder.setAlwaysShow(true); // this is the key ingredient
                // **************************

                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                        .checkLocationSettings(mGoogleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates state = result
                                .getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location
                                // requests here.
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be
                                // fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling
                                    // startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(SamplesActivity.this, 1000);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have
                                // no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                });
            }

            mGeocodingAddressFilter = new IntentFilter(UPDATED_ADDRESS_ACTION);

            mPresenter = new SamplesPresenter(this,
                    Injection.provideGetSamples(getApplicationContext()),
                    Injection.provideGetSample(getApplicationContext()),
                    Injection.provideAddSample(getApplicationContext()),
                    Injection.provideDeleteSample(getApplicationContext()),
                    Injection.provideEditSample(getApplicationContext()),
                    Injection.provideSearchSamples(getApplicationContext()),
                    UseCaseHandler.getInstance());

            random = new Random();
            mSampleIdMarkerMap = new HashMap<>();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(map);
            mMapFragment.getMapAsync(this);
        }
    }

    private void storeSample() {
        // Store current location to database
        if (mCurrentLocation != null) {
            final Sample sample = buildSample();
            if (sample == null) {
                return;
            }
            mPresenter.addSample(sample);
        }
    }

    private Sample buildSample() {
        String name = mSearchEditTxt.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mSearchEditTxt.setError("Tên không được rỗng");
            return null;
        }

        Sample sample = new Sample();
        sample.setSampleId(String.valueOf(random.nextInt(SAMPLE_ID_RANGE)));
        sample.setName(name);
        sample.setResult(getResources().getString(R.string.sample_default_result_text));
        sample.setAddress("");
        sample.setLatitude(mCurrentLocation.getLatitude());
        sample.setLongitude(mCurrentLocation.getLongitude());
        sample.setTime(mCurrentLocation.getTime());
        return sample;
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
        if (permissionGranted() && mGoogleApiClient.isConnected()) {
            mCurrentZoom = DEFAULT_ZOOM;
            mMap = googleMap;
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            mMap.clear();

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (TextUtils.isEmpty(mSampleId)) {
                        mCurrentZoom = mMap.getCameraPosition().zoom;
                        mCurrentLocation.setLatitude(latLng.latitude);
                        mCurrentLocation.setLongitude(latLng.longitude);
                        showCurrentLocationMarker(true, true);
                    }
                }
            });

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(final Marker marker) {
                    if(marker.getTag() != null) {
                        int index = (int) marker.getTag();
                        Sample sample = mSamples.get(index);
                        showSampleDetailUI(sample);
                    }
                }
            });

            if (TextUtils.isEmpty(mSampleId)) {
                setAddSampleActive(false);
                startLocationUpdates();
            }
            mPresenter.start();
        }
    }

    private void showSampleDetailUI(Sample sample) {
        SampleDetailBottomSheetFragment sampleDetailBottomSheetFragment = SampleDetailBottomSheetFragment.newInstance(sample);
        sampleDetailBottomSheetFragment.setActiveLocationButton(false);
        sampleDetailBottomSheetFragment.setCallback(new SampleDetailBottomSheetFragment.Callback() {
            @Override
            public void onDeleteSample(String sampleId) {
                showDeleteSampleCommitDialog(sampleId);
            }

            @Override
            public void onEditSample(Sample sample) {
                mPresenter.editSample(sample);
            }

            @Override
            public void onShowSampleDetails(Sample sample) {

            }
        });
        sampleDetailBottomSheetFragment.show(getSupportFragmentManager(), "sample_detail_tag");
    }

    private void showDeleteSampleCommitDialog(final String sampleId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn xoá mẫu thử này không ?")
                .setPositiveButton("Vâng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.deleteSample(sampleId);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mGeocodingAddressReceiver, mGeocodingAddressFilter);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGeocodingAddressReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SEARCH_REQUEST_CODE :
                if(resultCode == RESULT_OK){
                    String sampleId = data.getStringExtra(SearchActivity.SAMPLE_ID_RESULT_KEY);
                    Toast.makeText(this, sampleId, Toast.LENGTH_SHORT).show();
                    moveCameraTo(mSampleIdMarkerMap.get(sampleId), false, true);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(outState);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 888: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mMapFragment.getMapAsync(this);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean permissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                    , android.Manifest.permission.ACCESS_COARSE_LOCATION}, 888);
            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();

        /*if (!TextUtils.isEmpty(mSampleId) && mCurrentMarker != null && mCurrentMarker.isVisible()) {
            mCurrentMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
            mSampleId = "";
        }*/
        mCurrentLocation = location;

        showCurrentLocationMarker(false, true);
    }

    private void showCurrentLocationMarker(final boolean animate, final boolean shouldShowInfoWindow) {
        if (mMap != null) {
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
            }
            if (mCurrentLocation != null) {
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                if (mCurrentMarker == null) {
                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title("Vị trí hiện tại")
                            .position(latLng)
                    );
                } else {
                    mCurrentMarker.setPosition(latLng);
                }
                moveCameraTo(mCurrentMarker, animate, shouldShowInfoWindow);
            }
        }
    }

    protected void startLocationUpdates() {
        if (permissionGranted()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void setPresenter(@NonNull SamplesContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void showSamples(List<Sample> samples) {
        mSamples = samples;
        showSamplesMarkers();
        showCurrentLocationMarker(false, true);
    }

    private void showSamplesMarkers() {
        if (!mSampleIdMarkerMap.isEmpty()) {
            mMap.clear();
            mSampleIdMarkerMap.clear();
            mCurrentMarker = null;
        }

        if (mSamples != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy hh:mm:ss a");
            for (int i = 0; i < mSamples.size(); i++) {
                Sample sample = mSamples.get(i);

                final Marker marker = mMap.addMarker(markerOptions
                        .anchor(0.0f, 1.0f)
                        .position(new LatLng(sample.getLatitude(), sample.getLongitude()))
                        .snippet("Mẫu thử Id : " + sample.getSampleId() + "\n"
                                + "Tên : " + sample.getName() + "\n"
                                + "Thời gian : " + format.format(new Date(sample.getTime()))));
                marker.setTag(i);

                if (!TextUtils.isEmpty(mAddedSampleId) && mAddedSampleId.equals(sample.getSampleId())) {
                    moveCameraTo(marker, false, true);
                    mAddedSampleId = "";
                }
                mSampleIdMarkerMap.put(sample.getSampleId(), marker);
            }
        }
    }

    @Override
    public void showNoSamples() {

    }

    @Override
    public void showSearchSampleResult(Sample sample) {
        Marker marker = mSampleIdMarkerMap.get(sample.getSampleId());
        moveCameraTo(marker, true, true);

    }

    private void moveCameraTo(final Marker marker, final boolean animate, final boolean shouldShowInfoWindow) {
        LatLng latLng = marker.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mCurrentZoom);

        if(animate) {
            mMapRoot.setEnabled(false);
            mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mMapRoot.setEnabled(true);
                    setAddSampleActive(true);

                    if (shouldShowInfoWindow) {
                        marker.showInfoWindow();
                    }
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            setAddSampleActive(true);
            mMap.moveCamera(cameraUpdate);
            if (shouldShowInfoWindow) {
                marker.showInfoWindow();
            }
        }
    }

    @Override
    public void showSearchQueryNotFound(String query) {
        showAdvanceSearchSuggestion(query);
    }

    @Override
    public void setSearchSampleActive(boolean active) {
        /*Drawable drawable = mSearchSampleBtn.getBackground();
        int alpha = active ? 255 : 123; // active 54%, non-active 26%
        drawable.setAlpha(alpha);
        mSearchSampleBtn.setIcon(drawable);
        mSearchSampleBtn.setEnabled(active);*/

        mInputAction.setAlpha(active ? 1f : 0.54f);
        mInputAction.setEnabled(active);
    }

    @Override
    public void setAddSampleActive(boolean active) {
        /*Drawable drawable = mAddSampleBtn.getBackground();
        int alpha = active ? 255 : 123; // default icon opacity is active 54% (mean 138/255, but here we map 138 as 255), non-active 26%
        drawable.setAlpha(alpha);
        mAddSampleBtn.setIcon(drawable);
        mAddSampleBtn.setEnabled(active);*/

        mInputAction.setAlpha(active ? 1f : 0.54f);
        mInputAction.setEnabled(active);
    }

    private void showAdvanceSearchSuggestion(final String sampleSearchValue) {
        mSnackbar = Snackbar.
                make(findViewById(R.id.rootLayout), "Không tìm thấy mẫu thử " + sampleSearchValue, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.setAction("Tìm nâng cao", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(SearchActivity.getStartIntent(SamplesActivity.this, sampleSearchValue), SEARCH_REQUEST_CODE);
                mSnackbar.dismiss();
            }
        });
        mSnackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.dismiss();
            }
        });
        mSnackbar.show();
    }

    @Override
    public void highlightSavedSampleMarker(String sampleId) {
        mAddedSampleId = sampleId;
    }

    @Override
    public void setDeleteSampleMarkerVisible(String sampleId, boolean visible) {
        mSampleIdMarkerMap.get(sampleId).setVisible(visible);
    }

    @Override
    public void notifySampleDeleted(boolean success, final String sampleId) {
        String message = "Xoá mẫu thử " + sampleId + (success ? " thành công" : " thất bại");

        mSnackbar = Snackbar.
                make(findViewById(R.id.rootLayout),  message, Snackbar.LENGTH_LONG);
        if(!success) {
            mSnackbar.setAction("Xem", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveCameraTo(mSampleIdMarkerMap.get(sampleId), false, true);
                }
            });
        }
        mSnackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.dismiss();
            }
        });
        mSnackbar.show();
    }

    @Override
    public void notifySampleUpdated(boolean success, final Sample sample) {
        final String sampleId = sample.getSampleId();
        String message = "Cập nhật mẫu thử " + sampleId + (success ? " thành công" : " thất bại");

        if(success) {
            Marker marker = mSampleIdMarkerMap.get(sampleId);
            int index = (int) marker.getTag();
            mSamples.set(index, sample);
        }

        mSnackbar = Snackbar.
                make(findViewById(R.id.rootLayout),  message, Snackbar.LENGTH_LONG);
        mSnackbar.setAction("Xem", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCameraTo(mSampleIdMarkerMap.get(sampleId), false, true);
            }
        });
        mSnackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.dismiss();
            }
        });
        mSnackbar.show();
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView titleUi = ((TextView) mContents.findViewById(R.id.title));
            TextView snippetUi = ((TextView) mContents.findViewById(R.id.snippet));

            String title = marker.getTitle();
            String snippet = marker.getSnippet();

            titleUi.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
            titleUi.setText(title);
            snippetUi.setVisibility(!TextUtils.isEmpty(snippet) ? View.VISIBLE : View.GONE);
            snippetUi.setText(snippet);
            return mContents;
        }
    }
}
