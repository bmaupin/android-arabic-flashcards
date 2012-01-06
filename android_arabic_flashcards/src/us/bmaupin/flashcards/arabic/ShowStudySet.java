package us.bmaupin.flashcards.arabic;

import java.util.ArrayList;
import java.util.List;

import us.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import us.bmaupin.flashcards.arabic.data.CardProvider;
import us.bmaupin.flashcards.arabic.data.StudySetDatabaseHelper;
import us.bmaupin.flashcards.arabic.data.StudySetProvider;
import us.bmaupin.flashcards.arabic.old.ProfileDatabaseHelper;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ShowStudySet extends FragmentActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ShowStudySet";
    private static final int RESPONSE_KNOWN = 0;
    private static final int RESPONSE_IFFY = 1;
    private static final int RESPONSE_UNKNOWN = 2;
    private static final int LOADER_CARD = 0;
    private static final int LOADER_STUDYSET = 1;
    // how many new cards to show per study set session
    // we'll probably replace this later using shared preferences
    private static final int MAX_NEW_CARDS_TO_SHOW = 10;
    private static final int MAX_STUDYSET_CARDS_TO_SHOW = 20;
    // constants for swipe
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int ONE_HOUR_IN_MS = 3600000;
    
    private static final String[] PROJECTION_CARDS = new String[] {
        CardDatabaseHelper.CARDS_TABLE + "." + CardDatabaseHelper._ID,
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC,
    };
    private static final String[] PROJECTION_STUDYSET = new String[] {
        StudySetDatabaseHelper.SET_DUE_TIME,
    };
    
    private Cursor cardsCursor;
    private int cardsCursorPosition;
    // current card language
    private String currentLang;
    // default card language 
    private String defaultLang;
    // whether or not to apply arabic fixes
    private boolean fixArabic;
    private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    // the ID of the current study set
    private long studySetId;
    private String studySetIds = "";
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper vf;
    
    private Cursor studySetCursor;
    private SQLiteDatabase studySetDb;
    private StudySetDatabaseHelper studySetDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.cards);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, 
        		R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, 
        		R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, 
        		R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();

        // set the typeface for the TextViews within the ViewFlipper
        Typeface tf = Typeface.createFromAsset(getAssets(), 
        		Cards.ARABIC_TYPEFACE);
        TextView tv1 = (TextView)vf.findViewById(R.id.textview1);
        TextView tv2 = (TextView)vf.findViewById(R.id.textview2);
        tv1.setTypeface(tf);
        tv2.setTypeface(tf);
        
        Bundle bundle = this.getIntent().getExtras();
        studySetId = bundle.getLong(Cards.EXTRA_STUDY_SET_ID);
        String cardSet = bundle.getString(Cards.EXTRA_CARD_GROUP);
        String cardSubSet = bundle.getString(Cards.EXTRA_CARD_SUBGROUP);
        defaultLang = bundle.getString(Cards.EXTRA_STUDY_SET_LANGUAGE);
        
        String selection = StudySetDatabaseHelper.SET_DUE_TIME + " <  0" + 
                System.currentTimeMillis();
        
        studySetDbHelper = new StudySetDatabaseHelper(this);
        studySetDb = studySetDbHelper.getReadableDatabase();
        studySetCursor = studySetDb.query(
                StudySetDatabaseHelper.SET_TABLE_PREFIX + studySetId, 
                new String[] {StudySetDatabaseHelper.SET_CARD_ID}, 
                selection, null, null, null, null, 
                "" + MAX_STUDYSET_CARDS_TO_SHOW);
        
        if (studySetCursor.moveToFirst()) {
            studySetIds = "(";
            while (!studySetCursor.isAfterLast()) {
                studySetIds += studySetCursor.getString(0) + ", ";
                studySetCursor.moveToNext();
            }
            // drop the separator from the last part of the string
            studySetIds = studySetIds.substring(0, studySetIds.length() - 2) + 
                    ")";
            
        } else {
// TODO: no due cards, do something here
        }
        
        studySetCursor.close();
        studySetDb.close();
        studySetDbHelper.close();
        
        getSupportLoaderManager().initLoader(LOADER_CARD, null, this);
        
/*      
        selection = "";
        String[] selectionArgs = new String[] {};
        String sortOrder = "";
        
        if (cardSet.equals(getString(R.string.card_group_ahlan_wa_sahlan))) {
            /*
             * this looks like:
             * where cards._id in (select card_ID from aws_chapters where chapter = ?) and chapter = ?
             * 
             * it may seem terribly redundant, but it's much faster than:
             * where chapter = ?
             * 
             * because it doesn't do the left join (in the provider class) 
             * on both entire tables
             * 
             * the entire query ends up looking like this:
             * select * from cards left join aws_chapters on cards._id = aws_chapters.card_id where cards._id in (select card_ID from aws_chapters where chapter = 4) and chapter = 4 order by aws_chapters._id;
             * 
             * instead of this:
             * select * from cards left join aws_chapters on cards._id = aws_chapters.card_id where chapter = 4 order by aws_chapters._id;
             */
/*
            selection = CardDatabaseHelper.CARDS_TABLE + "." + 
                    CardDatabaseHelper._ID + " IN (SELECT " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " FROM " + 
                    CardDatabaseHelper.AWS_CHAPTERS_TABLE + " WHERE " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = ?) AND " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = ? ";
            selectionArgs = new String[] {cardSubSet, cardSubSet};
            sortOrder = CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." + 
                    CardDatabaseHelper._ID;
            
        } else if (cardSet.equals(getString(R.string.card_group_categories))) {
            selection = CardDatabaseHelper.CARDS_CATEGORY + " = ? ";
            selectionArgs = new String[] {cardSubSet};
            
        } else if (cardSet.equals(getString(
                R.string.card_group_parts_of_speech))) {
            selection = CardDatabaseHelper.CARDS_TYPE + " = ? ";
            selectionArgs = new String[] {cardSubSet};
        }
        
        cardsCursor = managedQuery(
        		// add the limit to the content uri
        		ContentUris.withAppendedId(
        				CardProvider.CONTENT_URI_CARDS_LIMIT, 
        				MAX_NEW_CARDS_TO_SHOW),
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );

        // make sure the cursor isn't empty
        // if it is empty, we take care of that in onResume
        if (cardsCursor != null && cardsCursor.getCount() != 0) {
            cardsCursor.moveToFirst();
        }
*/
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
//        studySetDbHelper = new StudySetDatabaseHelper(this);
//        studySetDb = studySetDbHelper.getReadableDatabase();
        
        // get any preferences that may have changed
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }

/*
// TODO: do we want to do this if the order is random?
        // if we're coming back to this activity from another, we've probably
        // lost our cursor postion
        if (cardsCursor.isBeforeFirst()) {
            cardsCursor.moveToPosition(cardsCursorPosition);
        }
*/
        
/*
        if (cardsCursor == null || cardsCursor.getCount() == 0) {
            Log.e(TAG, "for some reason the cursor is empty...");
        } else {
            showFirstCard();
        }
*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
// TODO: clean this up
        String selection;
        
        switch (id) {
        case LOADER_CARD:
            selection = CardDatabaseHelper._ID + " IN " + studySetIds;

// TODO: use a specific/random order?
            return new CursorLoader(this,
                    CardProvider.CONTENT_URI_CARDS,
                    PROJECTION_CARDS,
                    selection,
                    null,
                    null);
            
        case LOADER_STUDYSET:
            return new CursorLoader(this,
                    ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                            studySetId),
                    args.getStringArray("columns"),
                    args.getString("selection"),
                    args.getStringArray("selectionArgs"),
                    null);
/*            
        case LOADER_STUDYSET:
            selection = StudySetDatabaseHelper.SET_DUE_TIME + " < " + 
                    System.currentTimeMillis();
            
            return new CursorLoader(this,
                    ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                            studySetId),
                    PROJECTION_STUDYSET, 
                    selection,
                    null,
                    null);
*/
            
        default:
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
        case LOADER_CARD:
            cardsCursor = data;
            
            if (cardsCursor.moveToFirst()) {
                showFirstCard();
            } else {
                Log.e(TAG, "for some reason the cursor is empty...");
            }
            
            break;

        case LOADER_STUDYSET:
            if (data.moveToFirst()) {
                Toast.makeText(getApplicationContext(), "DEBUG: " + data.getString(0), Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
//        mAdapter.swapCursor(null);
    }

    /* Inflates the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /* Handles menu selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
// TODO: do we want to add a way to go back to ChooseStudySet?
/*
            case R.id.menu_choose_cards:
                chooseCardSet();
                return true;
*/
            case R.id.menu_help:
                startActivity(new Intent(this, Help.class));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
                + event);
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
            showNextCard(RESPONSE_KNOWN);
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            showNextCard(RESPONSE_UNKNOWN);
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            showPrevCard();
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            showNextCard(RESPONSE_IFFY);
            break;
        case KeyEvent.KEYCODE_DPAD_CENTER:
            flipCard();
            break;
        default:
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
/*                
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextCard();
                    return true;
                // left to right
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showPrevCard();
                    return true;
                }
*/
                // from http://stackoverflow.com/questions/4098198/adding-fling-gesture-to-an-image-view-android
                // right to left
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextCard(RESPONSE_IFFY);
                    return true;
                // left to right
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showPrevCard();
                    return true;
                }
                // bottom to top
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextCard(RESPONSE_KNOWN);
                    return true;
                // top to bottom
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextCard(RESPONSE_UNKNOWN);
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "ERROR: Exception caught when swiping");
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp()");
            flipCard();
            return true;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent called");
        if (gestureDetector.onTouchEvent(event))
            return true;
        else
            return false;
    }
    
    private void showFirstCard() {
        setCurrentCardText();
    }
    
    private void setCardText(int layoutIndexToShow) {
        ViewGroup rl = (RelativeLayout)vf.getChildAt(layoutIndexToShow);
        TextView tv = (TextView)rl.getChildAt(0);
        
        Log.d(TAG, "currentLang=" + currentLang);
        
        if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ENGLISH)) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(cardsCursor.getString(1));
            
        } else if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ARABIC)) {
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            String arabic = cardsCursor.getString(2);
            if (fixArabic) {
                arabic = Cards.fixArabic(arabic, showVowels);
            }
            if (showVowels) {
                tv.setText(arabic);
            } else {
                tv.setText(Cards.removeVowels(arabic));
            }
        }
    }
    
    private void setCurrentCardText() {
        setCardText(vf.getDisplayedChild());
    }
    
    /*
     * sets the text of the card that isn't seen
     */
    private void setUnseenCardText() {
        // the index of the layout view we want to show
        int layoutIndexToShow;
        // we want to change the layout that isn't currently being shown
        if (vf.getDisplayedChild() == 0) {
            layoutIndexToShow = 1;
        } else {
            layoutIndexToShow = 0;
        }
        
        setCardText(layoutIndexToShow);
    }
    
    private void showNextCard(int response) {
        updateCardInterval(cardsCursor.getString(0), response);
        showNextCard();
    }
    
    private void showNextCard() {
        if (cardsCursor.isLast()) {
            // return a blank card so we can show the user a message that
            // there aren't any more cards (or any cards at all)
// TODO: do something if no more cards to show
            int doSomethingHere;
            // for now let's at least let people know there are no more cards...
            Toast.makeText(getApplicationContext(), "No more cards!", Toast.LENGTH_SHORT).show();
        } else {
            cardsCursor.moveToNext();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideLeftIn);
            vf.setOutAnimation(slideLeftOut);
            vf.showNext();
        }
    }
    
    private void showPrevCard() {
        if (cardsCursor.isFirst()) {
// TODO: if back is clicked a bunch of times this will show a bunch of times (but even as you're browsing next)
            Toast.makeText(getApplicationContext(), "No previous cards!", 
                    Toast.LENGTH_SHORT).show();
        } else {
            cardsCursor.moveToPrevious();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
        }
    }
    
    private void flipCard() {
        if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ENGLISH)) {
            currentLang = Cards.LANGUAGE_ARABIC;
            
        } else if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ARABIC)) {
            currentLang = Cards.LANGUAGE_ENGLISH;
        }
        // update the text of the current card
        setCurrentCardText();
    }
    
    private void updateCardInterval(String cardId, int response) {
        final String[] COLUMNS = {StudySetDatabaseHelper.SET_INTERVAL};
        final String SELECTION = StudySetDatabaseHelper.SET_CARD_ID + " = ? ";
        final String[] SELECTIONARGS = {cardId};
        
        long newDueTime;
        int newInterval;
        int oldInterval;
    	
        Bundle bundle = new Bundle();
// TODO: use extra constants for these once we get cursorloader stuff sorted out
        bundle.putStringArray("columns", new String[] {StudySetDatabaseHelper.SET_INTERVAL});
        bundle.putString("selection", StudySetDatabaseHelper.SET_CARD_ID + " = ? ");
        bundle.putStringArray("selectionArgs", new String[] {cardId});
        
        getSupportLoaderManager().initLoader(LOADER_STUDYSET, bundle, this);
/*        
    	studySetCursor = studySetDb.query(
    	        StudySetDatabaseHelper.SET_TABLE_PREFIX + 
    			studySetId, COLUMNS, SELECTION, SELECTIONARGS, null, null, 
    			null);
    	
        if (studySetCursor.getCount() != 0) {
            studySetCursor.moveToFirst();
            oldInterval = studySetCursor.getInt(0);
            
        	switch(response) {
        	case RESPONSE_KNOWN:
        		// create the new interval and round it
        	    newInterval = Math.round(oldInterval * Cards.MULTIPLIER_KNOWN);
        	    break;
        	case RESPONSE_IFFY:
        		newInterval = Math.round(oldInterval * Cards.MULTIPLIER_IFFY);
        	    break;
        	case RESPONSE_UNKNOWN:
        		newInterval = Math.round(oldInterval * 
        				Cards.MULTIPLIER_UNKNOWN);
        		
                // don't allow the interval to go below the minimum
                if (newInterval < Cards.MIN_INTERVAL) {
                    newInterval = Cards.MIN_INTERVAL;
                // don't allow the interval to go above the maximum
                } else if (newInterval > Cards.MAX_UNKNOWN_INTERVAL) {
                    newInterval = Cards.MAX_UNKNOWN_INTERVAL;
                }
        	    break;
        	default:
        		Log.e(TAG, "ERROR: unknown response");
        	    newInterval = oldInterval;
        	}
        } else {
            switch(response) {
            case RESPONSE_KNOWN:
                newInterval = Cards.FIRST_INTERVAL_KNOWN;
                break;
            case RESPONSE_IFFY:
                newInterval = Cards.FIRST_INTERVAL_IFFY;
                break;
            case RESPONSE_UNKNOWN:
// TODO: implement logic for dealing with unknown cards
                newInterval = Cards.MIN_INTERVAL;
                break;
            default:
            	Log.e(TAG, "ERROR: unknown response");
                newInterval = Cards.MIN_INTERVAL;
            }
        }
        
        newDueTime = System.currentTimeMillis() + (newInterval * 
                ONE_HOUR_IN_MS);
        
        ContentValues cv = new ContentValues();
        cv.put(StudySetDatabaseHelper.SET_CARD_ID, cardId);
        cv.put(StudySetDatabaseHelper.SET_INTERVAL, newInterval);
        cv.put(StudySetDatabaseHelper.SET_DUE_TIME, newDueTime);
        
        studySetDb.replace(StudySetDatabaseHelper.SET_TABLE_PREFIX + studySetId, 
        		ProfileDatabaseHelper.CARD_ID, cv);
*/
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        
        // store the cursor position in case we come back
        cardsCursorPosition = cardsCursor.getPosition();
/*        
        // clean up after ourselves
        if (!studySetCursor.isClosed()) {
            studySetCursor.close();
        }
        studySetDb.close();
        studySetDbHelper.close();
*/
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
