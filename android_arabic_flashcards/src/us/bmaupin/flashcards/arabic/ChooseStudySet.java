package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChooseStudySet extends ListActivity {
	private static final int DIALOG_CREATE_STUDY_SET = 0;
	private static final int REQUEST_CARD_SET_BROWSE = 0;
    private static final int REQUEST_CARD_SET_CREATE = 1;
    private static final String TAG = "ChooseStudySet";
    
    // string to hold the new study set name based on set and subset
    private String newStudySetName = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.choose_study_set);
        
        Button browseCardsButton = (Button)findViewById(
        		R.id.study_set_button_browse_cards);
        browseCardsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, ChooseCardSet.class);
                startActivityForResult(intent, REQUEST_CARD_SET_BROWSE);
            }
        });
        
        Button createNewButton = (Button)findViewById(
        		R.id.study_set_button_create_new);
        createNewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, ChooseCardSet.class);
                startActivityForResult(intent, REQUEST_CARD_SET_CREATE);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult()");
        
        switch(requestCode) {
            case (REQUEST_CARD_SET_BROWSE) :
                if (resultCode == Activity.RESULT_OK) {
                    String cardSet = data.getStringExtra(ChooseCardSet.EXTRA_CARD_SET);
                    Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
                    
/*                    
                    if (data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET) == null) {
//                        ch.loadCardSet(cardSet);
                    } else {
//                        ch.loadCardSet(cardSet, data.getStringExtra(
//                                ChooseCardSet.CARD_SUBSET));
                    }
*/
                    
//                    if (ch.isAskCardOrder()) {
//                        showDialog(DIALOG_SELECT_CARD_ORDER);
//                    } else {
//                        showFirstCard();
//                    }
                    
                    Intent intent = new Intent(this, BrowseCards.class);
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SET));
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SUBSET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET));
                    startActivity(intent);
                    
//                    startActivity(new Intent(this, FreeMode.class));
                }
            	break;
            case (REQUEST_CARD_SET_CREATE) :
                if (resultCode == Activity.RESULT_OK) {
                    String cardSet = data.getStringExtra(
                    		ChooseCardSet.EXTRA_CARD_SET);
                    Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
                    
                    // create a study set name to prefill our create study set dialog
                    newStudySetName = data.getStringExtra(
                    		ChooseCardSet.EXTRA_CARD_SET);
                    
                    if (data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET) != 
                    		null) {
                    	newStudySetName += " - " + data.getStringExtra(
                    			ChooseCardSet.EXTRA_CARD_SUBSET);
                    }
                    
/*                    
                    Intent intent = new Intent(this, BrowseCards.class);
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SET));
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SUBSET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET));
                    startActivity(intent);
*/
                    showDialog(DIALOG_CREATE_STUDY_SET);
                }
                break;
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
        	case R.id.menu_help:
        		startActivity(new Intent(this, Help.class));
        		return true;
        	case R.id.menu_settings:
        		startActivity(new Intent(this, Preferences.class));
        		return true;
        	case R.id.menu_search:
        	    onSearchRequested();
        	    return true;
        	// uncomment this (and the res/menu/menu.xml entry) for testing
        	/*
        	case R.id.menu_choose_card:
        	    showDialog(DIALOG_TESTING_CHOOSE_CARDS);
        	    return true;
        	*/
    	}
    	return false;
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
	            return createCreateStudySetDialog();
        }
        return null;
	}
    
    
    
    @Override
	protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
//	        	AlertDialog createStudySetDialog = (AlertDialog)dialog;
//	        	createStudySetDialog.setMessage(m_dlgMsg);

	        	
	            EditText et = (EditText) findViewById(
	            		R.id.dialog_create_study_set_name);
	            et.setText(newStudySetName);
	    }
	}

	private Dialog createCreateStudySetDialog() {
        View layout = LayoutInflater.from(this).inflate(
        		R.layout.dialog_create_study_set, 
        		(ViewGroup) findViewById(R.id.dialog_create_study_set_layout));
/*        
        EditText et = (EditText) layout.findViewById(
        		R.id.dialog_create_study_set_name);
        et.setText(newStudySetName);
*/        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout)
               .setCancelable(true)
        	   .setTitle(getString(R.string.dialog_create_study_set_title))
               .setPositiveButton(getString(
            		   R.string.dialog_create_study_set_positive_button), 
            		   new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
//		                MyActivity.this.finish();
		           }
               })
	           .setNegativeButton(getString(
	        		   R.string.dialog_create_study_set_negative_button), 
	        		   new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });

        AlertDialog ad = builder.create();
        return ad;
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
