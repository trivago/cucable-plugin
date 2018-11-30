package com.trivago.rta.logging;

public class Language {
    public static String singularPlural(final int value, final String singular, final String plural){
        if (value == 1){
            return singular;
        }
        return plural;
    }
}
