package us.bmaupin.flashcards.arabic;

//$Id$

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

public class ShowSearchResult extends Activity {
//    private static final String TAG = "ShowSearchResult";
    
    private int cardId;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        cardId = getIntent().getExtras().getInt(Search.EXTRA_CARD_ID);
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
        String english = cursor.getString(0);
        String arabic = cursor.getString(1);
        
        Toast.makeText(this, "english: " + english + "\tarabic: " + arabic, Toast.LENGTH_LONG)
        .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // close the database connection
        db.close();
        dbHelper.close();
    }
}
