package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ShowStudySet extends Activity {
    private static final String TAG = "MyActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }
    
    /* Inflates the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /* Handles menu selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                startActivity(new Intent(this, Help.class));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
