package com.google.android.gms.location.sample.covifacgps;

public class Localisation {
    // Mes coordonnées géographiques (latitude et longitude) en temps réel
    public static double latitudeTempsT;
    public static double longitudeTempsT;

    // Initialise le champs du temps de mise à jour de la localisation comme un String vide
    public static String updatetime = "";

    // Permet de rédéclencher la méthode 'fixerPositionsSalles' entre chaque mise à jour de localisation en appuyant sur le bouton 'regenererPositionsSalles'
    public static boolean valeursLongLatAttribuees = true;

    // Identifie si je suis dans la Salle 1 ou la Salle 2
    public static boolean estDansSalle1 = false;
    public static boolean estDansSalle2 = false;

}