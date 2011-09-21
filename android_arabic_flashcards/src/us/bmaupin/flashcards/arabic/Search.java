package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Search extends Activity {
    private static final String TAG = "Search";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          doSearch(query);
        }
    }
    
    void doSearch(String query) {
        Log.d(TAG, "query=" + query);
    }
}
