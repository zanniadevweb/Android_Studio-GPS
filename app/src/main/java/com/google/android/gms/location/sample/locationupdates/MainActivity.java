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

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
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
import java.util.Date;
import java.util.Locale;
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
     * Constante utilisée pour faire les demandes de permissions en temps réel.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constante utilisée dans les paramètres de l'interface de localisation.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000; // Paramètre par défaut : 1 seconde

    /**
     * Le taux le plus rapide pour les mises à jour de la localisation. Exact.
     * Les mises à jour ne seront jamais plus fréquentes que cette valeur.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2; // Paramètre par défaut : Toutes les demi-secondes

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Fournit l'accès à l'API de localisation : 'Fused Location Provider API'.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Fournit l'accès aux paramètres de l'API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stocke les paramètres pour les demandes propres à l'API de localisation 'Fused Location Provider API'.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stocke les types des services de localisation auquel le client pourrait être intéressé d'utiliser. Utiliser pour vérifier
     * les paramètres qui déterminent si l'appareil a des paramètres optimals pour la localisation.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Appel de retour pour les événements de localisation.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represente la coordonnée géographique de la localisation de l'utilisateur.
     */
    private Location mCurrentLocation;

    // Widgets de l'UI.
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
     * Récupère le statut de la demande de localisation. La valeur change quand l'utilisateur appuie sur :
     * les boutons de "commencerLocalisation" et "arreterLocalisation".
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
        ImageView imgv2 = findViewById(R.id.imageViewSalleVerte1);
        ImageView imgv3 = findViewById(R.id.moi1);
        ImageView imgv4 = findViewById(R.id.imageViewSalleRouge2);
        ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
        ImageView imgv6 = findViewById(R.id.moi2);

        // Masque au démarrage de l'application les salles rouges et la position de l'utilisateur (localisation pas encore démarrée)
        View[] views1 = { imgv1, imgv3, imgv4, imgv6 };
        for (View view : views1) {
            view.setVisibility(View.INVISIBLE);
        }

        // Affiche au démarrage de l'application les salles vertes
        View[] views2 = { imgv2, imgv5 };
        for (View view : views2) {
            view.setVisibility(View.VISIBLE);
        }

        // Localiser les widgets de l'UI.
        mStartUpdatesButton = (Button) findViewById(R.id.commencer_localisation_bouton);
        mStopUpdatesButton = (Button) findViewById(R.id.arreter_localisation_bouton);
        RegenererPositionSalles = (Button) findViewById(R.id.regenererPositionsSalles);
        GenererUnNombre = (Button) findViewById(R.id.generernombrealeatoire);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Définir les labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Mettre à jour les valeurs en utilisant les données stockées dans le Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Met en route le procédé de mise en route des objets de 'LocationCallback', 'LocationRequest', et 'LocationSettingsRequest'
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Met à jour les champs sur la base des données stockées dans le bundle.
     *
     * @param savedInstanceState L'état de l'activité est sauvée dans le bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Met à jour la valeur de 'mRequestingLocationUpdates' du Bundle, et assure que
            // 'commencerLocalisation' et 'arreterLocalisation' ont été correctement activé ou désactivé.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Met à jour mCurrentLocation du Bundle et met à jour à l'UI pour monter la
            // localisation et latitude correcte.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Comme KEY_LOCATION est trouvé dans le bundle, on veut être sûr que mCurrentLocation ne soit pas nul
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Met à jour les valeurs de mLastUpdateTime du Bundle et met à jour l'UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Met en place la demande de localisation. Android a deux types de paramètres pour la localisation:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. Ces paramètres contrôlent
     * la précision de la localisation actuelle. Ce projet utilise ACCESS_FINE_LOCATION, tel que défini dans
     * le AndroidManifest.xml.
     * <p/>
     * Quand le paramètre ACCESS_FINE_LOCATION est spécifié, combiné avec un interval de mise à jour rapide
     * (5 secondes), l'API 'Fused Location Provider API' retourne des mises à jour de localisation
     * qui sont précises à quelques mètres près.
     * <p/>
     * Ces paramètres sont appropriés pour des applications de cartographie qui montrent une localisation
     * avec des mises à jour en temps réel.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Met l'intervalle de temps désiré pour les mises à jour de localisation. Cet intervalle est
        // inexacte. Vous pourrez ne recevoir aucune localisation s'il y a pas de données de localisation disponibles;
        // ou vous pourrez les recevoir plus tardivement que prévu. Vous pourrez aussi recevoir ces données plus rapidement
        // que prévu si d'autres applications demandent une localisation à un intervall de temps plus rapide.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Met à jour l'intervalle le plus rapide pour les mises à jour de localisation. Cet intervalle est exact, et votre
        // application ne recevra jamais de valeurs plus rapides que celle-ci.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Créé un appel de retour pour recevoir les événements de localisation
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
     * Utilise un {@link com.google.android.gms.location.LocationSettingsRequest.Builder} pour construire
     * un {@link com.google.android.gms.location.LocationSettingsRequest} qui est utilisé pour vérifier
     * que l'appareil a les bons paramètres de localisation.
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
            // Vérifie que le code Integer de la requête a été précédement initialisée pour startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Rien à faire. startLocationupdates() gets called in onResume again.
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
     * Prend en charge le bouton commencerLocalisation et demande une mise à jour des données de localisation. Ne fait rien si
     * les mises à jour ont déjà été demandées.
     */
    public void commencerLocalisationBouton(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            commencerLocalisation();
        }
    }

    /**
     * Gère le bouton 'arreterLocalisation' et demande l'arrêt des mises à jour de localisation.
     */
    public void arreterLocalisationBouton(View view) {
        // C'est une bonne pratique que d'enlever les demandes de localisation quand l'activité est en pause ou
        // à l'état d'arrêt. Cela permet d'augmenter la performance de la batterie et cela est particulièrement recommandé
        // pour les applications qui demandent des mises à jour de localisation fréquentes.
        arreterLocalisation();
    }

    /**
     * Demande les mises à jour de localisation à l'API 'FusedLocationApi'. Note: on ne l'appelle pas à moins que
     * la permission de localisation n'ait été accordée.
     */
    private void commencerLocalisation() {
        // Commence par vérifier si l'appareil a les permissions nécessaires pour la localisation.
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
     * Met à jour tous les champs de l'UI
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    /**
     * Désactive les deux boutons quand la fonctionnalité est désactivée en raison de paramètres de localisation insuffisants.
     * Sinon cela assure que seulement un bouton soit actif à la fois. Le bouton 'commencerLocalisation'
     * est activable si l'utilisateur n'a pas demandé de mises à jour de localisation. Le bouton 'arreterLocalisation'
     *  est activable si l'utilisateur a demandé des mises à jour de localisation.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) { // Si la localisation est arrêtée
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
            RegenererPositionSalles.setEnabled(true);
            GenererUnNombre.setEnabled(false);
        } else { // Si la localisation est en cours
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
            RegenererPositionSalles.setEnabled(false);
            GenererUnNombre.setEnabled(true);

            ImageView imgv1 = findViewById(R.id.imageViewSalleRouge2);
            ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
            ImageView imgv3 = findViewById(R.id.moi1);
            ImageView imgv4 = findViewById(R.id.imageViewSalleVerte1);
            ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
            ImageView imgv6 = findViewById(R.id.moi2);
            TextView tv1 = findViewById(R.id.nombrePersonnesActuelSalle1);
            tv1.setText(String.valueOf(Salle.compteurEleveSalle1));
            TextView tv2 = findViewById(R.id.nombrePersonnesActuelSalle2);
            tv2.setText(String.valueOf(Salle.compteurEleveSalle2));
            TextView tv3 = findViewById(R.id.librepaslibre1);
            TextView tv4 = findViewById(R.id.librepaslibre2);

            View[] views1 = {imgv1, imgv2, imgv3, imgv6  };
            for (View view : views1) {
                view.setVisibility(View.INVISIBLE);
            }

            View[] views2 = {imgv4, imgv5  };
            for (View view : views2) {
                view.setVisibility(View.VISIBLE);
            }

            tv3.setText(getString(R.string.estLibre1_label));
            tv4.setText(getString(R.string.estLibre2_label));

            Salle.compteurEleveSalle1 = Salle.randomPersonnesSalle1;
            Salle.compteurEleveSalle2 = Salle.randomPersonnesSalle2;
        }
    }

    private void fixerPositionsSalles() {
            final double latitudeTempsTConstant = Localisation.latitudeTempsT;
            final double longitudeTempsTConstant = Localisation.longitudeTempsT;
            Salle.latitudeCentreSalleUneDynamique=latitudeTempsTConstant;
            Salle.longitudeCentreSalleUneDynamique=longitudeTempsTConstant;
            Salle.latitudeCentreSalleDeuxDynamique=latitudeTempsTConstant + Salle.quinzeMetresLatitude;
            Salle.longitudeCentreSalleDeuxDynamique=longitudeTempsTConstant + Salle.quinzeMetresLongitude;
    }

    public void regenererPositionsSalles(View view) {
        Localisation.valeursLongLatAttribuees = true;
    }

    /**
     * Met les valeurs des champs de l'UI pour la localisation (latitude, longitude) et temps de dernière localisation en temps réel.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- LOCALISATION AUTOMATIQUE ------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));

            Localisation.updatetime = mLastUpdateTime;

            if (Localisation.valeursLongLatAttribuees) {
                fixerPositionsSalles();
                Localisation.valeursLongLatAttribuees = false;
            }

            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- STOCKAGE COORDONNES DES SALLES ------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            final double longitudeCentreSalleConstant1 = Salle.longitudeCentreSalleUneDynamique; // Centre salle (x) (Fixé à la même position que l'utilisateur)
            final double latitudeCentreSalleConstant1 = Salle.latitudeCentreSalleUneDynamique; // Centre salle (y) (Fixé à la même position que l'utilisateur)

            final double longitudeCentreSalleConstant2 = Salle.longitudeCentreSalleDeuxDynamique; // Centre salle (x) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en latitude)
            final double latitudeCentreSalleConstant2 = Salle.latitudeCentreSalleDeuxDynamique; // Centre salle (y) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en longitude)

            double maLatitudeTresPrecise = mCurrentLocation.getLatitude();
            double maLatitudeMoinsPrecise = (int)(Math.round(maLatitudeTresPrecise * 10000000))/10000000.0; // Récupérer le résultat de ma localisation (latitude) 7 chiffres après la virgule
            Localisation.longitudeTempsT = maLatitudeMoinsPrecise;
            double maLongitudeTresPrecise = mCurrentLocation.getLongitude();
            double maLongitudeMoinsPrecise = (int)(Math.round(maLongitudeTresPrecise * 10000000))/10000000.0; // Récupérer le résultat de ma localisation (longitude) 7 chiffres après la virgule
            Localisation.latitudeTempsT = maLongitudeMoinsPrecise;

            int rayonSalle = 10; // Le rayon d'une salle en mètres

            float[] distanceSalle1 = new float[1];
            float[] distanceSalle2 = new float[1];

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- SALLE 1 -----------------------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            TextView tv1 = findViewById(R.id.longitudeValeurSalle1);
            tv1.setText(String.valueOf(longitudeCentreSalleConstant1));

            TextView tv2 = findViewById(R.id.latitudeValeurSalle1);
            tv2.setText(String.valueOf(latitudeCentreSalleConstant1));

            Location.distanceBetween(
                    maLongitudeMoinsPrecise, // Latitude de départ (Moi)
                    maLatitudeMoinsPrecise, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant1, // Latitude de fin (salle)
                    longitudeCentreSalleConstant1, // Longitude de fin (salle)
                    distanceSalle1); // Résultat = distance entre deux points

            TextView tv8 = findViewById(R.id.maDistanceSalle1);
            tv8.setText("Je suis à environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci");

            // Si les coordonnées où je suis (latitude, longitude) sont égales à celles de la salle1... alors.. je me trouve dans cette salle
            if (distanceSalle1[0] < rayonSalle) { // || && Modele.estDansSalle1
                Salle.compteurEleveSalle1 = Salle.randomPersonnesSalle1;
                Salle.compteurEleveSalle1++;
                TextView tv0 = findViewById(R.id.nombrePersonnesActuelSalle1);
                tv0.setText(String.valueOf(Salle.compteurEleveSalle1));
                Integer limiteNombreEleveSalle1 = 10;
                if (Salle.compteurEleveSalle1 > limiteNombreEleveSalle1) {
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.INVISIBLE);
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.VISIBLE);
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    tv3.setText(getString(R.string.estPasLibre1_label));
                }
                if (Salle.compteurEleveSalle1 <= limiteNombreEleveSalle1) {
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.INVISIBLE);
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.VISIBLE);
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    tv3.setText(getString(R.string.estLibre1_label));
                }
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.VISIBLE);
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                tv4.setText(getString(R.string.dansSalle1_label));
                Localisation.estDansSalle2 = false;
            }
            if (distanceSalle1[0] > rayonSalle) { // || && !(Modele.estDansSalle1)
                ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                imgv1.setVisibility(View.VISIBLE);
                ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                imgv2.setVisibility(View.INVISIBLE);
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.INVISIBLE);
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                tv4.setText(getString(R.string.pasSalle1_label));
                Localisation.estDansSalle2 = true;
            }


            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- SALLE 2 -----------------------------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            TextView tv3 = findViewById(R.id.longitudeValeurSalle2);
            tv3.setText(String.valueOf(longitudeCentreSalleConstant2));


            TextView tv4 = findViewById(R.id.latitudeValeurSalle2);
            tv4.setText(String.valueOf(latitudeCentreSalleConstant2));

            Location.distanceBetween(
                    maLongitudeMoinsPrecise, // Latitude de départ (Moi)
                    maLatitudeMoinsPrecise, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant2, // Latitude de fin (salle)
                    longitudeCentreSalleConstant2, // Longitude de fin (salle)
                    distanceSalle2); // Résultat = distance entre deux points

            TextView tv7 = findViewById(R.id.maDistanceSalle2);
            tv7.setText("Je suis à environ " + distanceSalle2[0]/2 + " mètres du centre de celle-ci");

            // Si les coordonnées où je suis (latitude, longitude) sont égales à celles de la salle1... alors.. je me trouve dans cette salle
            if (distanceSalle2[0] < rayonSalle) { // || && Modele.estDansSalle2
                Salle.compteurEleveSalle2 = Salle.randomPersonnesSalle2;
                Salle.compteurEleveSalle2++;
                TextView tv0 = findViewById(R.id.nombrePersonnesActuelSalle2);
                tv0.setText(String.valueOf(Salle.compteurEleveSalle2));
                Integer limiteNombreEleveSalle2 = 10;
                if (Salle.compteurEleveSalle2 > limiteNombreEleveSalle2) {
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.INVISIBLE);
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.VISIBLE);
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText(getString(R.string.estPasLibre2_label));
                }
                if (Salle.compteurEleveSalle2 <= limiteNombreEleveSalle2) {
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.VISIBLE);
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.INVISIBLE);
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText(getString(R.string.estLibre2_label));
                }
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.VISIBLE);
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText(getString(R.string.dansSalle2_label));
                Localisation.estDansSalle1 = false;
            }
            if (distanceSalle2[0] > rayonSalle) { // || && !(Modele.estDansSalle2
                ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                imgv3.setVisibility(View.INVISIBLE);
                ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                imgv2.setVisibility(View.VISIBLE);
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.INVISIBLE);
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText(getString(R.string.pasSalle2_label));
                Localisation.estDansSalle1 = true;
            }

        }
    }

    public void onGenerateRandomNumber(View view) {
        Salle.randomPersonnesSalle1 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        Salle.randomPersonnesSalle2 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        TextView tv1 = findViewById(R.id.nombrePersonnesActuelSalle1);
        tv1.setText(String.valueOf(Salle.randomPersonnesSalle1));
        TextView tv2 = findViewById(R.id.nombrePersonnesActuelSalle2);
        tv2.setText(String.valueOf(Salle.randomPersonnesSalle2));
    }



    /**
     * Enlève les mises à jour de localisation pour l'API 'FusedLocationApi'.
     */
    private void arreterLocalisation() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "arreterLocalisation: mises à jour jamais demandées, pas de résultat.");
            return;
        }

        // C'est une bonne pratique que d'enlever les demandes de localisation quand l'activité est en pause ou
        // à l'état d'arrêt. Cela permet d'augmenter la performance de la batterie et cela est particulièrement recommandé
        // pour les applications qui demandent des mises à jour de localisation fréquentes.
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
        if (mRequestingLocationUpdates && verifierPermissions()) {
            commencerLocalisation();
        } else if (!verifierPermissions()) {
            demanderPermissions();
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Enlèves les mises à jour de localisation pour économiser de la batterie.
        arreterLocalisation();
    }

    /**
     * Stocke les données de l'activité dans le bundle.
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
     * @param mainTextStringId L'id pour la ressource string du texte de la Snackbar.
     * @param actionStringId   Le texte de l'item d'action.
     * @param listener         L'écouteur associé à l'action du Snackbar.
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
     * Retourne l'état actuel de la permission requise.
     */
    private boolean verifierPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void demanderPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Fournit une information supplémentaire à l'utilisateur. Cela arrivera si l'utilisateur refuse
        // la requête précédente ou n'a pas coché la case : "Don't ask again".
        if (shouldProvideRationale) {
            Log.i(TAG, "Affiche les permissions rationnelles pour fournir un contexte plus détaillé.");
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
            Log.i(TAG, "Demande de permission");
            // Demande de permission. Il est possible que la réponse se fasse seule si les politiques de l'appareil
            // mettent la permission dans un état donné ou que l'utilisateur a refusé la permission
            // précédente et a coché la case "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Appel de retour reçu quand une demande de permission a été achevée.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // Si l'interaction avec l'utilisateur est interrompue, la demande de permission est stoppée
                // et vous allez recevoir des valeurs nulles.
                Log.i(TAG, "L'interaction avec l'utilisateur a été annulé.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission accordée, mises à jour permises, commencer la localisation");
                    commencerLocalisation();
                }
            } else {
                // Permission refusée.

                // Avertit l'utilisateur via un SnackBar qu'ils ont rejetés une permission du 'Core' pour
                // l'application, ce qui rend l'activité inutilisable. Dans une vraie application, les permissions de 'Core'
                // seraient typiquement demandées via un process sur l'écran d'accueil.

                // De plus, il est important de se rappeler qu'une permision pourrait avoir été rejetée
                // sans avoir demandé à l'utilisateur la permission (politque de l'appreil ou les messages du type
                // "Never ask again"). En conséquence, l'apport de l'interface d'un utilisateur est typiquement implémenté
                // quand les permissions ont été refusées. Autrement, votre application pourrait apparaître insensible
                // aux interactions qui demandent des permissions.
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
