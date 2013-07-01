package com.jasonphanley.dashbit.api;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class FitbitApi extends DefaultApi10a {
    
    private static final String REQUEST_TOKEN_URL = "http://api.fitbit.com/oauth/request_token";
    
    private static final String ACCESS_TOKEN_URL = "http://api.fitbit.com/oauth/access_token";
    
    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authenticate?oauth_token=%s";
    
    @Override
    public String getAccessTokenEndpoint() {
        return ACCESS_TOKEN_URL;
    }
    
    @Override
    public String getRequestTokenEndpoint() {
        return REQUEST_TOKEN_URL;
    }
    
    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(AUTHORIZE_URL, requestToken.getToken());
    }
    
}