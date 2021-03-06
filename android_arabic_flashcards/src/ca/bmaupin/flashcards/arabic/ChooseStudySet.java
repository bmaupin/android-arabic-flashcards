package ca.bmaupin.flashcards.arabic;

import ca.bmaupin.flashcards.arabic.data.StudySetDatabaseHelper;
import ca.bmaupin.flashcards.arabic.data.StudySetHelper;
import ca.bmaupin.flashcards.arabic.data.StudySetProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ChooseStudySet extends BaseActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "ChooseStudySet";

    private static final int DIALOG_CREATE_STUDY_SET = 0;
    private static final int DIALOG_CONFIRM_DELETE_STUDY_SET = 1;
    private static final int DIALOG_RENAME_STUDY_SET = 2;
	private static final int REQUEST_CARD_SET_BROWSE = 0;
    private static final int REQUEST_CARD_SET_CREATE = 1;
    
    private static final String[] PROJECTION = new String[] {
        StudySetDatabaseHelper._ID, 
        StudySetDatabaseHelper.META_SET_NAME,
        StudySetDatabaseHelper.META_CARD_GROUP,
        StudySetDatabaseHelper.META_CARD_SUBGROUP,
        StudySetDatabaseHelper.META_SET_LANGUAGE,
        StudySetDatabaseHelper.META_INITIAL_COUNT_DATE,
        StudySetDatabaseHelper.META_INITIAL_COUNT,
	};
    
    private StudySetCursorAdapter adapter;
    // view to show while study sets are loading, or if there are none
    private View emptyView;
    private ListView lv;
    // the card set of a new study set
    private String newStudySetCardGroup = "";
    // the card subset of a new study set
    private String newStudySetCardSubgroup = "";
    // string to hold the name of a new study set based on set and subset
    private String newStudySetName = "";
    private Resources resources;
    // string to hold current study set name when renaming a study set
    private String studySetCurrentName = "";
    // ID of study set to delete
    private long studySetToDelete;
    // ID of study set to rename
    private long studySetToRename;
    
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
                        ChooseCardGroup.class);
                startActivityForResult(intent, REQUEST_CARD_SET_BROWSE);
            }
        });
        
        Button createNewButton = (Button) findViewById(
        		R.id.study_set_button_create_new);
        createNewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, 
                        ChooseCardGroup.class);
                startActivityForResult(intent, REQUEST_CARD_SET_CREATE);
            }
        });
        
        lv = (ListView) findViewById(android.R.id.list);

        adapter = new StudySetCursorAdapter(this,
                R.layout.choose_study_set_row,
                null,
                new String[] {StudySetDatabaseHelper.META_SET_NAME},
                new int[] {R.id.study_set_title,
                        R.id.study_set_due,
                        R.id.study_set_new},
                0);

        lv.setAdapter(adapter);
        // show a loading message while we wait for the study sets to load
        emptyView = findViewById(R.id.study_set_empty);
        ((TextView) emptyView.findViewById(R.id.study_set_empty_text1)).setText(
                R.string.choose_study_set_loading_message);
        ((TextView) emptyView.findViewById(R.id.study_set_empty_text2)).setText(
                "");
        lv.setEmptyView(emptyView); 
        
        getSupportLoaderManager().initLoader(0, null, this);
        
        registerForContextMenu(lv);
        
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
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // force the adapter to reload the study set list in case anything's 
        // changed
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult()");
        
        switch(requestCode) {
            case (REQUEST_CARD_SET_BROWSE) :
                if (resultCode == Activity.RESULT_OK) {
                    String cardSet = data.getStringExtra(Cards.EXTRA_CARD_GROUP);
                    Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
                    
                    Intent intent = new Intent(this, BrowseCards.class);
                    intent.putExtra(Cards.EXTRA_CARD_GROUP, 
                            data.getStringExtra(Cards.EXTRA_CARD_GROUP));
                    intent.putExtra(Cards.EXTRA_CARD_SUBGROUP, 
                            data.getStringExtra(Cards.EXTRA_CARD_SUBGROUP));
                    startActivity(intent);
                }
            	break;
            case (REQUEST_CARD_SET_CREATE) :
                if (resultCode == Activity.RESULT_OK) {
                    String cardSet = data.getStringExtra(
                    		Cards.EXTRA_CARD_GROUP);
                    Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
                    
                    newStudySetCardGroup = data.getStringExtra(
                            Cards.EXTRA_CARD_GROUP);
                    newStudySetCardSubgroup = data.getStringExtra(
                            Cards.EXTRA_CARD_SUBGROUP);
                    
                    // create a study set name to prefill our create study set dialog
                    // if the group is parts of speech
                    if (newStudySetCardGroup.equals(getString(
                            R.string.card_group_parts_of_speech))) {
                        final String[] partsOfSpeechLong = 
                                resources.getStringArray(
                                R.array.dialog_parts_of_speech_long);
                        final String[] partsOfSpeechShort = 
                                resources.getStringArray(
                                R.array.dialog_parts_of_speech_short);
                        // use the subgroup as the study set name, and convert 
                        // it from short to long
                        newStudySetName = partsOfSpeechLong[
                                java.util.Arrays.asList(partsOfSpeechShort).
                                indexOf(newStudySetCardSubgroup)];
                    
                    // for categories just use the subgroup
                    } else if (newStudySetCardGroup.equals(getString(
                            R.string.card_group_categories))) {
                        newStudySetName = newStudySetCardSubgroup;
                    
                    // for ahlan wa sahlan - all just use ahlan wa sahlan
                    } else if (newStudySetCardGroup.equals(getString(
                            R.string.card_group_ahlan_wa_sahlan)) && 
                            newStudySetCardSubgroup.equals(getString(
                            R.string.card_group_all_chapters))) {
                        newStudySetName = newStudySetCardGroup;
                    
                    } else {
                        newStudySetName = newStudySetCardGroup;
                        if (newStudySetCardSubgroup != null) {
                            newStudySetName += " - " + newStudySetCardSubgroup;
                        }
                    }
                    
                    // arabic is the default study set language, so add "(ar)" 
                    // to the end of the study set name.  see 
                    // res/layout/dialog_create_study_set.xml
                    newStudySetName += " " + getString(
                            R.string.dialog_create_study_set_arabic_short);

                    showDialog(DIALOG_CREATE_STUDY_SET);
                }
                break;
        }
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                StudySetProvider.CONTENT_URI_META,
                PROJECTION,
                null,
                null,
                null
        );
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(data);
        
        // if there are no study sets
        if (data.getCount() == 0) {
            // set the empty view text and the empty view
            ((TextView) emptyView.findViewById(R.id.study_set_empty_text1)).
                    setText(R.string.study_set_no_results);
            ((TextView) emptyView.findViewById(R.id.study_set_empty_text2)).
                    setText(R.string.study_set_no_results_details);
            lv.setEmptyView(findViewById(R.id.study_set_empty));
        } else {
            // otherwise clear it, so it won't flash in between cursor loads
            lv.setEmptyView(null);
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) 
                item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.choose_study_set_delete_study_set:
            studySetToDelete = info.id;
            showDialog(DIALOG_CONFIRM_DELETE_STUDY_SET);
            return true;
        case R.id.choose_study_set_rename_study_set:
            studySetToRename = info.id;
            studySetCurrentName = ((TextView) info.targetView.findViewById(
                    R.id.study_set_title)).getText().toString();
            showDialog(DIALOG_RENAME_STUDY_SET);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    private void createStudySet(String studySetName, String language) {
        ContentValues cv=new ContentValues();
        cv.put(StudySetDatabaseHelper.META_SET_NAME, studySetName);
        cv.put(StudySetDatabaseHelper.META_CARD_GROUP, newStudySetCardGroup);
        cv.put(StudySetDatabaseHelper.META_CARD_SUBGROUP, newStudySetCardSubgroup);
        cv.put(StudySetDatabaseHelper.META_SET_LANGUAGE, language);
        // this needs a default non-null value
        cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT_DATE, "0");
        
        getContentResolver().insert(
                StudySetProvider.CONTENT_URI_META,
                cv);
    }
    
    private void deleteStudySet(long id) {
        getContentResolver().delete(
                ContentUris.withAppendedId(
                        StudySetProvider.CONTENT_URI_META, id),
                null, null);
        Toast.makeText(getApplicationContext(), 
                R.string.choose_study_set_study_set_deleted, 
                Toast.LENGTH_SHORT).show();
    }
    
    private void renameStudySet(long id, String studySetNewName) {
        ContentValues cv = new ContentValues();
        cv.put(StudySetDatabaseHelper.META_SET_NAME, 
                studySetNewName);
        getContentResolver().update(
                StudySetProvider.CONTENT_URI_META,
                cv,
                StudySetDatabaseHelper._ID + " = ? ",
                new String[] {"" + id});
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
	            return createCreateStudySetDialog();
            case DIALOG_CONFIRM_DELETE_STUDY_SET:
                return createConfirmDeleteStudySetDialog();
            case DIALOG_RENAME_STUDY_SET:
                return createRenameStudySetDialog();
        }
        return null;
	}
    
    @Override
	protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
	        case DIALOG_CREATE_STUDY_SET:
	            // this code must be here because in onCreateDialog it will
	            // only get set once and won't change if you click a different
	            // list item and create a different dialog for it
	            EditText et = (EditText) dialog.findViewById(
	            		R.id.dialog_create_study_set_name);
	            et.setText(newStudySetName);
	            break;
	        case DIALOG_RENAME_STUDY_SET:
	            // set the text of the text box to the current study set name
	            et = (EditText) (EditText) dialog.findViewById(
	                    R.id.dialog_rename_study_set_name);
	            et.setText(studySetCurrentName);
	    }
	}

	private Dialog createCreateStudySetDialog() {
        final View layout = LayoutInflater.from(this).inflate(
        		R.layout.dialog_create_study_set, 
        		(ViewGroup) findViewById(R.id.dialog_create_study_set_layout));
        
        final RadioGroup studySetLanguage = (RadioGroup) layout.findViewById(
                R.id.dialog_create_study_set_language);
        final EditText studySetName = (EditText) layout.findViewById(
                R.id.dialog_create_study_set_name);

        // if one of the study set language radio buttons is clicked
        studySetLanguage.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int checkedId) {
                // the study set name with "(en)" at the end instead of "(ar)"
                String newStudySetNameEnglish = newStudySetName.substring(0, 
                        newStudySetName.length() - 4) + getString(
                        R.string.dialog_create_study_set_english_short);
                
                // if arabic is checked
                if (checkedId == R.id.dialog_create_study_set_arabic) {
                    // if study set name is the default english study set name
                    if (studySetName.getText().toString().equals(
                            newStudySetNameEnglish)) {
                        // change it to the default study set name
                        studySetName.setText(newStudySetName);
                    }
                // if english is checked
                } else if (checkedId == R.id.dialog_create_study_set_english) {
                    // if the study set name hasn't changed from the default
                    if (studySetName.getText().toString().equals(
                            newStudySetName)) {
                        // replace it with the default english study set name
                        studySetName.setText(newStudySetNameEnglish);
                    }
                }
            }
        });

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
	
	private Dialog createRenameStudySetDialog() {
	    final View layout = getLayoutInflater().inflate(
	            R.layout.dialog_rename_study_set, 
                (ViewGroup) findViewById(R.id.dialog_rename_study_set_name));
	    
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_study_set_rename_study_set)
               .setMessage(R.string.dialog_rename_study_set_message)
               .setView(layout)
               .setCancelable(true)
               .setPositiveButton(getString(
                       R.string.dialog_rename_study_set_positive_button), 
                       new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       String studySetNewName = ((EditText) layout.findViewById(
                               R.id.dialog_rename_study_set_name)).getText().
                               toString();
                       // make sure the new name is different before we do anything
                       if (!studySetNewName.equals(studySetCurrentName)) {
                           renameStudySet(studySetToRename, studySetNewName);
                       }
                   }
               })
               .setNegativeButton(getString(
                       R.string.dialog_rename_study_set_negative_button), 
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
    
    class StudySetCursorAdapter extends SimpleCursorAdapter {
        private final String loadingMessage = getString(
                R.string.choose_study_set_loading_message);
        
        public StudySetCursorAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            
        }

        /* 
         * taken from android 4.0.3_r1 source
         * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.3_r1/android/widget/SimpleCursorAdapter.java
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewBinder binder = getViewBinder();
            final int[] from = mFrom;
            final int[] to = mTo;
            
            // update the study set title
            final View v = view.findViewById(to[0]);
            if (v != null) {
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[0]);
                }

                if (!bound) {
                    String text = cursor.getString(from[0]);
                    if (text == null) {
                        text = "";
                    }
                    setViewText((TextView) v, text);
                }
            }
            setViewText((TextView) view.findViewById(to[1]), loadingMessage);
            setViewText((TextView) view.findViewById(to[2]), "");
            
            // update the study set new and due card counts
            new LoadDueCardsTask().execute(cursor.getInt(0), 
                    view.findViewById(to[1]),
                    view.findViewById(to[2])); 
        }
    }

    private class LoadDueCardsTask extends AsyncTask<Object, Void, Integer[]> {
        private TextView tv1;
        private TextView tv2;
        
        @Override
        protected Integer[] doInBackground(Object... studySetItems) {
            int studySetId = (Integer) studySetItems[0];
            tv1 = (TextView) studySetItems[1];
            tv2 = (TextView) studySetItems[2];
            
            // the first item in this array is the number of due cards
            // the second item is the number of new cards seen today
            Integer[] dueCardsCount = new Integer[2];
            
            Log.d(TAG, "LoadDueCards: studySetId=" + studySetId);
            Log.d(TAG, "LoadDueCards: tv text=" + tv1.getText());
            
            // get the count of due cards
            String selection = StudySetDatabaseHelper.SET_DUE_TIME + 
                    " < " + System.currentTimeMillis();
            Cursor studySetCursor = getContentResolver().query(
                    // specify the study set ID and a limit
                    ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                            studySetId),
                    new String[] {StudySetDatabaseHelper.COUNT},
                    selection,
                    null,
                    StudySetDatabaseHelper.SET_DUE_TIME);
        
            if (studySetCursor.moveToFirst()) {
                dueCardsCount[0] = studySetCursor.getInt(0);
            }
            studySetCursor.close();
            
            // figure out how many new cards we've already seen today
            int studySetCount = StudySetHelper.getStudySetCount(
                    ChooseStudySet.this, studySetId);
            studySetCursor = getContentResolver().query(
                    StudySetProvider.CONTENT_URI_META,
                    new String[] {
                            StudySetDatabaseHelper.META_CARD_GROUP,
                            StudySetDatabaseHelper.META_CARD_SUBGROUP,
                            StudySetDatabaseHelper.META_INITIAL_COUNT_DATE,
                            StudySetDatabaseHelper.META_INITIAL_COUNT},
                    StudySetDatabaseHelper._ID + " = ? ",
                    new String[] {"" + studySetId},
                    null);
            if (studySetCursor.moveToFirst()) {
                int initialStudySetCount = 
                    StudySetHelper.maybeUpdateInitialStudySetCount(
                            ChooseStudySet.this, studySetId, 
                            studySetCount, studySetCursor.getString(2), 
                            studySetCursor.getInt(3));
                
                dueCardsCount[1] = studySetCount - initialStudySetCount;
            }
            studySetCursor.close();
            
            return dueCardsCount;
        }

        @Override
        protected void onPostExecute(Integer[] dueCardsCount) {
            adapter.setViewText(tv1, "" + dueCardsCount[0] + getString(
                    R.string.choose_study_set_cards_due));
            adapter.setViewText(tv2, "" + dueCardsCount[1] + getString(
                    R.string.choose_study_set_cards_new));
        }
    }
}
