package com.jasonphanley.dashbit.app;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Response;

import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.jasonphanley.dashbit.R;
import com.jasonphanley.dashbit.api.FitbitClient;
import com.jasonphanley.dashbit.api.FitbitUtil;
import com.jasonphanley.dashbit.model.Activities;
import com.jasonphanley.dashbit.model.Units;

public class FitbitExtension extends DashClockExtension {
    
    private static final String FITBIT_PACKAGE = "com.fitbit.FitbitMobile";
    
    private static final String FITBIT_URL = "http://www.fitbit.com/";
    
    private FitbitClient fitbit;
    
    @Override
    protected void onUpdateData(int reason) {
        fitbit = FitbitUtil.createClient(this);
        
        if (!fitbit.isAuthenticated() || !isNetworkConnected()) {
            return;
        }
        
        Activities activities = fetchData();
        if (activities != null) {
            publishUpdate(activities);
        } else {
            publishErrorUpdate();
        }
    }
    
    private boolean isNetworkConnected() {
        NetworkInfo ni = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
    
    private Activities fetchData() {
        return fetchActivities(fetchDistanceUnit());
    }
    
    private Units fetchDistanceUnit() {
        Response response = fitbit.fetchProfile();
        if (response.isSuccessful()) {
            return parseDistanceUnit(response.getBody());
        } else {
            Log.w(App.TAG, "Error fetching profile: " + response.getMessage()
                    + " (" + response.getCode() + ")");
            return null;
        }
    }
    
    private Units parseDistanceUnit(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject summary = json.getJSONObject("user");
            String distanceUnit = summary.getString("distanceUnit");
            return FitbitUtil.getUnits(distanceUnit);
        } catch (JSONException e) {
            Log.w(App.TAG, "Error parsing profile", e);
            return null;
        }
    }
    
    private Activities fetchActivities(Units distanceUnit) {
        Response response = fitbit.fetchActivities(
                FitbitUtil.getUnits(distanceUnit));
        if (response.isSuccessful()) {
            return parseActivities(response.getBody(), distanceUnit);
        } else {
            Log.w(App.TAG, "Error fetching data: " + response.getMessage()
                    + " (" + response.getCode() + ")");
            if (response.getCode() == 401) {
                fitbit.setAccessToken(null);
            }
            return null;
        }
    }
    
    private Activities parseActivities(String jsonString, Units distanceUnit) {
        int steps;
        int floors;
        float distance;
        int calories;
        
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONObject summary = json.getJSONObject("summary");
            steps = summary.getInt("steps");
            floors = summary.optInt("floors", -1);
            distance = parseDistance(summary);
            calories = summary.getInt("caloriesOut");
        } catch (JSONException e) {
            Log.w(App.TAG, "Error parsing data", e);
            return null;
        }
        
        return new Activities(steps, floors, distance, calories, distanceUnit);
    }
    
    private static float parseDistance(JSONObject summary) throws JSONException {
        JSONArray distances = summary.getJSONArray("distances");
        for (int i = 0; i < distances.length(); i++) {
            JSONObject distance = distances.getJSONObject(i);
            if ("total".equals(distance.getString("activity"))) {
                return (float) distance.getDouble("distance");
            }
        }
        
        return 0;
    }
    
    private void publishUpdate(Activities activities) {
        boolean hasFloors = activities.floors >= 0;
        
        String statusType = PrefsFragment.getStatusType(this);
        String status;
        if ("floors".equals(statusType)) {
            status = hasFloors ? Integer.toString(activities.floors)
                    : getString(R.string.dashclock_status_none);
        } else if ("distance".equals(statusType)) {
            status = formatDistance(activities.distance);
        } else if ("calories".equals(statusType)) {
            status = Integer.toString(activities.calories);
        } else {
            status = Integer.toString(activities.steps);
        }
        
        Resources resources = getResources();
        String stepsString = resources.getQuantityString(
                R.plurals.dashclock_steps, activities.steps, activities.steps);
        String floorsString = resources.getQuantityString(
                R.plurals.dashclock_floors, activities.floors, activities.floors);
        String distanceString = formatDistance(resources, activities.distance,
                activities.units);
        String caloriesString = resources.getQuantityString(
                R.plurals.dashclock_calories, activities.calories,
                activities.calories);
        
        String expandedTitle = getString(R.string.dashclock_expanded_title,
                stepsString);
        int expandedBodyString = hasFloors
                ? R.string.dashclock_expanded_body
                : R.string.dashclock_expanded_body_no_floors;
        String expandedBody = getString(expandedBodyString, floorsString,
                distanceString, caloriesString);
        
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_launcher_fitbit)
                .status(status)
                .expandedTitle(expandedTitle)
                .expandedBody(expandedBody)
                .clickIntent(getFitbitLaunchIntent()));
    }
    
    private void publishErrorUpdate() {
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_launcher_fitbit)
                .status(getString(R.string.dashclock_status_none))
                .expandedBody(getString(R.string.dashclock_expanded_body_error))
                .clickIntent(getFitbitLaunchIntent()));
    }
    
    private static String formatDistance(float distance) {
        return new DecimalFormat("#.##").format(distance);
    }
    
    private static String formatDistance(Resources resources, float distance,
            Units units) {
        int quantity = distance < 1 ? 0 : (int) Math.ceil(distance);
        String distanceString = formatDistance(distance);
        
        if (units == Units.US) {
            return resources.getQuantityString(
                    R.plurals.dashclock_distance_miles, quantity,
                    distanceString);
        } else {
            return resources.getQuantityString(
                    R.plurals.dashclock_distance_kilometers, quantity,
                    distanceString);
        }
    }
    
    private Intent getFitbitLaunchIntent() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(
                FITBIT_PACKAGE);
        
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(FITBIT_URL));
        }
        
        return intent;
    }
    
}