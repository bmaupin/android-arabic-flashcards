package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ChooseStudySet extends ListActivity {
    private static final String[] COLUMNS = new String[] {
            StudySetDatabaseHelper._ID, 
            StudySetDatabaseHelper.META_SET_NAME};
    private static final int DIALOG_CREATE_STUDY_SET = 0;
    private static final int DIALOG_CONFIRM_DELETE_STUDY_SET = 1;
	private static final int REQUEST_CARD_SET_BROWSE = 0;
    private static final int REQUEST_CARD_SET_CREATE = 1;
    private static final String TAG = "ChooseStudySet";
    
    SimpleCursorAdapter adapter;
    Cursor cursor;
    private SQLiteDatabase db;
    private StudySetDatabaseHelper dbHelper;
    // the card set of a new study set
    private String newStudySetCardSet = "";
    // the card subset of a new study set
    private String newStudySetCardSubset = "";
    // string to hold the new study set name based on set and subset
    private String newStudySetName = "";
    // ID of study set to delete
    private long studySetToDelete;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.choose_study_set);
        
        Button browseCardsButton = (Button) findViewById(
        		R.id.study_set_button_browse_cards);
        browseCardsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, 
                        ChooseCardSet.class);
                startActivityForResult(intent, REQUEST_CARD_SET_BROWSE);
            }
        });
        
        Button createNewButton = (Button) findViewById(
        		R.id.study_set_button_create_new);
        createNewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, 
                        ChooseCardSet.class);
                startActivityForResult(intent, REQUEST_CARD_SET_CREATE);
            }
        });
        
        dbHelper = new StudySetDatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
        cursor = db.query(StudySetDatabaseHelper.META_TABLE_NAME, 
                COLUMNS, null, null, null, null, null);
        startManagingCursor(cursor);
        
        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {StudySetDatabaseHelper.META_SET_NAME},
                new int[] { android.R.id.text1 });
        
        // Bind to our new adapter.
        setListAdapter(adapter);
        
        ListView list = getListView();
        registerForContextMenu(list);
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
                    
                    newStudySetCardSet = data.getStringExtra(
                            ChooseCardSet.EXTRA_CARD_SET);
                    newStudySetCardSubset = data.getStringExtra(
                            ChooseCardSet.EXTRA_CARD_SUBSET);
                    
                    // create a study set name to prefill our create study set dialog
                    newStudySetName = newStudySetCardSet;
                    
                    if (newStudySetCardSubset != null) {
                    	newStudySetName += " - " + newStudySetCardSubset;
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
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // set a title (this can't be done in xml)
        menu.setHeaderTitle(R.string.choose_study_set_context_menu_title);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.choose_study_set_context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.choose_study_set_delete_study_set:
            studySetToDelete = info.id;
            showDialog(DIALOG_CONFIRM_DELETE_STUDY_SET);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    private void createStudySet(String studySetName, String language) {
        ContentValues cv=new ContentValues();
        cv.put(StudySetDatabaseHelper.META_SET_NAME, studySetName);
        cv.put(StudySetDatabaseHelper.META_SET_CATEGORY, newStudySetCardSet);
        cv.put(StudySetDatabaseHelper.META_SET_SUBCATEGORY, newStudySetCardSubset);
        cv.put(StudySetDatabaseHelper.META_SET_LANGUAGE, language);
        
        long newStudySetId = db.insert(
                StudySetDatabaseHelper.META_TABLE_NAME, 
                StudySetDatabaseHelper._ID, cv);
        
        if (newStudySetId != -1) {
            dbHelper.createNewStudySet(db, newStudySetId);
            
        } else {
            Log.e(TAG, String.format("ERROR: insert new study set failed. " +
            		"name=%s, language=%s", studySetName, language));
        }
        
        updateStudySetList();
        
        
//        Cursor cursor = studySetDb.query(StudySetDatabaseHelper.META_TABLE_NAME, 
//                null, language, null, language, language, language);
        
        /*
         * 1. insert new row into studyset_meta
         * 2. get the ID from that row
         * 3. create a new study set table with that ID
         */
    }
    
    private void deleteStudySet(long id) {
        dbHelper.deleteStudySet(db, id);
        Toast.makeText(getApplicationContext(), 
                R.string.choose_study_set_study_set_deleted, 
                Toast.LENGTH_SHORT).show();
        
        updateStudySetList();
    }
    
    private void updateStudySetList() {
        // update the list of study sets
        cursor = db.query(StudySetDatabaseHelper.META_TABLE_NAME, 
                COLUMNS, null, null, null, null, null);
        adapter.changeCursor(cursor);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
	            return createCreateStudySetDialog();
            case DIALOG_CONFIRM_DELETE_STUDY_SET:
                return createConfirmDeleteStudySetDialog();
        }
        return null;
	}
    
    @Override
	protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
	            EditText et = (EditText) dialog.findViewById(
	            		R.id.dialog_create_study_set_name);
	            et.setText(newStudySetName);
	            break;
	    }
	}

	private Dialog createCreateStudySetDialog() {
        final View layout = LayoutInflater.from(this).inflate(
        		R.layout.dialog_create_study_set, 
        		(ViewGroup) findViewById(R.id.dialog_create_study_set_layout));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout)
               .setCancelable(true)
        	   .setTitle(getString(R.string.dialog_create_study_set_title))
               .setPositiveButton(getString(
            		   R.string.dialog_create_study_set_positive_button), 
            		   new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // yikes these are ugly, but they work...
		               String studySetName = ((EditText) layout.findViewById(
                               R.id.dialog_create_study_set_name)).getText().
                               toString();
		               String language = ((RadioButton) layout.findViewById(
		                       (((RadioGroup) layout.findViewById(
                               R.id.dialog_create_study_set_language)).
                               getCheckedRadioButtonId()))).getText().
                               toString();
		               
		               createStudySet(studySetName, language);
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
	
	private Dialog createConfirmDeleteStudySetDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.choose_study_set_confirm_delete_study_set)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       deleteStudySet(studySetToDelete);
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
        
        // clean up after ourselves
        db.close();
        dbHelper.close();
    }
}
