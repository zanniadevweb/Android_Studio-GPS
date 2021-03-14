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
    private static final int DEMANDE_PERMISSIONS_CODE = 34;

    /**
     * Constante utilisée dans les paramètres de l'interface de localisation.
     */
    private static final int DEMANDE_VERIFICATION_PARAMETRES = 0x1;

    /**
     * L'intervalle désiré pour les mises à jour de localisation. Inexact. Les mises à jour peuvent être plus ou moins fréquentes.
     */
    private static final long METTRE_A_JOUR_INTERVALLE_EN_MILLISECONDES = 10000; // Paramètre par défaut : 1 seconde

    /**
     * Le taux le plus rapide pour les mises à jour de la localisation. Exact.
     * Les mises à jour ne seront jamais plus fréquentes que cette valeur.
     */
    private static final long MISE_A_JOUR_PLUS_RAPIDE_INTERVALLE_EN_MILLISECONDES =
            METTRE_A_JOUR_INTERVALLE_EN_MILLISECONDES / 2; // Paramètre par défaut : Chaque demi-seconde

    // Clés pour stocker les états de l'activité dans le bundle.
    private final static String KEY_DEMANDER_MISE_A_JOUR_LOCALISATION = "requesting-location-updates";
    private final static String KEY_LOCALISATION = "location";
    private final static String KEY_DERNIER_MISE_A_JOUR_TEMPS_STRING = "last-updated-time-string";

    /**
     * Fournit l'accès à l'API de localisation : 'Fused Location Provider API'.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Fournit l'accès aux paramètres de l'API.
     */
    private SettingsClient mParametresClient;

    /**
     * Stocke les paramètres pour les demandes propres à l'API de localisation 'Fused Location Provider API'.
     */
    private LocationRequest mDemandeLocalisation;

    /**
     * Stocke les types des services de localisation auquel le client pourrait être intéressé de se servir. Utilisé pour vérifier
     * les paramètres qui déterminent si l'appareil a des paramètres optimals pour la localisation.
     */
    private LocationSettingsRequest mParametresDemandeLocalisation;

    /**
     * Appel de retour pour les événements de localisation.
     */
    private LocationCallback mAppelRetourLocalisation;

    /**
     * Represente la coordonnée géographique de la localisation de l'utilisateur.
     */
    private Location mLocalisationActuelle;

    // Widgets de l'UI.
    private Button mCommencerLocalisationBouton;
    private Button mArreterLocalisationBouton;
    private Button RegenererPositionSalles;
    private Button GenererUnNombreUsagers;
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
    private Boolean mDemandeMiseAJourLocalisation;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Objets d'interface Android qui sont définis visibles / invisibles pour une localisation pas encore démarrée
        ImageView imgv1 = findViewById(R.id.imageViewSalleRouge1);
        ImageView imgv2 = findViewById(R.id.imageViewSalleVerte1);
        ImageView imgv3 = findViewById(R.id.moi1);
        ImageView imgv4 = findViewById(R.id.imageViewSalleRouge2);
        ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
        ImageView imgv6 = findViewById(R.id.moi2);

        // Masque au démarrage de l'application les salles rouges et l'icône de localisation de l'utilisateur
        View[] views1 = { imgv1, imgv3, imgv4, imgv6 };
        for (View view : views1) {
            view.setVisibility(View.INVISIBLE);
        }

        // Affiche au démarrage de l'application les salles en vert
        View[] views2 = { imgv2, imgv5 };
        for (View view : views2) {
            view.setVisibility(View.VISIBLE);
        }

        // Localiser les widgets de l'UI.
        mCommencerLocalisationBouton = (Button) findViewById(R.id.commencer_localisation_bouton);
        mArreterLocalisationBouton = (Button) findViewById(R.id.arreter_localisation_bouton);
        RegenererPositionSalles = (Button) findViewById(R.id.regenererPositionsSalles);
        GenererUnNombreUsagers = (Button) findViewById(R.id.generernombrealeatoire);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Définir les labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mDemandeMiseAJourLocalisation = false;
        mLastUpdateTime = "";

        // Mettre à jour les valeurs en utilisant les données stockées dans le Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mParametresClient = LocationServices.getSettingsClient(this);

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
            if (savedInstanceState.keySet().contains(KEY_DEMANDER_MISE_A_JOUR_LOCALISATION)) {
                mDemandeMiseAJourLocalisation = savedInstanceState.getBoolean(
                        KEY_DEMANDER_MISE_A_JOUR_LOCALISATION);
            }

            // Met à jour mCurrentLocation du Bundle et met à jour à l'UI pour monter la
            // localisation et latitude correcte.
            if (savedInstanceState.keySet().contains(KEY_LOCALISATION)) {
                // Comme KEY_LOCATION est trouvé dans le bundle, on veut être sûr que mCurrentLocation ne soit pas nul
                mLocalisationActuelle = savedInstanceState.getParcelable(KEY_LOCALISATION);
            }

            // Met à jour les valeurs de mLastUpdateTime du Bundle et met à jour l'UI.
            if (savedInstanceState.keySet().contains(KEY_DERNIER_MISE_A_JOUR_TEMPS_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_DERNIER_MISE_A_JOUR_TEMPS_STRING);
            }
            mettreAjourUI();
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
        mDemandeLocalisation = new LocationRequest();

        // Met l'intervalle de temps désiré pour les mises à jour de localisation. Cet intervalle est
        // inexacte. Vous pourrez ne recevoir aucune localisation s'il y a pas de données de localisation disponibles;
        // ou vous pourrez les recevoir plus tardivement que prévu. Vous pourrez aussi recevoir ces données plus rapidement
        // que prévu si d'autres applications demandent une localisation à un intervall de temps plus rapide.
        mDemandeLocalisation.setInterval(METTRE_A_JOUR_INTERVALLE_EN_MILLISECONDES);

        // Met à jour l'intervalle le plus rapide pour les mises à jour de localisation. Cet intervalle est exact, et votre
        // application ne recevra jamais de valeurs plus rapides que celle-ci.
        mDemandeLocalisation.setFastestInterval(MISE_A_JOUR_PLUS_RAPIDE_INTERVALLE_EN_MILLISECONDES);

        mDemandeLocalisation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Créé un appel de retour pour recevoir les événements de localisation
     */
    private void createLocationCallback() {
        mAppelRetourLocalisation = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mLocalisationActuelle = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                mettreAjourUILocalisation();
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
        builder.addLocationRequest(mDemandeLocalisation);
        mParametresDemandeLocalisation = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Vérifie que le code Integer de la requête a été précédement initialisée pour startResolutionForResult().
            case DEMANDE_VERIFICATION_PARAMETRES:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Rien à faire. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mDemandeMiseAJourLocalisation = false;
                        mettreAjourUI();
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
        if (!mDemandeMiseAJourLocalisation) {
            mDemandeMiseAJourLocalisation = true;
            definirEtatBoutons();
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
        mParametresClient.checkLocationSettings(mParametresDemandeLocalisation)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mDemandeLocalisation,
                                mAppelRetourLocalisation, Looper.myLooper());

                        mettreAjourUI();
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
                                    rae.startResolutionForResult(MainActivity.this, DEMANDE_VERIFICATION_PARAMETRES);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mDemandeMiseAJourLocalisation = false;
                        }

                        mettreAjourUI();
                    }
                });
    }

    /**
     * Met à jour tous les champs de l'UI
     */
    private void mettreAjourUI() {
        definirEtatBoutons();
        mettreAjourUILocalisation();
    }

    /**
     * Désactive les deux boutons quand la fonctionnalité est désactivée en raison de paramètres de localisation insuffisants.
     * Sinon cela assure que seulement un bouton soit actif à la fois. Le bouton 'commencerLocalisation'
     * est activable si l'utilisateur n'a pas demandé de mises à jour de localisation. Le bouton 'arreterLocalisation'
     *  est activable si l'utilisateur a demandé des mises à jour de localisation.
     */
    private void definirEtatBoutons() {
        // Si la localisation est en cours
        if (mDemandeMiseAJourLocalisation) {
            // On rend impossible l'activation des boutons pour commencer la localisation et de régénération du nombre d'usagers par salle
            mCommencerLocalisationBouton.setEnabled(false);
            GenererUnNombreUsagers.setEnabled(false);

            // On rend à nouveau possible l'activation des boutons pour arrêter la localisation et de régénération de la positions des salles
            mArreterLocalisationBouton.setEnabled(true);
            RegenererPositionSalles.setEnabled(true);
        }

        // Si la localisation est arrêtée
        else {
            // On rend impossible l'activation des boutons pour arrêter la localisation et de régénération de la positions des salles
            mArreterLocalisationBouton.setEnabled(false);
            RegenererPositionSalles.setEnabled(false);

            // On rend à nouveau possible l'activation des boutons pour commencer la localisation et de régénération du nombre d'usagers par salle
            mCommencerLocalisationBouton.setEnabled(true);
            GenererUnNombreUsagers.setEnabled(true);

            // Objets d'interface Android qui sont définis visibles / invisibles pour une localisation remise à zéro (pas de localisation)
            ImageView imgv1 = findViewById(R.id.imageViewSalleRouge2);
            ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
            ImageView imgv3 = findViewById(R.id.moi1);
            ImageView imgv4 = findViewById(R.id.imageViewSalleVerte1);
            ImageView imgv5 = findViewById(R.id.imageViewSalleVerte2);
            ImageView imgv6 = findViewById(R.id.moi2);
            TextView tv1 = findViewById(R.id.nombreUsagersActuelSalle1);
            tv1.setText(String.valueOf(Salle.compteurUsagersSalle1));
            TextView tv2 = findViewById(R.id.nombreUsagersActuelSalle2);
            tv2.setText(String.valueOf(Salle.compteurUsagersSalle2));
            TextView tv3 = findViewById(R.id.librepaslibre1);
            TextView tv4 = findViewById(R.id.librepaslibre2);

            // Remet à zéro comme au démarrage de l'application le fait de masquer les salles rouges et l'icône de localisation de l'utilisateur
            View[] views1 = { imgv1, imgv2, imgv3, imgv6 };
            for (View view : views1) {
                view.setVisibility(View.INVISIBLE);
            }

            // Remet à zéro comme au démarrage de l'application les salles en vert
            View[] views2 = { imgv4, imgv5 };
            for (View view : views2) {
                view.setVisibility(View.VISIBLE);
            }

            // Rétablit les labels indiquant que les salles sont libres
            tv3.setText(getString(R.string.estLibre1_label));
            tv4.setText(getString(R.string.estLibre2_label));

            // Remet à zéro le nombre d'usagers dans chaque salle
            Salle.compteurUsagersSalle1 = 0;
            Salle.compteurUsagersSalle2 = 0;
        }
    }

    private void fixerPositionsSalles() {
            // Mes coordonnées géographiques en temps réel récupérés à un temps t lorsqu'on appuie sur le bouton
            final double latitudeTempsTConstant = Localisation.latitudeTempsT;
            final double longitudeTempsTConstant = Localisation.longitudeTempsT;

            // Les coordonnées géographiques de la Salle 1 sont fixés à ma position lorsque j'appuie sur le bouton
            Salle.latitudeCentreSalleUneDynamique=latitudeTempsTConstant;
            Salle.longitudeCentreSalleUneDynamique=longitudeTempsTConstant;

            // Les coordonnées géographiques de la Salle 2 sont fixés à ma position + 15 mètres vers l'Est lorsque j'appuie sur le bouton
            Salle.latitudeCentreSalleDeuxDynamique=latitudeTempsTConstant + Salle.quinzeMetresLatitude;
            Salle.longitudeCentreSalleDeuxDynamique=longitudeTempsTConstant + Salle.quinzeMetresLongitude;
    }

    public void regenererPositionsSalles(View view) {
        Localisation.valeursLongLatAttribuees = true;
    }

    /**
     * Met à jour les valeurs des champs de l'UI :
     * - La localisation (latitude, longitude) et temps de dernière localisation en temps réel.
     * - Nombres d'individus par salle
     * - Couleur des salles (rouge et vert)
     */
    private void mettreAjourUILocalisation() {
        if (mLocalisationActuelle != null) {

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- GESTION AFFICHAGE PROPRE A LA LOCALISATION AUTOMATIQUE DE L'UTILISATEUR -------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel, mLocalisationActuelle.getLatitude()));

            Localisation.updatetime = mLastUpdateTime;

            if (Localisation.valeursLongLatAttribuees) {
                fixerPositionsSalles();
                Localisation.valeursLongLatAttribuees = false;
            }

            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel, mLocalisationActuelle.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- STOCKAGE COORDONNES DES SALLES ------------------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            final double longitudeCentreSalleConstant1 = Salle.longitudeCentreSalleUneDynamique; // Centre salle (x) (Fixé à la même position que l'utilisateur)
            final double latitudeCentreSalleConstant1 = Salle.latitudeCentreSalleUneDynamique; // Centre salle (y) (Fixé à la même position que l'utilisateur)

            final double longitudeCentreSalleConstant2 = Salle.longitudeCentreSalleDeuxDynamique; // Centre salle (x) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en latitude)
            final double latitudeCentreSalleConstant2 = Salle.latitudeCentreSalleDeuxDynamique; // Centre salle (y) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en longitude)

            /* Pour éviter des problèmes de calcul de distance entre ma position (> 7 chiffres à la virgule)
            et celle d'une coordonnée géographique Google Maps (7 chiffres après la virgule) :
            - On réduit la précision de la géolocalisation à 7 chiffres après la virgules (précision acceptable).
            - Cette précision est obtenue en faisant une division de la coordonnée par un entier de 7 chiffres après la virgule,
              qui est ensuite divisée par un nombre décimal afin d'obtenir un chiffre à virgules (double).
             */
            // Réduit la précision de ma latitude
            double maLatitudeTresPrecise = mLocalisationActuelle.getLatitude();
            double maLatitudeMoinsPrecise = (int)(Math.round(maLatitudeTresPrecise * 10000000))/10000000.0; // Récupérer le résultat de ma localisation (latitude) 7 chiffres après la virgule
            Localisation.longitudeTempsT = maLatitudeMoinsPrecise;

            // Réduit la précision de ma longitude
            double maLongitudeTresPrecise = mLocalisationActuelle.getLongitude();
            double maLongitudeMoinsPrecise = (int)(Math.round(maLongitudeTresPrecise * 10000000))/10000000.0; // Récupérer le résultat de ma localisation (longitude) 7 chiffres après la virgule
            Localisation.latitudeTempsT = maLongitudeMoinsPrecise;

            // La simulation des salles est simplifiée en considérant la forme de celles-ci comme des cercles plutôt que des rectangles ou des carrés
            int rayonSalle = 10; // Le rayon d'une salle en mètres

            // Le résultat des distances s'exprime sous la forme d'un tableau de float
            float[] distanceSalle1 = new float[1];
            float[] distanceSalle2 = new float[1];

            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- GESTION AFFICHAGE PROPRE A LA SALLE 1 -----------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            // On récupère la longitude de la Salle 1 qui a été définie en appuyant sur le bouton 'fixerPositionsSalles'
            TextView tv1 = findViewById(R.id.longitudeValeurSalle1);
            tv1.setText(String.valueOf(longitudeCentreSalleConstant1));

            // On récupère la latitude de la Salle 2 qui a été définie en appuyant sur le bouton 'fixerPositionsSalles'
            TextView tv2 = findViewById(R.id.latitudeValeurSalle1);
            tv2.setText(String.valueOf(latitudeCentreSalleConstant1));

            // Calcul de distance entre ma localisation et celle du centre de la salle 1
            Location.distanceBetween(
                    maLongitudeMoinsPrecise, // Latitude de départ (Moi)
                    maLatitudeMoinsPrecise, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant1, // Latitude de fin (salle1)
                    longitudeCentreSalleConstant1, // Longitude de fin (salle1)
                    distanceSalle1); // Résultat = distance entre deux coordonnées géographiques

            TextView tv8 = findViewById(R.id.maDistanceSalle1);
            tv8.setText("Je suis à environ " + distanceSalle1[0]/2 + " mètres du centre de celle-ci");

            // Si la distance entre ma localisation et celle du centre de la Salle 1 est inférieure au rayon de la Salle 1... alors je me trouve dans cette salle
            if (distanceSalle1[0] < rayonSalle) {
                // On récupère le nombre d'usagers actuels générés aléatoirement via le bouton 'genererNombreUsagesParSalle'
                Salle.compteurUsagersSalle1 = Salle.randomUsagersSalle1;

                // Je me trouve dans cette salle, on augmente donc le nombre d'usagers actuels de 1
                Salle.compteurUsagersSalle1++;
                TextView tv0 = findViewById(R.id.nombreUsagersActuelSalle1);
                tv0.setText(String.valueOf(Salle.compteurUsagersSalle1));

                // La limite maximale du nombre d'usagers que peut contenir cette salle
                Integer limiteNombreUsagersSalle1 = 10;

                // Si le nombre d'usagers actuels dans la salle 1 a dépassé son quota maximal
                if (Salle.compteurUsagersSalle1 > limiteNombreUsagersSalle1) {
                    // On affiche la salle en rouge
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.VISIBLE);
                    // On l'empêche de devenir verte
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.INVISIBLE);
                    // On indique que la salle est indisponible
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    tv3.setText(getString(R.string.estPasLibre1_label));
                }
                // Si le nombre d'usagers actuels dans la salle 1 est inférieur au quota maximal
                if (Salle.compteurUsagersSalle1 <= limiteNombreUsagersSalle1) {
                    // On affiche la salle en vert
                    ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                    imgv1.setVisibility(View.VISIBLE);
                    // On l'empêche de devenir rouge
                    ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                    imgv2.setVisibility(View.INVISIBLE);
                    // On indique que la salle est libre
                    TextView tv3 = findViewById(R.id.librepaslibre1);
                    tv3.setText(getString(R.string.estLibre1_label));
                }

                // On rend visible l'icône de ma localisation
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.VISIBLE);

                // On indique que je suis dans la Salle 1 (et donc pas dans la Salle 2)
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                tv4.setText(getString(R.string.dansSalle1_label));
                Localisation.estDansSalle1 = true;
                Localisation.estDansSalle2 = false;
            }
            // Si la distance entre ma localisation et celle du centre de la Salle 1 est supérieure au rayon de la Salle 1... alors je ne me trouve PAS dans cette salle
            if (distanceSalle1[0] > rayonSalle) {
                // On affiche la salle en vert (elle est à nouveau disponible)
                ImageView imgv1 = findViewById(R.id.imageViewSalleVerte1);
                imgv1.setVisibility(View.VISIBLE);
                // On l'empêche de devenir rouge
                ImageView imgv2 = findViewById(R.id.imageViewSalleRouge1);
                imgv2.setVisibility(View.INVISIBLE);
                // On masque l'icône de ma localisation
                ImageView imgv3 = findViewById(R.id.moi1);
                imgv3.setVisibility(View.INVISIBLE);
                // On indique que je ne suis PAS dans la Salle 1
                TextView tv4 = findViewById(R.id.mapositionsalle1);
                tv4.setText(getString(R.string.pasSalle1_label));
                Localisation.estDansSalle1 = false;
            }


            /* -------------------------------------------------------------------------------------------------------------------------------------*/
            /* ----------------------------------------- GESTION AFFICHAGE PROPRE A LA SALLE 2 -----------------------------------------------------*/
            /* -------------------------------------------------------------------------------------------------------------------------------------*/

            // On récupère la longitude de la Salle 2 qui a été définie en appuyant sur le bouton 'fixerPositionsSalles'
            TextView tv3 = findViewById(R.id.longitudeValeurSalle2);
            tv3.setText(String.valueOf(longitudeCentreSalleConstant2));

            // On récupère la latitude de la Salle 2 qui a été définie en appuyant sur le bouton 'fixerPositionsSalles'
            TextView tv4 = findViewById(R.id.latitudeValeurSalle2);
            tv4.setText(String.valueOf(latitudeCentreSalleConstant2));

            // Calcul de distance entre ma localisation et celle du centre de la salle 2
            Location.distanceBetween(
                    maLongitudeMoinsPrecise, // Latitude de départ (Moi)
                    maLatitudeMoinsPrecise, // Longitude de départ (Moi)
                    latitudeCentreSalleConstant2, // Latitude de fin (salle2)
                    longitudeCentreSalleConstant2, // Longitude de fin (salle2)
                    distanceSalle2); // Résultat = distance entre deux coordonnées géographiques

            TextView tv7 = findViewById(R.id.maDistanceSalle2);
            tv7.setText("Je suis à environ " + distanceSalle2[0]/2 + " mètres du centre de celle-ci");

            // Si la distance entre ma localisation et celle du centre de la Salle 2 est inférieure au rayon de la Salle 2... alors je me trouve dans cette salle
            if (distanceSalle2[0] < rayonSalle) {
                // On récupère le nombre d'usagers actuels générés aléatoirement via le bouton 'genererNombreUsagesParSalle'
                Salle.compteurUsagersSalle2 = Salle.randomUsagersSalle2;

                // Je me trouve dans cette salle, on augmente donc le nombre d'usagers actuels de 1
                Salle.compteurUsagersSalle2++;
                TextView tv0 = findViewById(R.id.nombreUsagersActuelSalle2);
                tv0.setText(String.valueOf(Salle.compteurUsagersSalle2));

                // La limite maximale du nombre d'usagers que peut contenir cette salle
                Integer limiteNombreUsagersSalle2 = 10;

                // Si le nombre d'usagers actuels dans la salle 2 a dépassé son quota maximal
                if (Salle.compteurUsagersSalle2 > limiteNombreUsagersSalle2) {
                    // On affiche la salle en rouge
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.VISIBLE);
                    // On l'empêche de devenir verte
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.INVISIBLE);
                    // On indique que la salle est indisponible
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText(getString(R.string.estPasLibre2_label));
                }
                // Si le nombre d'usagers actuels dans la salle 2 est inférieur au quota maximal
                if (Salle.compteurUsagersSalle2 <= limiteNombreUsagersSalle2) {
                    // On affiche la salle en vert
                    ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                    imgv2.setVisibility(View.VISIBLE);
                    // On l'empêche de devenir rouge
                    ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                    imgv3.setVisibility(View.INVISIBLE);
                    // On indique que la salle est libre
                    TextView tv5 = findViewById(R.id.librepaslibre2);
                    tv5.setText(getString(R.string.estLibre2_label));
                }

                // On rend visible l'icône de ma localisation
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.VISIBLE);

                // On indique que je suis dans la Salle 2 (et donc pas dans la Salle 1)
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText(getString(R.string.dansSalle2_label));
                Localisation.estDansSalle2 = true;
                Localisation.estDansSalle1 = false;
            }
            // Si la distance entre ma localisation et celle du centre de la Salle 2 est supérieure au rayon de la Salle 2... alors je ne me trouve PAS dans cette salle
            if (distanceSalle2[0] > rayonSalle) {
                // On affiche la salle en vert (elle est à nouveau disponible)
                ImageView imgv2 = findViewById(R.id.imageViewSalleVerte2);
                imgv2.setVisibility(View.VISIBLE);
                // On l'empêche de devenir rouge
                ImageView imgv3 = findViewById(R.id.imageViewSalleRouge2);
                imgv3.setVisibility(View.INVISIBLE);
                // On masque l'icône de ma localisation
                ImageView imgv4 = findViewById(R.id.moi2);
                imgv4.setVisibility(View.INVISIBLE);
                // On indique que je ne suis PAS dans la salle 2
                TextView tv6 = findViewById(R.id.mapositionsalle2);
                tv6.setText(getString(R.string.pasSalle2_label));
                Localisation.estDansSalle2 = false;
            }

        }
    }

    public void genererNombreUsagesParSalle(View view) {
        // Génère deux nombres compris entre 1 et 10 pour les affecter à des variables propres à la salle 1 et à la salle 2
        Salle.randomUsagersSalle1 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        Salle.randomUsagersSalle2 = new Random().nextInt(10) + 1; // [0, 1] + 1 => [1, 2] : Minimum 1 (si [0] + 1) et maximum 10 (si [1] + 1)
        
        // Délègue l'affichage du nombre obtenu pour la salle 1
        TextView tv1 = findViewById(R.id.nombreUsagersActuelSalle1);
        tv1.setText(String.valueOf(Salle.randomUsagersSalle1));

        // Délègue l'affichage du nombre obtenu pour la salle 2
        TextView tv2 = findViewById(R.id.nombreUsagersActuelSalle2);
        tv2.setText(String.valueOf(Salle.randomUsagersSalle2));
    }

    /**
     * Enlève les mises à jour de localisation pour l'API 'FusedLocationApi'.
     */
    private void arreterLocalisation() {
        if (!mDemandeMiseAJourLocalisation) {
            Log.d(TAG, "arreterLocalisation: mises à jour jamais demandées, pas de résultat.");
            return;
        }

        // C'est une bonne pratique que d'enlever les demandes de localisation quand l'activité est en pause ou
        // à l'état d'arrêt. Cela permet d'augmenter la performance de la batterie et cela est particulièrement recommandé
        // pour les applications qui demandent des mises à jour de localisation fréquentes.
        mFusedLocationClient.removeLocationUpdates(mAppelRetourLocalisation)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mDemandeMiseAJourLocalisation = false;
                        definirEtatBoutons();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mDemandeMiseAJourLocalisation && verifierPermissions()) {
            commencerLocalisation();
        } else if (!verifierPermissions()) {
            demanderPermissions();
        }

        mettreAjourUI();
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
        savedInstanceState.putBoolean(KEY_DEMANDER_MISE_A_JOUR_LOCALISATION, mDemandeMiseAJourLocalisation);
        savedInstanceState.putParcelable(KEY_LOCALISATION, mLocalisationActuelle);
        savedInstanceState.putString(KEY_DERNIER_MISE_A_JOUR_TEMPS_STRING, mLastUpdateTime);
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
                                    DEMANDE_PERMISSIONS_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Demande de permission");
            // Demande de permission. Il est possible que la réponse se fasse seule si les politiques de l'appareil
            // mettent la permission dans un état donné ou que l'utilisateur a refusé la permission
            // précédente et a coché la case "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    DEMANDE_PERMISSIONS_CODE);
        }
    }

    /**
     * Appel de retour reçu quand une demande de permission a été achevée.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == DEMANDE_PERMISSIONS_CODE) {
            if (grantResults.length <= 0) {
                // Si l'interaction avec l'utilisateur est interrompue, la demande de permission est stoppée
                // et vous allez recevoir des valeurs nulles.
                Log.i(TAG, "L'interaction avec l'utilisateur a été annulé.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mDemandeMiseAJourLocalisation) {
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
