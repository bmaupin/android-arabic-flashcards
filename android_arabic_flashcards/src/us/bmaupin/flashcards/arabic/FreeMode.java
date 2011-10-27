package us.bmaupin.flashcards.arabic;

import us.bmaupin.flashcards.arabic.ArabicFlashcards.MyGestureDetector;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class FreeMode extends Activity {
    private static final String TAG = "ShowCards";
    // constants for swipe
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    private static final String[] PROJECTION = new String[] {
        CardDatabaseHelper._ID,
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC,
    };
    
    // current card language
    private String currentLang;
    private Cursor cursor;
    // default card language 
    private String defaultLang;
    private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    private Resources resources;
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
        
        setContentView(R.layout.main);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        // create objects for shared preferences and resources
      preferences = getPreferences(MODE_PRIVATE);
      resources = getResources();
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/KacstOne.ttf");

        // set the typeface for the three TextViews within the ViewFlipper
        TextView leftView = (TextView)vf.findViewById(R.id.leftView);
        TextView centerView = (TextView)vf.findViewById(R.id.centerView);
        TextView rightView = (TextView)vf.findViewById(R.id.rightView);
        leftView.setTypeface(tf);
        centerView.setTypeface(tf);
        rightView.setTypeface(tf);
        
        Bundle bundle = this.getIntent().getExtras();
        String cardSet = bundle.getString(ChooseCardSet.EXTRA_CARD_SET);
        String cardSubSet = bundle.getString(ChooseCardSet.EXTRA_CARD_SUBSET);
        
//        Toast.makeText(getApplicationContext(), cardSet, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), cardSubSet, Toast.LENGTH_SHORT).show();
        
        String selection = "";
        String[] selectionArgs = new String[] {};
        
        cursor = managedQuery(
                CardProvider.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                null
        );

        // make sure the cursor isn't empty
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            
            Toast.makeText(getApplicationContext(), cursor.getString(1), Toast.LENGTH_SHORT).show();
        }
        
/*        
        if (currentCardSet.equals(context.getString(
                R.string.card_set_ahlan_wa_sahlan))) {
            sqlCardSetSelection = " WHERE " + CardDatabaseHelper.CARDS_TABLE + "." + 
                    CardDatabaseHelper._ID + " IN (SELECT " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " FROM " + 
                    CardDatabaseHelper.AWS_CHAPTERS_TABLE + " WHERE " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = " + 
                    currentCardSubset + ") ";
            
        } else if (currentCardSet.equals(context.getString(
                R.string.card_set_categories))) {
            sqlCardSetSelection = " WHERE category = '" + currentCardSubset + 
            "'";
            
        } else if (currentCardSet.equals(context.getString(
                R.string.card_set_parts_of_speech))) {
            sqlCardSetSelection = " WHERE type = '" + currentCardSubset + 
            "'";
            
        }
*/
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
        showPlurals = preferences.getBoolean(
                getString(R.string.preferences_show_plurals),
                resources.getBoolean(R.bool.preferences_show_plurals_default));
*/
        defaultLang = preferences.getString(
                getString(R.string.preferences_default_lang), 
                getString(R.string.preferences_default_lang_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }
        
        showFirstCard();
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
            Log.d(TAG, "onSingleTapUp");
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
        ViewGroup currentLayout = (RelativeLayout)vf.getCurrentView();
        // get the child view (TextView)
        TextView currentView = (TextView) currentLayout.getChildAt(0);
        
        if (currentLang.equals("english")) {
            currentView.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            currentView.setText(cursor.getString(1));
            
        } else if (currentLang.equals("arabic")) {
            currentView.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            currentView.setText(HelperMethods.fixArabic(cursor.getString(2), 
                    showVowels));
        }
    }
    
    private void showNextCard() {
        vf.setInAnimation(slideLeftIn);
        vf.setOutAnimation(slideLeftOut);
        vf.showNext();
/*        
        // update the status of the current card
        ch.updateCardStatus(currentCardId, currentCardStatus, direction);
        // get the next one
        nextCard = ch.nextCard();
        
        if (nextCard.isEmpty()) {
            showDialog(DIALOG_NO_MORE_CARDS);
        } else {
            currentCard = nextCard;
            // only show the right animation if there's a next card
            vf.setInAnimation(slideLeftIn);
            vf.setOutAnimation(slideLeftOut);
            vf.showNext();
            
            showCard(currentCard);
        }
*/
    }
    
    private void showPrevCard() {
        if (cursor.isFirst()) {
            Toast.makeText(getApplicationContext(), "No previous cards!", Toast.LENGTH_SHORT).show();
        } else {
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
        }
/*
        nextCard = ch.prevCard();
        
        // make sure there's a previous card to show
        if (nextCard.isEmpty()) {
// TODO: if back is clicked a bunch of times this will show a bunch of times (but even as you're browsing next)
            Toast.makeText(getApplicationContext(), "No previous cards!", Toast.LENGTH_SHORT).show();
        } else {
            currentCard = nextCard;
            // only show the left animation if there's a previous card
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
            
            showCard(currentCard);
        }
*/
    }
    
    private void flipCard() {
/*
        if (currentSide.equals("english")) {
            showCard(currentView, currentCard, "arabic");
        // only show plural if the current side is arabic and plural isn't empty
        } else if (showPlurals && currentSide.equals("arabic") && 
                !currentCard.get("plural").equals("")) {
            showCard(currentView, currentCard, "plural");
        } else {
            showCard(currentView, currentCard, "english");
        }
*/
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
