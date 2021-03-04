package com.google.android.gms.location.sample.locationupdates;

import com.google.android.gms.maps.model.LatLng;

public class Modele {
    public static double latitudeTempsT; // Ma position (x)
    public static double longitudeTempsT; // Ma position (y)

    public static boolean valeursLongLatAttribuees = true;
    public static boolean estDansSalle1 = true;
    public static boolean estDansSalle2 = true;

    public static String updatetime = "";

    public static double latitudeCentreSalleUneDynamique; // Centre salle1 (x) (Fixé à la même position que l'utilisateur)
    public static double longitudeCentreSalleUneDynamique; // Centre salle1 (y) (Fixé à la même position que l'utilisateur)

    public static double quinzeMetresLatitude = 0.00013064998; // 15m = 0.00013064998 ; 10 m = 0.00008709999 ; 5 m = 0.00004354999 ; 1 m = 0.00000870999 -> Convertir : diviser différence latitude / 0.00000870999
    public static double quinzeMetresLongitude = -0.00003315; // 15m = -0.00003315 ; 10 m = -0.0000221 ; 5 m = -0.00001105 ; 1 m = -0.00000221 -> Convertir : diviser différence longitude / -0.00000221

    public static double latitudeCentreSalleDeuxDynamique; // Centre salle2 (x) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en latitude)
    public static double longitudeCentreSalleDeuxDynamique; // Centre salle2 (y) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en longitude)

    public static Integer randomPersonnesSalle1 = 0; // nombre aléatoire permettant de déterminer le nombre de personnes dans la salle 1
    public static Integer randomPersonnesSalle2 = 0; // nombre aléatoire permettant de déterminer le nombre de personnes dans la salle 1
    public static Integer compteurEleveSalle1 = 0;
    public static Integer compteurEleveSalle2 = 0;
}