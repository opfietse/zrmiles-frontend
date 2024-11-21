package net.opfietse.zrmiles;

import static net.opfietse.zrmiles.Constants.DATE_FORMAT_AMERICAN;
import static net.opfietse.zrmiles.Constants.DATE_FORMAT_EUROPEAN;
import static net.opfietse.zrmiles.Constants.DATE_PREFERENCE_AMERICAN;
import static net.opfietse.zrmiles.Constants.DATE_PREFERENCE_EUROPEAN;
import static net.opfietse.zrmiles.Constants.DISTANCE_UNIT_KILOMETERS;
import static net.opfietse.zrmiles.Constants.DISTANCE_UNIT_MILES;

public class CookieStuff {
    public static boolean preferenceIsKilometers(String preferenceCookieValue) {
        return preferenceCookieValue != null && preferenceCookieValue.contains(DISTANCE_UNIT_KILOMETERS);
    }

    public static boolean preferenceIsMiles(String preferenceCookieValue) {
        return !preferenceIsKilometers(preferenceCookieValue);
    }

    public static boolean preferenceIsEuropeanDate(String preferenceCookieValue) {
        return preferenceCookieValue != null && preferenceCookieValue.contains(DATE_PREFERENCE_EUROPEAN);
    }

    public static boolean preferenceIsAmericanDate(String preferenceCookieValue) {
        return !preferenceIsEuropeanDate(preferenceCookieValue);
    }

    public static String getOdometerPreference(String preferenceCookieValue) {
        return preferenceCookieValue == null ? DISTANCE_UNIT_MILES : (preferenceCookieValue.contains(DISTANCE_UNIT_MILES) ? DISTANCE_UNIT_MILES : DISTANCE_UNIT_KILOMETERS);
    }

    public static String getDatePreference(String preferenceCookieValue) {
        return preferenceIsAmericanDate(preferenceCookieValue) ? DATE_PREFERENCE_AMERICAN : DATE_PREFERENCE_EUROPEAN;
    }

    public static String getDatePreferenceFormat(String preferenceCookieValue) {
        return preferenceIsAmericanDate(preferenceCookieValue) ? DATE_FORMAT_AMERICAN : DATE_FORMAT_EUROPEAN;
    }
}
