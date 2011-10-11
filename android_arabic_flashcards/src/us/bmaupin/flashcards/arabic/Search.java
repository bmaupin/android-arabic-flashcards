package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class Search extends ListActivity {
    static final String EXTRA_CARD_ID = "android.intent.extra.CARD_ID";
    private static final String TAG = "Search";
    
    private Cursor cursor;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Intent intent;
    private boolean showVowels;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        
        super.onCreate(savedInstanceState);
        // we need to specify a layout to show a message when no results are 
        // found
        setContentView(R.layout.search);
        
        this.intent = getIntent();
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
        super.onNewIntent(intent);
        
        this.intent = intent;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        
        String query = "";
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
        if (appData != null) {
            showVowels = appData.getBoolean(ArabicFlashcards.EXTRA_SHOW_VOWELS);
        }
        
        String[] columns = new String[] {DatabaseHelper._ID, 
                DatabaseHelper.CARDS_ENGLISH, DatabaseHelper.CARDS_ARABIC};
        String selection = DatabaseHelper.CARDS_ENGLISH + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + query + "%"};
        
        cursor = db.query(DatabaseHelper.CARDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        startManagingCursor(cursor);
        
        SimpleCursorAdapter adapter = new MySimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] {DatabaseHelper.CARDS_ENGLISH, DatabaseHelper.CARDS_ARABIC},
                new int[] { android.R.id.text1, android.R.id.text2 });

        // http://stackoverflow.com/questions/3609126/changing-values-from-cursor-using-simplecursoradapter
        adapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                if (aColumnIndex == 2) {
                    String arabic = aCursor.getString(aColumnIndex);
                    TextView tv = (TextView) aView;                    
                    tv.setText(HelperMethods.fixArabic(arabic, showVowels));
                    return true;
                }
                
                return false;
            }
        });
        
        // Bind to our new adapter.
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor cursor = (Cursor) this.getListAdapter().getItem(position);
        int cardId = cursor.getInt(cursor.getColumnIndex("_id"));
        
        Intent intent = new Intent(this, ShowSearchResult.class);
        intent.putExtra(EXTRA_CARD_ID, cardId);
        intent.putExtra(ArabicFlashcards.EXTRA_SHOW_VOWELS, showVowels);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        
        // clean up after ourselves
        cursor.close();
        db.close();
        dbHelper.close();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }
}

/**
 * http://stackoverflow.com/questions/5399781/change-text-color-in-listview/5399965#5399965
 * @author bmaupin
 *
 */
class MySimpleCursorAdapter extends SimpleCursorAdapter {
    private Context context;
    
    public MySimpleCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        
        TextView tv = (TextView)v.findViewById(android.R.id.text2);
        HelperMethods.setArabicTypeface(context, tv);
        
        return v;
    }
}