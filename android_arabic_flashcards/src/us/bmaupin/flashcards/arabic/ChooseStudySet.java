package us.bmaupin.flashcards.arabic;

import java.util.ArrayList;
import java.util.List;

import us.bmaupin.flashcards.arabic.data.StudySetDatabaseHelper;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
        StudySetDatabaseHelper.META_SET_LANGUAGE
	};
    
// TODO
//    SimpleCursorAdapter adapter;
    private StudySetAdapter adapter;
    private Cursor cursor;
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
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
/*        
        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[] {StudySetDatabaseHelper.META_SET_NAME},
                new int[] {android.R.id.text1},
                0);
*/        
        ListView lv = (ListView) findViewById(android.R.id.list);
/*        
        // Bind to our new adapter.
        lv.setAdapter(adapter);
*/
/*        
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2,
                new ArrayList<ArrayList<String>>());
*/        
// TODO
// TODO
        adapter = new StudySetAdapter(this,
                R.layout.choose_study_set_row,
                R.id.study_set_title,
                new ArrayList<ArrayList<String>>());
        
        lv.setAdapter(adapter);
        
        getSupportLoaderManager().initLoader(0, null, this);
        
        registerForContextMenu(lv);
        
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, 
                    int position, long id) {
                                
                Intent intent = new Intent(ChooseStudySet.this, 
                        ShowStudySet.class);
                // id is the study set id
                intent.putExtra(Cards.EXTRA_STUDY_SET_ID, 
                        Cards.stringToInteger(adapter.getItem(position)
                                .get(0)));
                startActivity(intent);
            }
        });
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
//        adapter.swapCursor(data);
        // make the cursor available to the rest of the class
        cursor = data;
        
        adapter.clear();
        new LoadListDataTask().execute(data);
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
//        adapter.swapCursor(null);
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
            studySetToDelete = adapter.getItem(info.position).get(0);
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
        cv.put(StudySetDatabaseHelper.META_NEW_CARDS_DATE, "0");
        
        getContentResolver().insert(
                StudySetProvider.CONTENT_URI_META,
                cv);
        // let our adapter know we added a study set
//        adapter.notifyDataSetChanged();
// TODO
    }
    
    private void deleteStudySet(String id) {
        getContentResolver().delete(
                ContentUris.withAppendedId(
                        StudySetProvider.CONTENT_URI_META,
                        Cards.stringToInteger(id)),
                null, null);
        // let our adapter know we deleted a study set
//        adapter.notifyDataSetChanged();
// TODO
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
    
// TODO
// TODO
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
// TODO: put this into a string resource
            holder.tv2.setText(studySets.get(position).get(2) + " due");
// TODO: put this into a string resource
            holder.tv3.setText("XX new today");
            
            return convertView;
        }
    }
    
    private static class ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
    }
    
    private class StudySetAdapter2 extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<ArrayList<String>> studySets;
        private int resource;

        public StudySetAdapter2(Context context, int resource,
                List<ArrayList<String>> studySets) {
            // Cache the LayoutInflater to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.studySets = studySets;
            this.resource = resource;
        }
        
        @Override
        public int getCount() {
            return studySets.size();
        }

        @Override
        public Object getItem(int position) {
            // return the first item (the study set ID)
            return studySets.get(position).get(0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(resource, null);
            }
            
            ((TextView) convertView.findViewById(R.id.study_set_title)).setText(
                    studySets.get(position).get(1));
            ((TextView) convertView.findViewById(R.id.study_set_due)).setText(
// TODO: put this into a string resource
                    studySets.get(position).get(2) + " due");
            ((TextView) convertView.findViewById(R.id.study_set_new)).setText(
// TODO: put this into a string resource
                    "XX new today");
            
            return convertView;
        }
        
    }
    
    private class LoadListDataTask extends AsyncTask<Cursor, ArrayList<String>, Void> {
        @Override
        protected Void doInBackground(Cursor... params) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(cursor.getString(0));
                    list.add(cursor.getString(1));
                    
                    String selection = StudySetDatabaseHelper.SET_DUE_TIME + " < " + 
                    System.currentTimeMillis();
                    
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
                    
                    publishProgress(list);
                    cursor.moveToNext();
                }
            }
            return(null);
        }
        
        @Override
        protected void onProgressUpdate(ArrayList<String>... item) {
            adapter.add(item[0]);
        }
    }
}
