package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
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
    
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private boolean showVowels;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        String query = "";
        
        super.onCreate(savedInstanceState);
        // we need to specify a layout to show a message when no results are 
        // found
        setContentView(R.layout.search);
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
        if (appData != null) {
            showVowels = appData.getBoolean(ArabicFlashcards.EXTRA_SHOW_VOWELS);
        }
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        
        String[] columns = new String[] {DatabaseHelper._ID, 
                DatabaseHelper.WORDS_ENGLISH, DatabaseHelper.WORDS_ARABIC};
        String selection = DatabaseHelper.WORDS_ENGLISH + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + query + "%"};
        
        Cursor cursor = db.query(DatabaseHelper.WORDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        startManagingCursor(cursor);
        
        SimpleCursorAdapter adapter = new MySimpleCursorAdapter(
                this,
//                android.R.layout.simple_list_item_1,
//                android.R.layout.two_line_list_item,
                android.R.layout.simple_list_item_2,
                cursor,
//                new String[] {DatabaseHelper.WORDS_ENGLISH},
//                columns,
                new String[] {DatabaseHelper.WORDS_ENGLISH, DatabaseHelper.WORDS_ARABIC},
//                new int[] {android.R.id.list});
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
        startActivity(intent);
        
        
        
        
//        Toast.makeText(this, "You selected: " + cursor.getInt(cursor.getColumnIndex("_id")), Toast.LENGTH_LONG)
//        .show();

/*        
        // Get the item that was clicked
        Object o = this.getListAdapter().getItem(position);
        String keyword = o.toString();
        Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_LONG)
                .show();
*/
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        
        db.close();
        dbHelper.close();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        
     // delete this method
        int temp;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        
// delete this method
        int temp;
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
        
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/KacstOne.ttf");
        TextView tv = (TextView)v.findViewById(android.R.id.text2);
        tv.setTypeface(tf);
        
//        String arabicWord = ArabicUtilities.reshape(thisWord.get(thisLang));
        
        return v;
    }
}