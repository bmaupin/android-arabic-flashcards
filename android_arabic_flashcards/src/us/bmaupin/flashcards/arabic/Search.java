package us.bmaupin.flashcards.arabic;

// $Id$

import org.amr.arabic.ArabicUtilities;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Search extends ListActivity {
    private static final String TAG = "Search";
    
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String query = "";
        
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.search);
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
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
        
        ListAdapter adapter = new MySimpleCursorAdapter(
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

        // Bind to our new adapter.
        setListAdapter(adapter);
    }
    
/*    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Get the item that was clicked
        Object o = this.getListAdapter().getItem(position);
        String keyword = o.toString();
        Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_LONG)
                .show();
    }
*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        db.close();
        dbHelper.close();
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