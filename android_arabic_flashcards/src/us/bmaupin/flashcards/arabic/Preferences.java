package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity 
                                implements OnSharedPreferenceChangeListener {
    private static final int DIALOG_CONFIRM_DELETE_PROFILE = 0;
    static final String EXTRA_PROFILE_ACTION = 
            "android.intent.extra.PROFILE_ACTION";
    
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
        
        // handle when the delete profile option gets clicked
        Preference deleteProfile = findPreference(getString(
                R.string.preferences_delete_profile));
        deleteProfile.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference p) {
                showDialog(DIALOG_CONFIRM_DELETE_PROFILE);
                return true;
            }
        });
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
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_DELETE_PROFILE:
                return createConfirmDeleteProfileDialog(); 
        }
        return null;
    }
    
    private Dialog createConfirmDeleteProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will delete your profile containing all " +
        		"cards marked as known and unkown.  Are you sure?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Intent result = new Intent();
                       result.putExtra(EXTRA_PROFILE_ACTION, "delete");
                       setResult(RESULT_OK, result);
                       // we actually delete the profile in the main activity,
                       // but announcing it there might be confusing
                       Toast.makeText(getApplicationContext(), 
                               "Profile deleted!", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        AlertDialog ad = builder.create();
        return ad;
    }
}
