package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class Search extends ListActivity {
    private static final String TAG = "Search";
    
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          doSearch(query);
        }
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
    }
    
    void doSearch(String query) {
        Log.d(TAG, "query=" + query);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        db.close();
        dbHelper.close();
    }
}
