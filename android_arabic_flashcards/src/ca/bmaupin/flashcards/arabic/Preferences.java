package ca.bmaupin.flashcards.arabic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class Preferences extends PreferenceActivity 
		implements OnSharedPreferenceChangeListener {
	private static final String TAG = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        
        // get the default preferences from XML
        addPreferencesFromResource(R.xml.preferences);
        // determine whether default card order preference should be enabled
        toggleDefaultCardOrder();
        // set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().
        		registerOnSharedPreferenceChangeListener(this);
        
        // handle when the about option gets clicked
        Preference about = findPreference(getString(
        		R.string.preferences_about));
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference p) {
            	startActivity(new Intent(Preferences.this, About.class));
                return true;
            }
        });
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, 
    		String key) {
        // when the ask card order preference value changes
        if (key.equals(getString(R.string.preferences_ask_card_order))) {
            toggleDefaultCardOrder();
        }
    }
    
    /*
     * disables default card order preference if ask card order is checked
     */
    private void toggleDefaultCardOrder() {
        CheckBoxPreference askCardOrder = (CheckBoxPreference)
            findPreference(getString(R.string.preferences_ask_card_order));
        Preference defaultCardOrder = 
            findPreference(getString(R.string.preferences_default_card_order));
        defaultCardOrder.setEnabled(!askCardOrder.isChecked());
    }
}
