package com.example.brailleradar;

import android.app.Activity;
import android.content.Context;

public class LocationObjectsSingleton {
    private static GPS g = null;
    private static Scanner s = null;
    private static Compass c = null;

    private static boolean created = false;

    public static boolean init(Activity activity, Context context) {
        if (!created) {
            g = new GPS(activity);
            s = new Scanner(activity);
            c = new Compass(context);
            created = true;
        } else {
            return false;
        }
        return true;
    }


    public static GPS getGPS() {
        return g;
    }
    public static Scanner getScanner() {
        return s;
    }
    public static Compass getCompass() {
        return c;
    }

}
