package us.bmaupin.flashcards.arabic;

//$Id$

import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShowSearchResult extends Activity {
//    private static final String TAG = "ShowSearchResult";
    
    private String arabic = "";
    private int cardId;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private String english = "";
    private String language = "";
    private TextView tv;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        cardId = getIntent().getExtras().getInt(Search.EXTRA_CARD_ID);
        
        setContentView(R.layout.search_result);
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
        
        Toast.makeText(this, "english: " + english + "\tarabic: " + arabic, Toast.LENGTH_LONG)
        .show();
        
        tv = (TextView) findViewById(R.id.textview);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();
            }
        });
        
        flipCard();
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
            tv.setTextSize(56f);
            language = "arabic";
            tv.setText(HelperMethods.fixArabic(arabic));
        } else if (language.equals("arabic")) {
            tv.setTextSize(42f);
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
