package us.bmaupin.flashcards.arabic;

import java.util.ArrayList;
import java.util.List;

import us.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import us.bmaupin.flashcards.arabic.data.CardProvider;
import us.bmaupin.flashcards.arabic.data.CardQueryHelper;
import us.bmaupin.flashcards.arabic.data.StudySetDatabaseHelper;
import us.bmaupin.flashcards.arabic.data.StudySetHelper;
import us.bmaupin.flashcards.arabic.data.StudySetProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ChooseStudySet extends FragmentActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "ChooseStudySet";

    private static final int DIALOG_CREATE_STUDY_SET = 0;
    private static final int DIALOG_CONFIRM_DELETE_STUDY_SET = 1;
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
    private Cursor cursor;
    private ListView lv;
    // the card set of a new study set
    private String newStudySetCardSet = "";
    // the card subset of a new study set
    private String newStudySetCardSubset = "";
    // string to hold the new study set name based on set and subset
    private String newStudySetName = "";
    // ID of study set to delete
    private String studySetToDelete;
    
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
// TODO
/*        
        adapter = new StudySetAdapter(this,
                R.layout.choose_study_set_row,
                R.id.study_set_title,
                new ArrayList<ArrayList<String>>());
*/
        adapter = new StudySetCursorAdapter(this,
                R.layout.choose_study_set_row,
                null,
                new String[] {StudySetDatabaseHelper.META_SET_NAME},
                new int[] {R.id.study_set_title,
                        R.id.study_set_due,
                        R.id.study_set_new},
                0);
        
        lv.setAdapter(adapter);
        
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
                    
                    newStudySetCardSet = data.getStringExtra(
                            Cards.EXTRA_CARD_GROUP);
                    newStudySetCardSubset = data.getStringExtra(
                            Cards.EXTRA_CARD_SUBGROUP);
                    
                    // create a study set name to prefill our create study set dialog
                    newStudySetName = newStudySetCardSet;
                    
                    if (newStudySetCardSubset != null) {
                    	newStudySetName += " - " + newStudySetCardSubset;
                    }

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
        // make the cursor available to the rest of the class
//        cursor = data;
        
        // clear out the empty view for now, so it doesn't pop up
// TODO
/*
        lv.setEmptyView(null);
        adapter.clear();
        new LoadListDataTask().execute(data);
*/
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) 
                item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.choose_study_set_delete_study_set:
// TODO            
//            studySetToDelete = adapter.getItem(info.position).get(0);
            showDialog(DIALOG_CONFIRM_DELETE_STUDY_SET);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    private void createStudySet(String studySetName, String language) {
        ContentValues cv=new ContentValues();
        cv.put(StudySetDatabaseHelper.META_SET_NAME, studySetName);
        cv.put(StudySetDatabaseHelper.META_CARD_GROUP, newStudySetCardSet);
        cv.put(StudySetDatabaseHelper.META_CARD_SUBGROUP, newStudySetCardSubset);
        cv.put(StudySetDatabaseHelper.META_SET_LANGUAGE, language);
        // this needs a default non-null value
        cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT_DATE, "0");
        
        getContentResolver().insert(
                StudySetProvider.CONTENT_URI_META,
                cv);
    }
    
    private void deleteStudySet(String id) {
        getContentResolver().delete(
                ContentUris.withAppendedId(
                        StudySetProvider.CONTENT_URI_META,
                        Cards.stringToInteger(id)),
                null, null);
        Toast.makeText(getApplicationContext(), 
                R.string.choose_study_set_study_set_deleted, 
                Toast.LENGTH_SHORT).show();
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
    }
    
    class StudySetCursorAdapter extends SimpleCursorAdapter {
/*
        private Cursor c;
        private String[] from;
        private int layout;
        private LayoutInflater mInflater;
        private int[] to;
*/
        
        public StudySetCursorAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
/*            
            // Cache the LayoutInflater to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            
            this.c = c;
            this.from = from;
            this.layout = layout;
            this.to = to;
*/
        }
/*
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
// TOD implement google's holder technique for faster adapters
            ViewHolder holder;
            
            View v = mInflater.inflate(layout, null);
            
            if (c != null) { 
                ((TextView) v.findViewById(to[0])).setText(c.getString(c.getColumnIndex(from[0])));
            }
            
            return v;
        }
*/

        /* 
         * taken from android 4.0.3_r1 source
         * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.3_r1/android/widget/SimpleCursorAdapter.java
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
// TODO
// TODO
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
// TODO put this (or whatever the final text is) into a constant            
            setViewText((TextView) view.findViewById(to[1]), "Loading...");
            setViewText((TextView) view.findViewById(to[2]), "");
            
            // update the study set new and due card counts
            new LoadDueCardsTask().execute(cursor.getInt(0), 
                    view.findViewById(to[1]),
                    view.findViewById(to[2])); 
            
/*            
            for (int i = 0; i < count; i++) {
                final View v = view.findViewById(to[i]);
                if (v != null) {
                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, cursor, from[i]);
                    }

                    if (!bound) {
                        String text = cursor.getString(from[i]);
                        if (text == null) {
                            text = "";
                        }

                        if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else if (v instanceof ImageView) {
                            setViewImage((ImageView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                    " view that can be bounds by this SimpleCursorAdapter");
                        }
                    }
                }
            }
*/
        }
        
        
    }
    
    private class StudySetAdapter extends ArrayAdapter<ArrayList<String>> {
        private LayoutInflater mInflater;
        private int resource;
        private final List<ArrayList<String>> studySets;

        public StudySetAdapter(Context context, int resource,
                int textViewResourceId, List<ArrayList<String>> studySets) {
            // textViewResourceId won't really be used since we override getView
            super(context, resource, textViewResourceId, studySets);
            
            // Cache the LayoutInflater to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            this.resource = resource;
            this.studySets = studySets;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // implement google's holder technique for faster adapters
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = mInflater.inflate(resource, null);
                holder = new ViewHolder();
                holder.tv1 = (TextView) convertView.findViewById(
                        R.id.study_set_title);
                holder.tv2 = (TextView) convertView.findViewById(
                        R.id.study_set_due);
                holder.tv3 = (TextView) convertView.findViewById(
                        R.id.study_set_new);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.tv1.setText(studySets.get(position).get(1));
            holder.tv2.setText(studySets.get(position).get(2) + 
                    getString(R.string.choose_study_set_cards_due));
            holder.tv3.setText(studySets.get(position).get(3) + 
                    getString(R.string.choose_study_set_cards_new));
            
            return convertView;
        }
    }
    
    private static class ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
    }

// TODO
// TODO
// http://stackoverflow.com/questions/4885350/how-to-pass-different-objects-as-a-parameter-to-asyctask
    private class LoadDueCardsTask extends AsyncTask<Object, Void, Integer[]> {
        private TextView tv1;
        private TextView tv2;
        
        @Override
        protected Integer[] doInBackground(Object... studySetItems) {
            int studySetId = (Integer) studySetItems[0];
            tv1 = (TextView) studySetItems[1];
            tv2 = (TextView) studySetItems[2];
            
            Integer[] dueCardsCount = new Integer[2];
            
            Log.d(TAG, "LoadDueCards: studySetId=" + studySetId);
            Log.d(TAG, "LoadDueCards: tv text=" + tv1.getText());
            
            // get the count of due cards
            String selection = StudySetDatabaseHelper.SET_DUE_TIME + 
                    " < " + System.currentTimeMillis();
            Cursor studySetCursor = getContentResolver().query(
                    // specify the study set ID and a limit
                    ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                            studySetId).buildUpon().appendQueryParameter(
                            StudySetProvider.QUERY_PARAMETER_LIMIT,
                            "" + Cards.MAX_CARDS_TO_SHOW).build(),
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
                int newCardsDue = Cards.MAX_NEW_CARDS_TO_SHOW - 
                        (studySetCount - initialStudySetCount);
                Log.d(TAG, "max new cards to show=" + newCardsDue);
                
                // get the total count of cards in the card group
                int cardGroupCount = 0;
                CardQueryHelper cqh = new CardQueryHelper(
                        ChooseStudySet.this,
                        studySetCursor.getString(0),
                        studySetCursor.getString(1));
                studySetCursor.close();
                studySetCursor = getContentResolver().query(
                        CardProvider.CONTENT_URI,
                        new String[] {CardDatabaseHelper.COUNT},
                        cqh.getSelection(),
                        cqh.getSelectionArgs(),
                        cqh.getSortOrder());
                if (studySetCursor.moveToFirst()) {
                    cardGroupCount = studySetCursor.getInt(0);
                }
                
                // can't have more cards due than are in the card group
                if (newCardsDue > (cardGroupCount - studySetCount)) {
                    newCardsDue = cardGroupCount - studySetCount;
                }
                dueCardsCount[1] = newCardsDue;
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
    
    private class LoadListDataTask extends AsyncTask<Cursor, ArrayList<String>, Void> {
        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Cursor... params) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(cursor.getString(0));
                    list.add(cursor.getString(1));
                    
                    // get the count of due cards
                    String selection = StudySetDatabaseHelper.SET_DUE_TIME + 
                            " < " + System.currentTimeMillis();
                    Cursor studySetCursor = getContentResolver().query(
                            // specify the study set ID and a limit
                            ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                                    cursor.getInt(0)).buildUpon().appendQueryParameter(
                                    StudySetProvider.QUERY_PARAMETER_LIMIT,
                                    "" + Cards.MAX_CARDS_TO_SHOW).build(),
                            new String[] {StudySetDatabaseHelper.COUNT},
                            selection,
                            null,
                            StudySetDatabaseHelper.SET_DUE_TIME);

                    if (studySetCursor.moveToFirst()) {
                        list.add(studySetCursor.getString(0));
                    }
                    studySetCursor.close();
                    
                    // figure out how many new cards we've already seen today
                    int studySetCount = StudySetHelper.getStudySetCount(
                            ChooseStudySet.this, cursor.getInt(0));
                    int initialStudySetCount = 
                        StudySetHelper.maybeUpdateInitialStudySetCount(
                                ChooseStudySet.this, cursor.getInt(0), 
                                studySetCount, cursor.getString(5), 
                                cursor.getInt(6));
                    int newCardsDue = Cards.MAX_NEW_CARDS_TO_SHOW - 
                            (studySetCount - initialStudySetCount);
                    Log.d(TAG, "max new cards to show=" + newCardsDue);
                    
                    // get the total count of cards in the card group
                    int cardGroupCount = 0;
                    CardQueryHelper cqh = new CardQueryHelper(
                            ChooseStudySet.this,
                            cursor.getString(2),
                            cursor.getString(3));
                    studySetCursor = getContentResolver().query(
                            CardProvider.CONTENT_URI,
                            new String[] {CardDatabaseHelper.COUNT},
                            cqh.getSelection(),
                            cqh.getSelectionArgs(),
                            cqh.getSortOrder());
                    if (studySetCursor.moveToFirst()) {
                        cardGroupCount = studySetCursor.getInt(0);
                    }
                    studySetCursor.close();
                    
                    // can't have more cards due than are in the card group
                    if (newCardsDue > (cardGroupCount - studySetCount)) {
                        newCardsDue = cardGroupCount - studySetCount;
                    }
                    Log.d(TAG, "newCardsDue=" + newCardsDue);
                    
                    list.add("" + newCardsDue);
                    
                    publishProgress(list);
                    cursor.moveToNext();
                }
            }
            return(null);
        }
        
        @Override
        protected void onProgressUpdate(ArrayList<String>... item) {
//            adapter.add(item[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            // set the empty view after the asynctask is done
            View ev = findViewById(R.id.study_set_empty);
            lv.setEmptyView(ev);
        }
    }
}
