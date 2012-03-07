package ca.bmaupin.flashcards.arabic;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private CardDatabaseHelper dbHelper;
    // whether or not to apply arabic fixes
    private boolean fixArabic;
    private Intent intent;
    private SharedPreferences preferences;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    
    /**
     * @return the fixArabic
     */
    public boolean isFixArabic() {
        return fixArabic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        // we need to specify a layout to show a message when no results are 
        // found
        setContentView(R.layout.search);
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();
        
        this.intent = getIntent();
        
        dbHelper = new CardDatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");
        
        this.intent = intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
        String query = "";
        
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        
        String[] columns = new String[] {CardDatabaseHelper._ID, 
                CardDatabaseHelper.CARDS_ENGLISH, CardDatabaseHelper.CARDS_ARABIC};
        String selection = CardDatabaseHelper.CARDS_ENGLISH + " LIKE ?";
        String[] selectionArgs = new String[] {"%" + query + "%"};
        
        cursor = db.query(CardDatabaseHelper.CARDS_TABLE, columns, selection, 
                selectionArgs, null, null, null);
        startManagingCursor(cursor);
        
        SimpleCursorAdapter adapter = new MySimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] {CardDatabaseHelper.CARDS_ENGLISH, CardDatabaseHelper.CARDS_ARABIC},
                new int[] { android.R.id.text1, android.R.id.text2 },
                fixArabic);

        // http://stackoverflow.com/questions/3609126/changing-values-from-cursor-using-simplecursoradapter
        adapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                if (aColumnIndex == 2) {
                    String arabic = aCursor.getString(aColumnIndex);
                    TextView tv = (TextView) aView;
                    if (fixArabic) {
                    	arabic = Cards.fixArabic(arabic, showVowels);
                    }
                    if (showVowels) {
                    	tv.setText(arabic);
                    } else {
                    	tv.setText(Cards.removeVowels(arabic));
                    }
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
        
        Intent intent = new Intent(this, ShowOneCard.class);
        intent.putExtra(EXTRA_CARD_ID, cardId);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        // clean up after ourselves
        cursor.close();
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
    private boolean fixArabic;
    
    public MySimpleCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to, boolean fixArabic) {
        super(context, layout, c, from, to);
        this.context = context;
        this.fixArabic = fixArabic;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (fixArabic) {
            ((TextView) v.findViewById(android.R.id.text2)).setTypeface(
                    Typeface.createFromAsset(context.getAssets(), 
                            Cards.ARABIC_TYPEFACE));
        }
        
        return v;
    }
}