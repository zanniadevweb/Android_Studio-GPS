package com.google.android.gms.location.sample.covifacgps;

public class Salle {
    // Latitude et longitude de la Salle 1
    public static double latitudeCentreSalleUneDynamique; // Centre salle1 (x) (Fixé à la même position que l'utilisateur)
    public static double longitudeCentreSalleUneDynamique; // Centre salle1 (y) (Fixé à la même position que l'utilisateur)

    // Permet de générer la salle 2 à 15 mètres de la première salle vers l'Est
    public static double quinzeMetresLatitude = 0.00013064998; // 15m = 0.00013064998 ; 10 m = 0.00008709999 ; 5 m = 0.00004354999 ; 1 m = 0.00000870999 -> Convertir : diviser différence latitude / 0.00000870999
    public static double quinzeMetresLongitude = -0.00003315; // 15m = -0.00003315 ; 10 m = -0.0000221 ; 5 m = -0.00001105 ; 1 m = -0.00000221 -> Convertir : diviser différence longitude / -0.00000221

    // Latitude et longitude de la Salle 2
    public static double latitudeCentreSalleDeuxDynamique; // Centre salle2 (x) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en latitude)
    public static double longitudeCentreSalleDeuxDynamique; // Centre salle2 (y) (Calculé à partir de la coordonnée de l'utilisateur + 15 mètres converti en une distance en longitude)

    // Nombre aléatoire (1-10) permettant de déterminer le nombre d'usagers dans les salles 1 et 2
    public static Integer randomUsagersSalle1 = 0;
    public static Integer randomUsagersSalle2 = 0;

    // Compteur du nombre d'usagers actuels dans les Salles 1 et 2
    public static Integer compteurUsagersSalle1 = 0;
    public static Integer compteurUsagersSalle2 = 0;
}