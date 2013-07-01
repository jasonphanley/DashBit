package com.jasonphanley.dashbit.api;

import com.jasonphanley.dashbit.R;
import com.jasonphanley.dashbit.model.Units;

import android.content.Context;

public class FitbitUtil {
    
    private static final String AUTH_CALLBACK_URL = "jph-dashbit://auth";
    
    public static FitbitClient createClient(Context context) {
        return new FitbitClient(context,
                context.getString(R.string.fitbit_api_key),
                context.getString(R.string.fitbit_api_secret),
                AUTH_CALLBACK_URL);
    }
    
    public static Units getUnits(String units) {
        return FitbitClient.FITBIT_API_US_UNITS.equals(units)
                ? Units.US : Units.METRIC;
    }
    
    public static String getUnits(Units units) {
        return units == Units.US ? FitbitClient.FITBIT_API_US_UNITS : null;
    }
    
}