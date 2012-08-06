package ca.bmaupin.flashcards.arabic.data;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;

// from sources/android-16/android/database/CrossProcessCursorWrapper.java

/**
 * Cursor wrapper that implements {@link CrossProcessCursor}.
 * <p>
 * If the wrapped cursor implements {@link CrossProcessCursor}, then the wrapper
 * delegates {@link #fillWindow}, {@link #getWindow()} and {@link #onMove} to it.
 * Otherwise, the wrapper provides default implementations of these methods that
 * traverse the contents of the cursor similar to {@link AbstractCursor#fillWindow}.
 * </p><p>
 * This wrapper can be used to adapt an ordinary {@link Cursor} into a
 * {@link CrossProcessCursor}.
 * </p>
 */
public class CrossProcessCursorWrapper extends CursorWrapper implements CrossProcessCursor {
    private Cursor mCursor;
    
    /**
     * Creates a cross process cursor wrapper.
     * @param cursor The underlying cursor to wrap.
     */
    public CrossProcessCursorWrapper(Cursor cursor) {
        super(cursor);
        
        mCursor = cursor;
    }

    @Override
    public void fillWindow(int position, CursorWindow window) {
        if (mCursor instanceof CrossProcessCursor) {
            final CrossProcessCursor crossProcessCursor = (CrossProcessCursor)mCursor;
            crossProcessCursor.fillWindow(position, window);
            return;
        }

        DatabaseUtils.cursorFillWindow(mCursor, position, window);
    }

    @Override
    public CursorWindow getWindow() {
        if (mCursor instanceof CrossProcessCursor) {
            final CrossProcessCursor crossProcessCursor = (CrossProcessCursor)mCursor;
            return crossProcessCursor.getWindow();
        }

        return null;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (mCursor instanceof CrossProcessCursor) {
            final CrossProcessCursor crossProcessCursor = (CrossProcessCursor)mCursor;
            return crossProcessCursor.onMove(oldPosition, newPosition);
        }

        return true;
    }
}

class DatabaseUtils {
    /**
     * Fills the specified cursor window by iterating over the contents of the cursor.
     * The window is filled until the cursor is exhausted or the window runs out
     * of space.
     *
     * The original position of the cursor is left unchanged by this operation.
     *
     * @param cursor The cursor that contains the data to put in the window.
     * @param position The start position for filling the window.
     * @param window The window to fill.
     * @hide
     */
    public static void cursorFillWindow(final Cursor cursor,
            int position, final CursorWindow window) {
        if (position < 0 || position >= cursor.getCount()) {
            return;
        }
        final int oldPos = cursor.getPosition();
        final int numColumns = cursor.getColumnCount();
        window.clear();
        window.setStartPosition(position);
        window.setNumColumns(numColumns);
        if (cursor.moveToPosition(position)) {
            do {
                if (!window.allocRow()) {
                    break;
                }
                for (int i = 0; i < numColumns; i++) {
                    final boolean success;
                    int type = 0;
                    if (cursor.isNull(i)) {
                        type = 1;
                    } else {
                        try {
                            cursor.getLong(i);
                            type = 2;
                        } catch (Exception e) {
                            try {
                                cursor.getDouble(i);
                                type = 3;
                            } catch (Exception e2) {
                            }
                        }
                    }
                    
                    switch (type) {
                        case 1:
                            success = window.putNull(position, i);
                            break;
                        case 2:
                            success = window.putLong(cursor.getLong(i), position, i);
                            break;
                        case 3:
                            success = window.putDouble(cursor.getDouble(i), position, i);
                            break;
                        default:
                            final String value = cursor.getString(i);
                            success = value != null ? window.putString(value, position, i)
                                    : window.putNull(position, i);
                            break;
                        }
                        
                    if (!success) {
                        window.freeLastRow();
                        break;
                    }
                }
                position += 1;
            } while (cursor.moveToNext());
        }
        cursor.moveToPosition(oldPos);
    }
}