package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ShowOneCard extends Activity {
    private static final String TAG = "ShowOneCard";
    
    private String arabic = "";
    private int cardId;
    // current card language
    private String currentLang;
    private SQLiteDatabase db;
    private CardDatabaseHelper dbHelper;
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
        Cards.setArabicTypeface(this, tv);
        
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();
            }
        });
        
        /* ordinarily we'd put all the db open code in onResume and close in 
         * onPause, but we don't need to here since we're only accessing the db
         * once
         */
        dbHelper = new CardDatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        
        Bundle bundle = this.getIntent().getExtras();
        getCard(bundle);
    }
        
    private void getCard(Bundle bundle) {
        cardId = bundle.getInt(Search.EXTRA_CARD_ID);
        
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent()");
		
        Bundle bundle = intent.getExtras();
        getCard(bundle);
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
        currentLang = "arabic";
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
        if (currentLang.equals("english")) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(english);
            
        } else if (currentLang.equals("arabic")) {
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            if (fixArabic) {
            	arabic = Cards.fixArabic(arabic, showVowels);
            }
            if (showVowels) {
            	tv.setText(arabic);
            } else {
            	tv.setText(Cards.removeVowels(arabic));
            }
        }
    }
    
    private void flipCard() {
        if (currentLang.equals("english")) {
            currentLang = "arabic";
            
        } else if (currentLang.equals("arabic")) {
            currentLang = "english";
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
