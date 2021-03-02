package com.google.android.gms.location.sample.locationupdates;

import com.google.android.gms.maps.model.LatLng;

public class Modele {
    public static double latitude = 0.0;
    public static double longitude = 0.0;
    public static String updatetime = "";
    public static LatLng maPosition;
    public static double latitudeTempsT; // Ma position (x)
    public static double longitudeTempsT; // Ma position (y)
    public static double latitudeCentreSalle=44.837788; // Centre salle (x)
    public static double longitudeCentreSalle=-0.579178; // Centre salle (x)
    public static Integer randomPersonnesParSalle = 0; // nombre aléatoire permettant de déterminer le nombre de personnes par salle
}