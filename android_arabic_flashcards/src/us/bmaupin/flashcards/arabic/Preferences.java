package us.bmaupin.flashcards.arabic;

// $Id$

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    private String profileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // get the profile name from the extra data in the intent
        profileName = getIntent().getExtras().getString(ArabicFlashcards.EXTRA_PROFILE_NAME);
        // set the preferences file to one based on the profile name
        getPreferenceManager().setSharedPreferencesName(profileName);
        // get the default preferences from XML
        addPreferencesFromResource(R.xml.preferences);
    }
}
