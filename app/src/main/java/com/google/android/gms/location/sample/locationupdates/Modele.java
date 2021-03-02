package com.google.android.gms.location.sample.locationupdates;

import com.google.android.gms.maps.model.LatLng;

public class Modele {
    public static boolean valeursLongLatAttribuees = true;
    public static double dixMetresLatitude = 0.00008709999;
    public static double dixMetresLongitude = -0.0000221;
    public static String updatetime = "";
    public static LatLng maPosition;
    public static double latitudeTempsT; // Ma position (x)
    public static double longitudeTempsT; // Ma position (y)
    public static double latitudeCentreSalleDynamique; // Centre salle (x) (Calculé à partir de la coordonnée de l'utilisateur + 10 mètres converti en une distance en latitude)
    public static double longitudeCentreSalleDynamique; // Centre salle (y) (Calculé à partir de la coordonnée de l'utilisateur + 10 mètres converti en une distance en longitude)
    public static Integer randomPersonnesParSalle = 0; // nombre aléatoire permettant de déterminer le nombre de personnes par salle
}