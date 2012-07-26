package ca.bmaupin.flashcards.arabic;

import org.amr.arabic.ArabicUtilities;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import ca.bmaupin.flashcards.arabic.data.CardProvider;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Search extends FragmentActivity 
        implements LoaderManager.LoaderCallbacks<Cursor>{
    static final String EXTRA_CARD_ID = "android.intent.extra.CARD_ID";
    private static final String TAG = "Search";
    
    private static final String[] PROJECTION = new String[] {
        CardDatabaseHelper._ID, 
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC
    };
    
    private SimpleCursorAdapter adapter;
    // whether or not to apply arabic fixes
    private boolean fixArabic;
    private Intent intent;
    private ListView lv;
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
        
        lv = (ListView) findViewById(android.R.id.list);
        
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, 
                    int position, long id) {
//                Cursor cursor = (Cursor) this.getListAdapter().getItem(position);
//                int cardId = cursor.getInt(cursor.getColumnIndex("_id"));
                
                Intent intent = new Intent(Search.this, ShowOneCard.class);
                // id is the card id.  convert it from long to int (I 
                // think it's safe to say it will never be > 2,147,483,647)
                intent.putExtra(EXTRA_CARD_ID, Cards.longToInteger(id));
                startActivity(intent);
            }
        });
        
// TODO
        /*
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, 
                    int position, long id) {
                Intent intent = new Intent(ChooseStudySet.this, 
                        ShowStudySet.class);
                // id is the study set id.  convert it from long to int (I 
                // think it's safe to say it will never be > 2,147,483,647)
                intent.putExtra(Cards.EXTRA_STUDY_SET_ID, 
                        Cards.longToInteger(id));
                startActivity(intent);
            }
        });
        */
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
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            getSupportLoaderManager().initLoader(0, null, this);
            
//
            Toast.makeText(getApplicationContext(), "search intent", Toast.LENGTH_SHORT).show();
            
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            int cardId = Cards.stringToInteger(intent.getDataString());
            Intent intent = new Intent(this, ShowOneCard.class);
            intent.putExtra(EXTRA_CARD_ID, cardId);
            startActivity(intent);
            
//
            Toast.makeText(getApplicationContext(), "view intent", Toast.LENGTH_SHORT).show();
        }
//        
        else {
//
            Toast.makeText(getApplicationContext(), "no intent", Toast.LENGTH_SHORT).show();
        }
        
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        

        
//        cursor = db.query(CardDatabaseHelper.CARDS_TABLE, columns, selection, 
//                selectionArgs, null, null, null);
//        startManagingCursor(cursor);
        

    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)  {
        // string containing the columns to map to the adapter
        String[] from = new String[] {};
        String selection = "";
        String[] selectionArgs = new String[] {};
        
        String query = intent.getStringExtra(SearchManager.QUERY);
        
//
        Toast.makeText(getApplicationContext(), "query=" + query, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "query=" + query);
        
        // if the query is arabic
        if (ArabicUtilities.isArabicWord(query)) {
            String arabicQuery = "%";
            // insert % between each character to account for vowels
            for (int i=0; i < query.length(); i++) {
                arabicQuery += query.charAt(i) + "%";
            }
            
            selection = CardDatabaseHelper.CARDS_ARABIC + " LIKE ?";
            selectionArgs = new String[] {arabicQuery};
            from = new String[] {CardDatabaseHelper.CARDS_ARABIC,
                    CardDatabaseHelper.CARDS_ENGLISH};
            
        // otherwise, the query is english
        } else {
            selection = CardDatabaseHelper.CARDS_ENGLISH + " LIKE ?";
            selectionArgs = new String[] {"%" + query + "%"};
            from = new String[] {CardDatabaseHelper.CARDS_ENGLISH, 
                    CardDatabaseHelper.CARDS_ARABIC};
        }

        // create the adapter here so we can make arabic or english the primary 
        // field for the cursor depending on the query
        adapter = new MySimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                null,
                from,
                new int[] {android.R.id.text1, android.R.id.text2},
                0,
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
        lv.setAdapter(adapter);
        
        return new CursorLoader(this,
                CardProvider.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
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
            String[] from, int[] to, int flags, boolean fixArabic) {
        super(context, layout, c, from, to, flags);
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