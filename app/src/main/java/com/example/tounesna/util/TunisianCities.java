package com.example.tounesna.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class containing Tunisian governorates/cities
 */
public class TunisianCities {
    
    public static final List<String> CITIES = Arrays.asList(
        "Ariana",
        "Ben Arous",
        "Manouba",
        "Nabeul",
        "Zaghouan",
        "Bizerte",
        "Béja",
        "Jendouba",
        "Le Kef",
        "Siliana",
        "Sousse",
        "Monastir",
        "Mahdia",
        "Kairouan",
        "Kasserine",
        "Sidi Bouzid",
        "Gafsa",
        "Tozeur",
        "Kebili",
        "Gabès",
        "Medenine",
        "Tataouine",
        "Sfax"
    );
    
    public static List<String> getCities() {
        return CITIES;
    }
    
    public static String[] getCitiesArray() {
        return CITIES.toArray(new String[0]);
    }
}
