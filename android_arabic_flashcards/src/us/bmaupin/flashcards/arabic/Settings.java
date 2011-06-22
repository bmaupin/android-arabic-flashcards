package us.bmaupin.flashcards.arabic;

// $Id$

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
    private String profileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            // get the profile name from the intent's bundle
            profileName = savedInstanceState.getString(ArabicFlashcards.EXTRA_PROFILE_NAME);
        }
        
        // set the preferences file to one based on the profile name
        getPreferenceManager().setSharedPreferencesName(profileName);
        // get the default settings from XML
        addPreferencesFromResource(R.xml.settings);
    }
    
/*    
    public static String getDefaultLang(Context context, String profileName) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("defaultLang", "arabic");
    }
    
    public static boolean getAskCardOrder(Context context, String profileName) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("askCardOrder", false);
    }
*/
    
/*    
    void setProfile(String profileName) {
        pm.setSharedPreferencesName(profileName);
    }
*/
    
/*
    public String getDefaultLang(Context context, profileNmae) {
        return pm.getSharedPreferences().getString("defaultLang", "arabic");
    }

    public boolean getAskCardOrder(Context context) {
        return pm.getSharedPreferences().getBoolean("askCardOrder", false);
    }
*/
    
/*
    public static String getDefaultLang(Context context) {
        return pm.getSharedPreferences().getString("defaultLang", "arabic");
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("defaultLang", "arabic");
    }
    
    public static boolean getAskCardOrder(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("askCardOrder", false);
    }
*/
}
