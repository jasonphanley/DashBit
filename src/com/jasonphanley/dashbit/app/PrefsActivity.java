package com.jasonphanley.dashbit.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class PrefsActivity extends PreferenceActivity {
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initActionBar();
        
        initPrefs();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void initActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void initPrefs() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }
    
}