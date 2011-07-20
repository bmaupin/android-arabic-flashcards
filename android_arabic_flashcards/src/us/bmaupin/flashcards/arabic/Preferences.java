package us.bmaupin.flashcards.arabic;

// $Id$

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private String profileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Preferences", "onCreate called");
        super.onCreate(savedInstanceState);
        
        // get the profile name from the extra data in the intent
        profileName = getIntent().getExtras().getString(ArabicFlashcards.EXTRA_PROFILE_NAME);
        // set the preferences file to one based on the profile name
        getPreferenceManager().setSharedPreferencesName(profileName);
        // get the default preferences from XML
        addPreferencesFromResource(R.xml.preferences);
        // determine whether default card order preference should be enabled
        toggleDefaultCardOrder();
        // set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
