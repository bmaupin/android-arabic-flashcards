package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class Search extends ListActivity {
    private static final String TAG = "Search";
    
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String query = "";
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          query = intent.getStringExtra(SearchManager.QUERY);
          doSearch(query);
        }
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        
        String[] columns = new String[] {DatabaseHelper._ID, 
                DatabaseHelper.WORDS_ENGLISH};
        String selection = DatabaseHelper.WORDS_ENGLISH + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + query + "%"};
        
        Cursor cursor = db.query(DatabaseHelper.WORDS_TABLE, columns, selection, selectionArgs, null, null, null);
        startManagingCursor(cursor);
        
        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {DatabaseHelper.WORDS_ENGLISH},
                new int[] {android.R.id.list});

        // Bind to our new adapter.
        setListAdapter(adapter);
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
