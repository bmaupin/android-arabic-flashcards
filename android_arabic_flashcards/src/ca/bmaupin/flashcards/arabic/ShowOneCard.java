package ca.bmaupin.flashcards.arabic;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ShowOneCard extends BaseActivity {
    private static final String TAG = "ShowOneCard";
    
    private String arabic = "";
    // current card language
    private String currentLang;
    private String english = "";
    // whether or not to apply arabic fixes
    private boolean fixArabic;
    private SharedPreferences preferences;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    private TextView tv;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.show_one_card);
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();
        
        tv = (TextView) findViewById(R.id.textview);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();
            }
        });
        
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");
        
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        int cardId = intent.getExtras().getInt(Search.EXTRA_CARD_ID);
        
        /* ordinarily we'd put all the db open code in onResume and close in 
         * onPause, but we don't need to here since we're only accessing the db
         * once
         */
        CardDatabaseHelper dbHelper = new CardDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] columns = new String[] {CardDatabaseHelper.CARDS_ENGLISH, 
                CardDatabaseHelper.CARDS_ARABIC};
        String selection = CardDatabaseHelper._ID + " = ?";
        String[] selectionArgs = new String[] {"" + cardId};
        
        Cursor cursor = db.query(CardDatabaseHelper.CARDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        
        cursor.moveToFirst();
        english = cursor.getString(0);
        arabic = cursor.getString(1);
        
        // close the database connection
        cursor.close();
        db.close();
        dbHelper.close();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        /* TODO: for now, show arabic first
         * in the future, show whichever language wasn't searched for
         */
        currentLang = Cards.LANGUAGE_ARABIC;
        
        if (fixArabic) {
            tv.setTypeface(Typeface.createFromAsset(getAssets(), 
                    Cards.ARABIC_TYPEFACE));
        } else {
            // reset to the default typeface
            tv.setTypeface(Typeface.DEFAULT);
        }
        
        setCardText();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
            flipCard();
            break;
        default:
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }
    
    private void setCardText() {
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(english);
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
        	// need to put this in a temporary variable so it updates if 
        	// preferences change and the activity resumes
        	String tempArabic = arabic;
        	
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            if (fixArabic) {
            	tempArabic = Cards.fixArabic(tempArabic, showVowels);
            }
            if (showVowels) {
            	tv.setText(tempArabic);
            } else {
            	tv.setText(Cards.removeVowels(tempArabic));
            }
        }
    }
    
    private void flipCard() {
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            currentLang = Cards.LANGUAGE_ARABIC;
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
            currentLang = Cards.LANGUAGE_ENGLISH;
        }
        // update the text of the current card
        setCardText();
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
