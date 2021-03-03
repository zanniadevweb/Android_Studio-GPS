/*
  Copyright 2017 Google Inc. All Rights Reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * Using location settings.
 * <p/>
 * Uses the {@link com.google.android.gms.location.SettingsApi} to ensure that the device's system
 * settings are properly configured for the app's location needs. When making a request to
 * Location services, the device's system settings may be in a state that prevents the app from
 * obtaining the location data that it needs. For example, GPS or Wi-Fi scanning may be switched
 * off. The {@code SettingsApi} makes it possible to determine if a device's system settings are
 * adequate for the location request, and to optionally invoke a dialog that allows the user to
 * enable the necessary settings.
 * <p/>
 * This sample allows the user to request location updates using the ACCESS_FINE_LOCATION setting
 * (as specified in AndroidManifest.xml).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    // UI Widgets.
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private Button RegenererPositionSalles;
    private Button GenererUnNombre;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    // Labels.
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ImageView imgv1 = findViewById(R.id.imageViewSalleRouge1);
        imgv1.setVisibility(View.INVISIBLE);

        ImageView imgv2 = findViewById(R.id.imageViewSalleVerte1);
        imgv2.setVisibility(View.VISIBLE);

        ImageView imgv3 = findViewById(R.id.moi1);
        imgv3.setVisibility(View.INVISIBLE);

        ImageView imgv4 = findViewById(R.id.imageViewSalleRouge2);
        imgv4.setVisibility(View.INVISIBLE);

        ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
        imgv5.setVisibility(View.VISIBLE);

        ImageView imgv6 = findViewById(R.id.moi2);
        imgv6.setVisibility(View.INVISIBLE);

        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        RegenererPositionSalles = (Button) findViewById(R.id.regenererPositionsSalles);
        GenererUnNombre = (Button) findViewById(R.id.generernombrealeatoire);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void stopUpdatesButtonHandler(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
            RegenererPositionSalles.setEnabled(true);
            GenererUnNombre.setEnabled(false);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
            RegenererPositionSalles.setEnabled(false);
            GenererUnNombre.setEnabled(true);
            ImageView imgv1 = findViewById(R.id.imageViewSalleRouge2);
            imgv1.setVisibility(View.INVISIBLE);
            ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
            imgv2.setVisibility(View.INVISIBLE);
            ImageView imgv3 = findViewById(R.id.moi1);
            imgv3.setVisibility(View.INVISIBLE);
            ImageView imgv4 = findViewById(R.id.imageViewSalleVerte1);
            imgv4.setVisibility(View.VISIBLE);
            ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
            imgv5.setVisibility(View.VISIBLE);
            ImageView imgv6 = findViewById(R.id.moi2);
            imgv6.setVisibility(View.INVISIBLE);
            Modele.compteurEleveSalle1 = Modele.randomPersonnesSalle1;
            Modele.compteurEleveSalle2 = Modele.randomPersonnesSalle2;

            TextView tv1 = findViewById(R.id.nombrePersonnesActuelSalle1);
            tv1.setText(String.valueOf(Modele.compteurEleveSalle1));
            TextView tv2 = findViewById(R.id.nombrePersonnesActuelSalle2);
            tv2.setText(String.valueOf(Modele.compteurEleveSalle2));
            TextView tv3 = findViewById(R.id.librepaslibre1);
            tv3.setText("La Salle1 est libre");
            TextView tv4 = findViewById(R.id.librepaslibre2);
            tv4.setText("La Salle1 est libre");
        }
    }

    private void fixerPositionsSalles() {
            final double latitudeTempsTConstant = Modele.latitudeTempsT;
            final double longitudeTempsTConstant = Modele.longitudeTempsT;
            Modele.latitudeCentreSalleUneDynamique=latitudeTempsTConstant;
            Modele.longitudeCentreSalleUneDynamique=longitudeTempsTConstant;
            Modele.latitudeCentreSalleDeuxDynamique=latitudeTempsTConstant + Modele.quinzeMetresLatitude;
            Modele.longitudeCentreSalleDeuxDynamique=longitudeTempsTConstant + Modele.quinzeMetresLongitude;
    }

    public void regenererPositionsSalles(View view) {
        Modele.valeursLongLatAttribuees = true;
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- LOCALISATION AUTOMATIQUE ------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));

            Modele.updatetime = mLastUpdateTime;

            Modele.latitudeTempsT = mCurrentLocation.getLatitude();
            Modele.longitudeTempsT = mCurrentLocation.getLongitude();

            if (Modele.valeursLongLatAttribuees) {
                fixerPositionsSalles();
                Modele.valeursLongLatAttribuees = false;
            }

            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- STOCKAGE COORDONNES DES SALLES ------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            HashMap<String, Double> LongitudeSalles = new HashMap<>();
            HashMap<String, Double> LatitudeSalles = new HashMap<>();

            final double longitudeCentreSalleConstant1 = Modele.longitudeCentreSalleUneDynamique; // Centre salle (x) (Fixé à la même position que l'utilisateur)
            final double latitudeCentreSalleConstant1 = Modele.latitudeCentreSalleUneDynamique; // Centre salle (y) (Fixé à la même position que l'utilisateur)

            final double longitudeCentreSalleConstant2 = Modele.longitudeCentreSalleDeuxDynamique; // Centre salle (x) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en latitude)
            final double latitudeCentreSalleConstant2 = Modele.latitudeCentreSalleDeuxDynamique; // Centre salle (y) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en longitude)

            LongitudeSalles.put("Salle1", latitudeCentreSalleConstant1); // Latitude du point dans une salle (c'est = à mon emplacement actuel)
            LatitudeSalles.put("Salle1", longitudeCentreSalleConstant1); // Longitude du point dans une salle (c'est = à mon emplacement actuel)

            LongitudeSalles.put("Salle2", latitudeCentreSalleConstant2); // Latitude du point dans une salle (c'est = à mon emplacement actuel)
            LatitudeSalles.put("Salle2", longitudeCentreSalleConstant2); // Longitude du point dans une salle (c'est = à mon emplacement actuel)

            Log.d ("1","long1 : " + longitudeCentreSalleConstant1);
            Log.d ("2","lat1 : " + latitudeCentreSalleConstant1);

            Log.d ("3","long2 : " + longitudeCentreSalleConstant2);
            Log.d ("4","lat2 : " + latitudeCentreSalleConstant2);


            // ---------------- A CORRIGER --------------

            // Obtenir la première valeur du Hash Map
            /*
            Iterator<String> iteratorLong = LongitudeSalles.keySet().iterator();
            Iterator<String> iteratorLat = LongitudeSalles.keySet().iterator();
             */
            Iterator<String> iteratorNomSalle = LongitudeSalles.keySet().iterator();
/*
            Double valueLongitude1 = null;
            Double valueLongitude2 = null;
            if(iteratorLong.hasNext()){
                valueLongitude1 = LongitudeSalles.get( iteratorLong.next() );
                valueLongitude2 = LongitudeSalles.get(1);
            }

            Double valueLatitude1 = null;
            Double valueLatitude2 = null;
            if(iteratorLat.hasNext()){
                valueLatitude1 = LatitudeSalles.get( iteratorLat.next() );
                valueLatitude2 = LongitudeSalles.get(1);
            }
*/
            String key = null;
            if(iteratorNomSalle.hasNext()){
                key = iteratorNomSalle.next();
            }

            // ---------------- A CORRIGER --------------

            Integer rayonSalle = 5; // Le rayon d'une salle en mètres

            float[] distanceSalle1 = new float[1];
            float[] distanceSalle2 = new float[1];

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- SALLE 1 -----------------------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            TextView tv1 = findViewById(R.id.longitudeValeurSalle1);
            //tv1.setText(String.valueOf(valueLongitude1)); // ---------------- A CORRIGER --------------
            tv1.setText(String.valueOf(longitudeCentreSalleConstant1));

            TextView tv2 = findViewById(R.id.latitudeValeurSalle1);
            //tv2.setText(String.valueOf(valueLatitude1)); // ---------------- A CORRIGER --------------
            tv2.setText(String.valueOf(latitudeCentreSalleConstant1));

            Location.distanceBetween(
                    Modele.latitudeTempsT, // Latitude de départ (Moi)
                    Modele.longitudeTempsT, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant1, // Latitude de fin (salle)
                    longitudeCentreSalleConstant1, // Longitude de fin (salle)
                    distanceSalle1); // Résultat = distance entre deux points

            // Si les coordonnées où je suis (latitude, longitude) sont égales à celles de la salle1... alors.. je me trouve dans cette salle
            if (distanceSalle1[0]/2 < rayonSalle && Modele.estDansSalle1) { // || distanceSalle1[0]/2 < distanceSalle2[0]/2
                Modele.compteurEleveSalle1 = Modele.randomPersonnesSalle1;
                Modele.compteurEleveSalle1++;
                TextView tv0 = findViewById(R.id.nombrePersonnesActuelSalle1);
                tv0.setText(String.valueOf(Modele.compteurEleveSalle1));
                Log.d("estDansSalle", "Vous êtes dans la " + key);
                Log.d("élève", "Vous êtes le " + Modele.compteurEleveSalle1 + "e élève dans cette salle");
                Integer limiteNombreEleveSalle1 = 10;
                if (Modele.compteurEleveSalle1 > limiteNombreEleveSalle1) {
                    Log.d("salleIndisponible", "Désolé, la salle vous est fermée d'accès car la limite est de " + limiteNombreEleveSalle1 + " élèves dans cette salle");
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.INVISIBLE);
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.VISIBLE);
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    //tv3.setText("La " + key + " est indisponible"); // ---------------- A CORRIGER --------------
                    tv3.setText("La Salle1 est indisponible");
                }
                if (Modele.compteurEleveSalle1 <= limiteNombreEleveSalle1) {
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.INVISIBLE);
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.VISIBLE);
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    //tv3.setText("La " + key + " est libre pour l'instant"); // ---------------- A CORRIGER --------------
                    tv3.setText("La Salle1 est libre");
                }
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.VISIBLE);
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                //tv4.setText("Je suis dans la " + key + " à environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci"); // ---------------- A CORRIGER --------------
                tv4.setText("Je suis dans la Salle1 à environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci");
                Modele.estDansSalle2 = false;
            }
            if (distanceSalle1[0]/2 > rayonSalle && !(Modele.estDansSalle1)) { // || distanceSalle1[0]/2 > distanceSalle2[0]/2
                ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                imgv1.setVisibility(View.VISIBLE);
                ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                imgv2.setVisibility(View.INVISIBLE);
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.INVISIBLE);
                //TextView tv4 = findViewById(R.id.mapositionsalle); // ---------------- A CORRIGER --------------
                //tv4.setText("Je ne suis PAS dans la " + key);
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                //tv4.setText("Je ne suis PAS dans la " + key + ". Je suis à une distance d'environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci"); // ---------------- A CORRIGER --------------
                tv4.setText("Je ne suis PAS dans la Salle1. Je suis à une distance d'environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci");
                Modele.estDansSalle2 = true;
            }


            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- SALLE 2 -----------------------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/


            TextView tv3 = findViewById(R.id.longitudeValeurSalle2);
            //tv3.setText(String.valueOf(valueLongitude2));  // ---------------- A CORRIGER --------------
            tv3.setText(String.valueOf(longitudeCentreSalleConstant2));


            TextView tv4 = findViewById(R.id.latitudeValeurSalle2);
            //tv4.setText(String.valueOf(valueLatitude2)); // ---------------- A CORRIGER --------------
            tv4.setText(String.valueOf(latitudeCentreSalleConstant2));

            Location.distanceBetween(
                    Modele.latitudeTempsT, // Latitude de départ (Moi)
                    Modele.longitudeTempsT, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant2, // Latitude de fin (salle)
                    longitudeCentreSalleConstant2, // Longitude de fin (salle)
                    distanceSalle2); // Résultat = distance entre deux points

            // Si les coordonnées où je suis (latitude, longitude) sont égales à celles de la salle1... alors.. je me trouve dans cette salle
            if (distanceSalle2[0]/2 < rayonSalle && Modele.estDansSalle2) { // || distanceSalle2[0]/2 < distanceSalle1[0]/2
                Modele.compteurEleveSalle2 = Modele.randomPersonnesSalle2;
                Modele.compteurEleveSalle2++;
                TextView tv0 = findViewById(R.id.nombrePersonnesActuelSalle2);
                tv0.setText(String.valueOf(Modele.compteurEleveSalle2));
                Log.d("estDansSalle", "Vous êtes dans la " + key);
                Log.d("élève", "Vous êtes le " + Modele.compteurEleveSalle2 + "e élève dans cette salle");
                Integer limiteNombreEleveSalle2 = 10;
                if (Modele.compteurEleveSalle2 > limiteNombreEleveSalle2) {
                    Log.d("salleIndisponible", "Désolé, la salle vous est fermée d'accès car la limite est de " + limiteNombreEleveSalle2 + " élèves dans cette salle");
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.INVISIBLE);
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.VISIBLE);
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText("La " + key + " est indisponible");
                }
                if (Modele.compteurEleveSalle2 <= limiteNombreEleveSalle2) {
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.VISIBLE);
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.INVISIBLE);
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText("La " + key + " est libre");
                }
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.VISIBLE);
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText("Je suis dans la " + key + " à environ " + distanceSalle2[0]/2 + " mètres du centre de celle-ci");
                Modele.estDansSalle1 = false;
            }
            if (distanceSalle2[0]/2 > rayonSalle && !(Modele.estDansSalle2)) { // || distanceSalle2[0]/2 > distanceSalle1[0]/2
                ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                imgv3.setVisibility(View.INVISIBLE);
                ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                imgv2.setVisibility(View.VISIBLE);
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.INVISIBLE);
                //TextView tv6 = findViewById(R.id.mapositionsalle2);
                //tv6.setText("Je ne suis PAS dans la " + key);
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText("Je ne suis PAS dans la " + key + ". Je suis à une distance d'environ " + distanceSalle2[0]/2 + " mètres du centre de celle-ci");
                Modele.estDansSalle1 = true;
            }


            Log.d ("5","dist1 : " + String.valueOf(distanceSalle1[0]));
            Log.d ("6","dist2 : " + String.valueOf(distanceSalle2[0]));

        }
    }

    public void onGenerateRandomNumber(View view) {
        Modele.randomPersonnesSalle1 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        Modele.randomPersonnesSalle2 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        TextView tv1 = findViewById(R.id.nombrePersonnesActuelSalle1);
        tv1.setText(String.valueOf(Modele.randomPersonnesSalle1));
        TextView tv2 = findViewById(R.id.nombrePersonnesActuelSalle2);
        tv2.setText(String.valueOf(Modele.randomPersonnesSalle2));
    }



    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
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
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
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
                        });
            }
        }
    }
}
