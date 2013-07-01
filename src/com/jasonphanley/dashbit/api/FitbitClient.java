package com.jasonphanley.dashbit.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FitbitClient {
    
    static final String FITBIT_API_US_UNITS = "en_US";
    
    private static final String FITBIT_API_ACTIVITIES_URL = "http://api.fitbit.com/1/user/-/activities/date/%s.json";
    
    private static final String FITBIT_API_PROFILE_URL = "http://api.fitbit.com/1/user/-/profile.json";
    
    private static final String PREF_REQUEST_TOKEN = "request_token";
    
    private static final String PREF_REQUEST_SECRET = "request_secret";
    
    private static final String PREF_ACCESS_TOKEN = "access_token";
    
    private static final String PREF_ACCESS_SECRET = "access_secret";
    
    private final OAuthService service;
    
    private final SharedPreferences preferences;
    
    private Token requestToken;
    
    private Token accessToken;
    
    public FitbitClient(Context context, String apiKey, String apiSecret,
            String callback) {
        service = createAuthService(context, apiKey, apiSecret, callback);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        restoreRequestToken();
        restoreAccessToken();
    }
    
    private static OAuthService createAuthService(Context context,
            String apiKey, String apiSecret, String callback) {
        return new ServiceBuilder()
                .provider(FitbitApi.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback(callback)
                .build();
    }
    
    public Token fetchRequestToken() {
        return service.getRequestToken();
    }
    
    public Token getRequestToken() {
        return requestToken;
    }
    
    public void setRequestToken(Token requestToken) {
        this.requestToken = requestToken;
        
        preferences.edit()
                .putString(PREF_REQUEST_TOKEN,
                        requestToken != null ? requestToken.getToken() : null)
                .putString(PREF_REQUEST_SECRET,
                        requestToken != null ? requestToken.getSecret() : null)
                .commit();
    }
    
    private void restoreRequestToken() {
        String token = preferences.getString(PREF_REQUEST_TOKEN, null);
        String secret = preferences.getString(PREF_REQUEST_SECRET, null);
        
        if (token != null && secret != null) {
            requestToken = new Token(token, secret);
        } else {
            requestToken = null;
        }
    }
    
    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl(requestToken);
    }
    
    public Token fetchAccessToken(Verifier verifier) {
        return service.getAccessToken(requestToken, verifier);
    }
    
    public Token getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken;
        
        preferences.edit()
                .putString(PREF_ACCESS_TOKEN,
                        accessToken != null ? accessToken.getToken() : null)
                .putString(PREF_ACCESS_SECRET,
                        accessToken != null ? accessToken.getSecret() : null)
                .commit();
    }
    
    private void restoreAccessToken() {
        accessToken = loadAccessToken(preferences);
    }
    
    private static Token loadAccessToken(SharedPreferences preferences) {
        String token = preferences.getString(PREF_ACCESS_TOKEN, null);
        String secret = preferences.getString(PREF_ACCESS_SECRET, null);
        
        if (token != null && secret != null) {
            return new Token(token, secret);
        } else {
            return null;
        }
    }
    
    public boolean isAuthenticated() {
        return accessToken != null;
    }
    
    public static boolean isAuthenticated(Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return loadAccessToken(preferences) != null;
    }
    
    public Response fetchProfile() {
        return fetch(FITBIT_API_PROFILE_URL);
    }
    
    public Response fetchActivities(String units) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
        String url = String.format(Locale.US, FITBIT_API_ACTIVITIES_URL, date);
        String acceptLanguage = FITBIT_API_US_UNITS.equals(units) ? FITBIT_API_US_UNITS
                : null;
        return fetch(url, acceptLanguage);
    }
    
    private Response fetch(String url) {
        return fetch(url, null);
    }
    
    private Response fetch(String url, String acceptLanguage) {
        OAuthRequest request = new OAuthRequest(Verb.GET, url);
        if (acceptLanguage != null) {
            request.addHeader("Accept-Language", acceptLanguage);
        }
        if (accessToken != null) {
            service.signRequest(accessToken, request);
        }
        return request.send();
    }
    
}