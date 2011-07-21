package us.bmaupin.flashcards.arabic;

// $Id$

import java.util.HashMap;
import java.util.Map;

import org.amr.arabic.ArabicUtilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ArabicFlashcards extends Activity {
    // unique dialog id
    private static final int DIALOG_NO_MORE_CARDS = 0;
    private static final int DIALOG_NO_UNKNOWN_CARDS = 1;
    private static final int DIALOG_SELECT_CARD_ORDER = 2;
    static final String EXTRA_PROFILE_NAME = 
        "android.intent.extra.PROFILE_NAME";
    private static final int CHOOSE_CARD_SET = 0;
	private static final String TAG = "ArabicFlashcards";
	
	private CardHelper ch;
	private String currentCardId;
	private int currentCardStatus;
	private String currentLang;
	private TextView currentView;
	private Map<String, String> currentWord = new HashMap<String, String>();
	private String defaultLang;
	private Map<String, String> nextWord;
	private Resources resources;
	private SharedPreferences preferences;
	
	// class variables for swipe
    private static final int SWIPE_MIN_DISTANCE = 120;
//    private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
//	private View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper vf;
    
//    private TextView leftView;
//    private TextView centerView;
//    private TextView rightView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        
        // get an instance of the profile db just to make sure it's initialized
        ProfileDatabaseHelper profileHelper = new ProfileDatabaseHelper(this);
        SQLiteDatabase profileDb = profileHelper.getReadableDatabase();
        profileDb.close();
        profileHelper.close();
        
        setContentView(R.layout.main);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
/*        
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
*/
        
    	ch = new CardHelper(this);
        
    	// create objects for shared preferences and resources
        preferences = getSharedPreferences(ch.getProfileName(), MODE_PRIVATE);
        resources = getResources();
		
//      Typeface face=Typeface.createFromAsset(getAssets(), "fonts/sil-lateef/LateefRegOT.ttf");
//      Typeface face=Typeface.createFromAsset(getAssets(), "fonts/tahoma.ttf");
		Typeface face=Typeface.createFromAsset(getAssets(), "fonts/DejaVuSans.ttf");

		// set the typeface for the three TextViews within the ViewFlipper
        TextView leftView = (TextView)vf.findViewById(R.id.leftView);
        TextView centerView = (TextView)vf.findViewById(R.id.centerView);
        TextView rightView = (TextView)vf.findViewById(R.id.rightView);
        leftView.setTypeface(face);
        centerView.setTypeface(face);
        rightView.setTypeface(face);
    }

	@Override
	protected void onStart() {
		super.onStart();
//		
		Log.d(TAG, "onStart called");
	}
    
	@Override
	protected void onResume() {
		super.onResume();
//		
		Log.d(TAG, "onResume called");
		
		// get any preferences that may have changed
		ch.setAskCardOrder(preferences.getBoolean(
		      getString(R.string.preferences_ask_card_order),
		      resources.getBoolean(R.bool.preferences_ask_card_order_default)));
		// if we're gonna ask for card order anyway, don't need to change it
		if (!ch.isAskCardOrder()) {
	        ch.setCardOrder(preferences.getString(
	               getString(R.string.preferences_default_card_order), 
	               getString(R.string.preferences_default_card_order_default)));
		}
		defaultLang = preferences.getString(
		        getString(R.string.preferences_default_lang), 
		        getString(R.string.preferences_default_lang_default));
		
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }
		
		if (currentWord.isEmpty()) {
		    showFirstCard();
		} else {
	        // reshow the current card in case anything's changed
	        reshowCurrentCard();
		}
	}
    
    @Override
    protected void onStop(){
    	super.onStop();
//       
		Log.d(TAG, "onStop called");
    }
    
	@Override
	protected void onPause() {
		super.onPause();
//		
		Log.d(TAG, "onPause called");
		
		// save any preferences that can be changed outside the preferences activity
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(getString(R.string.preferences_ask_card_order), 
		        ch.isAskCardOrder());
		editor.putString(getString(R.string.preferences_default_card_order), 
		        ch.getCardOrder());
		
		// Commit the edits!
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy called");
		
		// close the database helper so android doesn't whine
//		helper.close();
		ch.close();
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
    	case R.id.menu_about:
    		startActivity(new Intent(this, About.class));
    		return true;
    	case R.id.menu_choose_cards:
    		chooseCardSet();
    		return true;
    	case R.id.menu_exit:
    		finish();
    		return true;
    	case R.id.menu_help:
    		startActivity(new Intent(this, Help.class));
    		return true;
    	case R.id.menu_settings:
    	    Intent intent = new Intent(this, Preferences.class);
    	    intent.putExtra(EXTRA_PROFILE_NAME, ch.getProfileName());
    	    startActivity(intent);
    		return true;
    	}
    	return false;
    }
    
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	
	Log.d(TAG, "onActivityResult called");
	
	switch(requestCode) {
		case (CHOOSE_CARD_SET) : {
			if (resultCode == Activity.RESULT_OK) {
				String cardSet = data.getStringExtra(ChooseCardSet.CARD_SET);
				Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
				
				if (data.getStringExtra(ChooseCardSet.CARD_SUBSET) == null) {
				    ch.loadCardSet(cardSet);
				} else {
				    ch.loadCardSet(cardSet, data.getStringExtra(
				            ChooseCardSet.CARD_SUBSET));
				}
                
                if (ch.isAskCardOrder()) {
                    showDialog(DIALOG_SELECT_CARD_ORDER);
                } else {
                    showFirstCard();
                }
			}
			break;
		}
	}
}

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
				+ event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			showNextCard("up");
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			showNextCard("down");
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			showPrevCard();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			showNextCard("right");
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			flipCard();
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
    
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	        case DIALOG_NO_MORE_CARDS:
	            return createNoMoreCardsDialog();
            case DIALOG_NO_UNKNOWN_CARDS:
                return createNoUnkownCardsDialog();
	        case DIALOG_SELECT_CARD_ORDER:
	            return createSelectCardOrderDialog();
	    }
	    return null;
	}
	
    private Dialog createNoUnkownCardsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You don't currently have any cards marked as " +
        		"unkown, so there aren't any unknown cards to show.  " +
                "Please choose a different set of cards.")
                .setCancelable(false)
                .setPositiveButton("Choose new cards", 
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chooseCardSet();
                    }
                });
        AlertDialog ad = builder.create();
        return ad;
    }
	
	private Dialog createNoMoreCardsDialog() {
	    final CharSequence[] items = {"See these cards again", 
	            "Choose new cards"};

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("No more cards");
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int item) {
	            if (items[item].equals("See these cards again")) {
                    ch.startOver();
                    if (ch.isAskCardOrder()) {
                        showDialog(DIALOG_SELECT_CARD_ORDER);
                    }
                    showFirstCard();
                } else if (items[item].equals("Choose new cards")) {
	                chooseCardSet();
	            }
	        }
	    });    
	    AlertDialog ad = builder.create();
	    return ad;
	}
	
    private Dialog createSelectCardOrderDialog() {
        final CharSequence[] items = resources.getStringArray(
                R.array.preferences_default_card_order_entries);
//        {"Smart mode (recommended)", "Random", "In order"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.card_order_dialog,
                (ViewGroup) findViewById(R.id.card_order_dialog_layout));
        // This gets rid of the space around the android checkbox image
        layout.setPadding(0, -6, 0, -10);
        builder.setView(layout);
        
        final CheckBox checkBox = (CheckBox) layout.findViewById(
                R.id.card_order_dialog_checkbox);

        builder.setTitle("Select card order");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(
                        R.string.card_order_smart_entry))) {
                    ch.startOver();
                    showFirstCard();
                } else if (items[item].equals(getString(
                        R.string.card_order_random_entry))) {
                    ch.setCardOrder(getString(R.string.card_order_random));
                    ch.startOver();
                    showFirstCard();
                } else if (items[item].equals(getString(
                        R.string.card_order_in_order_entry))) {
                    ch.setCardOrder(getString(R.string.card_order_in_order));
                    ch.startOver();
                    showFirstCard();
                }
                if (checkBox.isChecked()) {
                    ch.setAskCardOrder(false);
                }
            }
        });
        
/*
        // Add an option to save the selection as default
        LinearLayout linearLayout = new LinearLayout(this);
        // This gets rid of the space around the android checkbox image
        linearLayout.setPadding(0, -6, 0, -10);
        CheckBox checkBox = new CheckBox(this);
        TextView textView = new TextView(this);
        textView.setText("Save as default");
        linearLayout.addView(checkBox);
        linearLayout.addView(textView);
        builder.setView(linearLayout);
*/

        AlertDialog ad = builder.create();
        return ad;
    }
	
	private void chooseCardSet() {
	    Intent intent = new Intent(this, ChooseCardSet.class);
        startActivityForResult(intent, CHOOSE_CARD_SET);
	}
		
	/**
	 * Given a view, a word, and a language, shows the word in the view and 
	 * formats it depending on the language
	 */
	private void showWord(TextView thisView, Map<String, String> thisWord, String thisLang) {
		Log.d(TAG, "showWord called, thisLang: " + thisLang);
		currentLang = thisLang;
		if (thisLang.equals("english")) {
			Log.d(TAG, "showWord, showing english");
			thisView.setTextSize(42f);
			thisView.setText(thisWord.get(thisLang));
		} else if (thisLang.equals("arabic")) {
			Log.d(TAG, "showWord, showing arabic");
			thisView.setTextSize(56f);
			thisView.setText(ArabicUtilities.reshape(thisWord.get(thisLang)));
		}
	}
	
	private void showWord(Map<String, String> thisWord) {
    	// store the ID and status of the current Word
    	currentCardId = currentWord.get("ID");
    	currentCardStatus = stringToInteger(currentWord.get("status"));
    	
//
    	Log.d(TAG, "showWord: currentWord=" + currentWord);
    	ViewGroup currentLayout = (RelativeLayout)vf.getCurrentView();
    	currentView = (TextView) currentLayout.getChildAt(0);
    	
    	ImageView knownCheck = (ImageView) currentLayout.getChildAt(1);
    	// if the current card is marked as "known"
		if (currentCardStatus == 3) {
			// show the check
			knownCheck.setImageResource(R.drawable.btn_check_buttonless_on);
		} else {
			// otherwise remove the check
			knownCheck.setImageResource(R.drawable.btn_check_buttonless_off);
		}
    	
    	showWord(currentView, currentWord, defaultLang);
	}
	
	private void flipCard() {
		if (currentLang.equals("english")) {
			showWord(currentView, currentWord, "arabic");
		} else if (currentLang.equals("arabic")) {
			showWord(currentView, currentWord, "english");
		}
	}
	
	/*
	private void loadViews() {
		Log.d(TAG, "loadViews called");
		//TextView thisView = (TextView) vf.getChildAt(vf.getDisplayedChild() -1);
		//vf.getChildAt(vf.getDisplayedChild() - 1); // previous
		//vf.getChildAt(vf.getDisplayedChild() + 1); // next
		
		ViewGroup currentLayout = (RelativeLayout)vf.getCurrentView();
//		int currentLayoutId = currentLayout.getId();
		currentView = (TextView) currentLayout.getChildAt(0);
// TODO: get current (prob next) card
		currentWord = getCurrentWord();
		showWord(currentView, currentWord);

		// TODO: pretty sure this won't work, but we'll need to figure it out at some point
/*		
		ViewGroup leftLayout = (RelativeLayout)findViewById(currentLayoutId - 1);
		TextView leftView = (TextView)leftLayout.getChildAt(0);
		Map<String, String> prevWord = getWordAtPosition(cursorPosition - 1);
		showWord(leftView, prevWord);
		
		ViewGroup rightLayout = (RelativeLayout)findViewById(currentLayoutId + 1);
		TextView rightView = (TextView)rightLayout.getChildAt(0);
		Map<String, String> nextWord = getWordAtPosition(cursorPosition + 1);
		showWord(rightView, nextWord);
*/
		
/*
		ViewGroup currentLayout = (RelativeLayout)vf.getCurrentView();
		
		vf.
		
		ViewGroup leftLayout = (RelativeLayout)vf.getLeft();
	
		
		ViewGroup thisLayout = (RelativeLayout)vf.getCurrentView();
//		int Id = thisView.getId();
//		Log.d(TAG, "onSingleTapUp: Id:" + Id);
		TextView thisView = (TextView) thisLayout.getChildAt(0);
		thisView.setText("success!");
*/
/*
	}
*/
	
	/**
	 * function to reload the current card in case any preferences have changed
	 */
	private void reshowCurrentCard() {
	    showWord(currentWord);
	}
	
	private void showFirstCard() {
		currentWord = ch.nextCard();
		// the first card should only be empty if the user picked the set of
		// unkown cards, and there aren't any cards marked as unkown yet
		if (currentWord.isEmpty()) {
		    showDialog(DIALOG_NO_UNKNOWN_CARDS);
		} else {
		    showWord(currentWord);
		}
	}
	
	private void showNextCard(String direction) {
    	// update the status of the current card
    	ch.updateCardStatus(currentCardId, currentCardStatus, direction);
    	// get the next one
    	nextWord = ch.nextCard();
    	
    	if (nextWord.isEmpty()) {
    	    showDialog(DIALOG_NO_MORE_CARDS);
    	} else {
    	    currentWord = nextWord;
    	    // only show the right animation if there's a next word
    	    vf.setInAnimation(slideLeftIn);
	        vf.setOutAnimation(slideLeftOut);
	        vf.showNext();
	        
    	    showWord(currentWord);
    	}
	}
	
	private void showPrevCard() {
    	nextWord = ch.prevCard();
    	
    	// make sure there's a previous word to show
    	if (nextWord.isEmpty()) {
// TODO: if back is clicked a bunch of times this will show a bunch of times (but even as you're browsing next)
    		Toast.makeText(getApplicationContext(), "No previous cards!", Toast.LENGTH_SHORT).show();
    	} else {
    	    currentWord = nextWord;
    		// only show the left animation if there's a previous word
    		vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
        	vf.showPrevious();
        	
    		showWord(currentWord);
    	}
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
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	showPrevCard();
                	return true;
                }
                */
            	// from http://stackoverflow.com/questions/4098198/adding-fling-gesture-to-an-image-view-android
            	// right to left
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	showNextCard("right");
                	return true;
                // left to right
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	showPrevCard();
                	return true;
                }
                // bottom to top
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	showNextCard("up");
                    return true;
                // top to bottom
                }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	showNextCard("down");
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
    
    static int stringToInteger(String s) {
    	try {
    		int i = Integer.parseInt(s.trim());
    		return i;
    	} catch (NumberFormatException e) {
    		Log.d(TAG, "stringToInteger: error: " + e.getMessage());
    		return 0;
    	}
    }
}
