package us.bmaupin.flashcards.arabic.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class StudySetHelper {
    private static final String TAG = "StudySetHelper";
    
    public static int maybeUpdateInitialStudySetCount(Context context, 
            int studySetId, int studySetCount, String initialStudySetCountDate, 
            int initialStudySetCount) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                StudySetDatabaseHelper.META_NEW_CARDS_DATE_FORMAT);
        String today = simpleDateFormat.format(new Date()).toString();
        
        // if the date found in the database is today
        if (initialStudySetCountDate.equals(today)) {
            Log.d(TAG, "initialStudySetCount (from db)=" + initialStudySetCount);
            // set the initial count of cards in the study set for today
            return initialStudySetCount;
            
        // otherwise update the initial study set card count in the db
        } else {
            ContentValues cv = new ContentValues();
            cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT_DATE, 
                    today);
            cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT, 
                    studySetCount);
            
            context.getContentResolver().update(
                    StudySetProvider.CONTENT_URI_META,
                    cv,
                    StudySetDatabaseHelper._ID + " = ? ",
                    new String[] {"" + studySetId});
            initialStudySetCount = studySetCount;
            Log.d(TAG, "initialStudySetCount=studySetCount=" + initialStudySetCount);
        }
        
        return initialStudySetCount;
    }
    
    /*
     * get the total count of rows in the study set table, to be used as an
     * offset for new cards
     */
    public static int getStudySetCount(Context context, int studySetId) {
        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                        studySetId),
                new String[] {StudySetDatabaseHelper.COUNT},
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            int studySetCount = cursor.getInt(0);
            cursor.close();
            return studySetCount;
        }
        cursor.close();
        
        return 0;
    }
}
