package com.jasonphanley.dashbit.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.jasonphanley.dashbit.R;
import com.jasonphanley.dashbit.api.FitbitClient;

public class PrefsFragment extends PreferenceFragment {
    
    private static final String PREF_AUTH = "auth";
    
    private static final String PREF_STATUS_TYPE = "status_type";
    
    private static final String PREF_STATUS_TYPE_DEFAULT = "steps";
    
    private Preference authPreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initPrefs();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        bindAuthPreference();
    }
    
    private void initPrefs() {
        addPreferencesFromResource(R.xml.prefs);
        
        authPreference = findPreference(PREF_AUTH);
        
        bindPreference(findPreference(PREF_STATUS_TYPE));
    }
    
    private void bindAuthPreference() {
        boolean authed = FitbitClient.isAuthenticated(getActivity());
        
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (authed) {
            preferenceScreen.removePreference(authPreference);
        } else {
            preferenceScreen.addPreference(authPreference);
        }
        
        findPreference(PREF_STATUS_TYPE).setEnabled(authed);
    }
    
    private static void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceListener);
        
        bindPreferenceListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }
    
    private static Preference.OnPreferenceChangeListener bindPreferenceListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
            
            return true;
        }
    };
    
    public static String getStatusType(Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(PREF_STATUS_TYPE,
                PREF_STATUS_TYPE_DEFAULT);
    }
    
}