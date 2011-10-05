package us.bmaupin.flashcards.arabic;

//$Id$

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ShowSearchResult extends Activity {
//    private static final String TAG = "ShowSearchResult";
    
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        
        String[] columns = new String[] {DatabaseHelper.WORDS_ENGLISH, 
                DatabaseHelper.WORDS_ARABIC};
        String selection = DatabaseHelper._ID + " = ?";
        String[] selectionArgs = new String[] {"" + cardId};
        
        Cursor cursor = db.query(DatabaseHelper.WORDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        startManagingCursor(cursor);
        
        cursor.moveToFirst();
        english = cursor.getString(0);
        arabic = cursor.getString(1);
        
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
     * Given a view, a word, and a language, shows the word in the view and 
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
    protected void onPause() {
        super.onPause();
        // close the database connection
        db.close();
        dbHelper.close();
    }
}
