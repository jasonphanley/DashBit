package com.jasonphanley.dashbit.app;

import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jasonphanley.dashbit.R;
import com.jasonphanley.dashbit.api.FitbitClient;
import com.jasonphanley.dashbit.api.FitbitUtil;

public class AuthActivity extends Activity {
    
    private FitbitClient fitbit;
    
    private ProgressDialog progressDialog;
    
    private boolean inBrowserTask;
    
    @SuppressWarnings("rawtypes")
    private AsyncTask asyncTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fitbit = FitbitUtil.createClient(this);
        
        progressDialog = createProgressDialog();
        
        handleIntent(getIntent());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (asyncTask != null) {
            asyncTask.cancel(false);
        }
        
        progressDialog.dismiss();
    }
    
    private void handleIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && fitbit.getRequestToken() != null) {
            String verifierString = uri.getQueryParameter("oauth_verifier");
            if (verifierString != null) {
                onAuthorization(new Verifier(verifierString));
                return;
            }
        }
        
        if (asyncTask == null) {
            asyncTask = new RequestTokenTask().execute();
        }
    }
    
    @Override
    public void finish() {
        if (inBrowserTask) {
            Intent intent = new Intent(this, PrefsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        
        super.finish();
        
        if (inBrowserTask) {
            moveTaskToBack(true);
        }
    }
    
    private void onRequest(Token requestToken) {
        fitbit.setRequestToken(requestToken);
        
        startBrowserActivity();
        finish();
    }
    
    private void onRequestError() {
        showErrorToast();
        
        finish();
    }
    
    private void onAuthorization(Verifier verifier) {
        inBrowserTask = true;
        
        if (asyncTask == null) {
            asyncTask = new AccessTokenTask().execute(verifier);
        }
    }
    
    private void onAccess(Token accessToken) {
        fitbit.setAccessToken(accessToken);
        
        finish();
    }
    
    private void onAccessError() {
        showErrorToast();
        
        finish();
    }
    
    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.auth_loading));
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        return progressDialog;
    }
    
    private void showErrorToast() {
        Toast.makeText(this, R.string.auth_error, Toast.LENGTH_LONG).show();
    }
    
    private void startBrowserActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                fitbit.getAuthorizationUrl() + "&display=touch"));
        startActivity(intent);
    }
    
    private class RequestTokenTask extends AsyncTask<Void, Void, Token> {
        
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }
        
        @Override
        protected Token doInBackground(Void... params) {
            try {
                return fitbit.fetchRequestToken();
            } catch (OAuthException e) {
                Log.w(App.TAG, "Error getting request token", e);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Token requestToken) {
            asyncTask = null;
            
            progressDialog.dismiss();
            
            if (requestToken != null) {
                onRequest(requestToken);
            } else {
                onRequestError();
            }
        }
        
        @Override
        protected void onCancelled(Token requestToken) {
            asyncTask = null;
            
            progressDialog.dismiss();
        }
        
    }
    
    private class AccessTokenTask extends AsyncTask<Verifier, Void, Token> {
        
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }
        
        @Override
        protected Token doInBackground(Verifier... verifiers) {
            try {
                return fitbit.fetchAccessToken(verifiers[0]);
            } catch (OAuthException e) {
                Log.w(App.TAG, "Error getting access token", e);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Token accessToken) {
            asyncTask = null;
            
            progressDialog.dismiss();
            
            if (accessToken != null) {
                onAccess(accessToken);
            } else {
                onAccessError();
            }
        }
        
        @Override
        protected void onCancelled(Token accessToken) {
            asyncTask = null;
            
            progressDialog.dismiss();
        }
        
    }
    
}