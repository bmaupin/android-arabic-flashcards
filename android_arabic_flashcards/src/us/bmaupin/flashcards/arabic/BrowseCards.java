package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class BrowseCards extends Activity {
    private static final String TAG = "ShowCards";
    // dialog id constants
    private static final int DIALOG_NO_CARDS = 0;
    private static final int DIALOG_NO_MORE_CARDS = 1;
    // constants for swipe
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    private static final String[] PROJECTION = new String[] {
    	CardDatabaseHelper.CARDS_TABLE + "." + CardDatabaseHelper._ID,
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC,
    };
    
    // current card language
    private String currentLang;
    private Cursor cursor;
    private int cursorPosition;
    // default card language 
    private String defaultLang;
    // whether or not to apply arabic fixes
    private boolean fixArabic;
    private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper vf;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.cards);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();

        // set the typeface for the TextViews within the ViewFlipper
        Typeface tf = Typeface.createFromAsset(getAssets(), Cards.ARABIC_TYPEFACE);
        TextView tv1 = (TextView)vf.findViewById(R.id.textview1);
        TextView tv2 = (TextView)vf.findViewById(R.id.textview2);
        tv1.setTypeface(tf);
        tv2.setTypeface(tf);
        
        Bundle bundle = this.getIntent().getExtras();
        String cardSet = bundle.getString(Cards.EXTRA_CARD_SET);
        String cardSubSet = bundle.getString(Cards.EXTRA_CARD_SUBSET);
        
        String selection = "";
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
        
        cursor = managedQuery(
                CardProvider.CONTENT_URI_CARDS,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );

        // make sure the cursor isn't empty
        // if it is empty, we take care of that in onResume
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
        // get any preferences that may have changed
/*        
        ch.setAskCardOrder(preferences.getBoolean(
              getString(R.string.preferences_ask_card_order),
              resources.getBoolean(R.bool.preferences_ask_card_order_default)));
        // if we're gonna ask for card order anyway, don't need to change it
        if (!ch.isAskCardOrder()) {
            ch.setCardOrder(preferences.getString(
                   getString(R.string.preferences_default_card_order), 
                   getString(R.string.preferences_default_card_order_default)));
        }
*/
        defaultLang = preferences.getString(
                getString(R.string.preferences_default_lang), 
                getString(R.string.preferences_default_lang_default));
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }
        
// TODO: do we want to do this if the order is random?
        // if we're coming back to this activity from another, we've probably
        // lost our cursor postion
        if (cursor.isBeforeFirst()) {
        	cursor.moveToPosition(cursorPosition);
        }
        
        if (cursor == null || cursor.getCount() == 0) {
        	showDialog(DIALOG_NO_CARDS);
        } else {
        	showFirstCard();
        }
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
        case KeyEvent.KEYCODE_DPAD_LEFT:
            showPrevCard();
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            showNextCard();
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
                return false;
            } catch (Exception e) {
                // nothing
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
        
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(cursor.getString(1));
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            String arabic = cursor.getString(2);
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
    
    private void showNextCard() {
        if (cursor.isLast()) {
            // return a blank card so we can show the user a message that
            // there aren't any more cards (or any cards at all)
// TODO: do something if no more cards to show
            int doSomethingHere;
//            showDialog(DIALOG_NO_MORE_CARDS);
            // for now let's at least let people know there are no more cards...
            Toast.makeText(getApplicationContext(), "No more cards!", Toast.LENGTH_SHORT).show();
        } else {
            cursor.moveToNext();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideLeftIn);
            vf.setOutAnimation(slideLeftOut);
            vf.showNext();
        }
    }
    
    private void showPrevCard() {
        if (cursor.isFirst()) {
// TODO: if back is clicked a bunch of times this will show a bunch of times (but even as you're browsing next)
            Toast.makeText(getApplicationContext(), "No previous cards!", Toast.LENGTH_SHORT).show();
        } else {
            cursor.moveToPrevious();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
        }
    }
    
    private void flipCard() {
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            currentLang = Cards.LANGUAGE_ARABIC;
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
            currentLang = Cards.LANGUAGE_ENGLISH;
        }
        // update the text of the current card
        setCurrentCardText();
    }
    
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
        case DIALOG_NO_CARDS:
            return createNoCardsDialog();
	    case DIALOG_NO_MORE_CARDS:
//	        return createNoMoreCardsDialog();
	    }
	    return null;
	}
	
    private Dialog createNoCardsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sorry, the set of cards you selected is empty.  " +
        		"Please choose a different set of cards.")
                .setCancelable(false)
                .setPositiveButton("Choose new cards", 
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog ad = builder.create();
        return ad;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        
        // store the cursor position in case we come back
        cursorPosition = cursor.getPosition();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
