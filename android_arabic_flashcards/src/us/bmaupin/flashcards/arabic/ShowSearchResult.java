package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ShowSearchResult extends Activity {
    private static final String TAG = "ShowSearchResult";
    
    private String arabic = "";
    private int cardId;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private String english = "";
    private String language = "";
    private boolean showVowels = true;
    private TextView tv;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        
        Bundle bundle = this.getIntent().getExtras();
        cardId = bundle.getInt(Search.EXTRA_CARD_ID);
        showVowels = bundle.getBoolean(ArabicFlashcards.EXTRA_SHOW_VOWELS);
        
        setContentView(R.layout.search_result);
        
        tv = (TextView) findViewById(R.id.textview);
        HelperMethods.setArabicTypeface(this, tv);
        
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
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        
        String[] columns = new String[] {DatabaseHelper.CARDS_ENGLISH, 
                DatabaseHelper.CARDS_ARABIC};
        String selection = DatabaseHelper._ID + " = ?";
        String[] selectionArgs = new String[] {"" + cardId};
        
        Cursor cursor = db.query(DatabaseHelper.CARDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        
        cursor.moveToFirst();
        english = cursor.getString(0);
        arabic = cursor.getString(1);
        
        // close the database connection
        cursor.close();
        db.close();
        dbHelper.close();
        
        flipCard();
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
    
    /**
     * Given a view, a card, and a language, shows the card in the view and 
     * formats it depending on the language
     */
    private void flipCard() {
/* TODO: for now, show arabic first
 * in the future, show whichever language wasn't searched for
 */
        if (language.equals("english") || language.equals("")) {
            tv.setTextSize(ArabicFlashcards.ARABIC_CARD_TEXT_SIZE);
            language = "arabic";
            tv.setText(HelperMethods.fixArabic(arabic, showVowels));
        } else if (language.equals("arabic")) {
            tv.setTextSize(ArabicFlashcards.ENGLISH_CARD_TEXT_SIZE);
            language = "english";
            tv.setText(english);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }
}
