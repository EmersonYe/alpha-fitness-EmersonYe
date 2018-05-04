package edu.sjsu.emerson.alphafitness.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import static edu.sjsu.emerson.alphafitness.database.workoutDbHelper.DATE;
import static edu.sjsu.emerson.alphafitness.database.workoutDbHelper.WORKOUTS_TABLE_NAME;

public class MyContentProvider extends ContentProvider
{
    static final String PROVIDER = "edu.sjsu.myprovider";
    static final String URL = "content://" + PROVIDER + "/" + WORKOUTS_TABLE_NAME;
    public static final Uri URI = Uri.parse(URL);

    private static final String TAG = "MyContentProvider";
    public static final String _ID = "_id";

    static final int WORKOUTS = 1;
    static final int WORKOUT_ID = 2;

    private SQLiteDatabase db;
    static final UriMatcher uriMatcher;

    Context mContext;
    private static HashMap<String, String> WORKOUTS_PROJECTION_MAP;

    static {
        uriMatcher = new UriMatcher((UriMatcher.NO_MATCH));
        uriMatcher.addURI(PROVIDER, WORKOUTS_TABLE_NAME, WORKOUTS);
        uriMatcher.addURI(PROVIDER, WORKOUTS_TABLE_NAME + "/#", WORKOUT_ID);
    }

    public MyContentProvider()
    {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        Log.d(TAG, "content provider: delete()");

        int count = 0;

        String sel_str = (getMatchedId(uri) == WORKOUT_ID) ? getSelectionWithId(uri, selection) : selection;

        count = db.delete(
                WORKOUTS_TABLE_NAME,
                sel_str,
                selectionArgs);
        // notifyChange(uri);
        return count;

    }

    @Override
    public String getType(Uri uri)
    {
        Log.d(TAG, "content provider: getType()");

        if (getMatchedId(uri) == WORKOUTS)
            return "vnd.android.cursor/vnd.edu.sjsu.provider." + WORKOUTS_TABLE_NAME;
        else
            return "vnd.android.item/vnd.edu.sjsu.provider." + WORKOUTS_TABLE_NAME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Log.d(TAG, "content provider: insert()");

        long row = db.insert(WORKOUTS_TABLE_NAME, "", values);

        if (row > 0) {
            Uri _uri = ContentUris.withAppendedId(URI, row);
            // notifyChange(_uri);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public boolean onCreate()
    {
        Log.d(TAG, "content provider: onCreate()");

        mContext = getContext();
        if (mContext == null) {
            Log.e(TAG, "Failed to retrieve the context.");
            return false;
        }
        workoutDbHelper dbHelper = new workoutDbHelper(mContext);
        db = dbHelper.getWritableDatabase();
        if (db == null) {
            Log.e(TAG, "Failed to create a writable database");
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        Log.v(TAG, "content provider: query()");

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(WORKOUTS_TABLE_NAME);

        if (getMatchedId(uri) == WORKOUTS) {
            sqLiteQueryBuilder.setProjectionMap(WORKOUTS_PROJECTION_MAP);
        } else {
            sqLiteQueryBuilder.appendWhere(getIdString(uri));
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = DATE;
        }
        Cursor c = sqLiteQueryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        Log.v(TAG, "content provider: update()");

        int count = 0;
        int matchedId = getMatchedId(uri);

        String sel_str = (matchedId == WORKOUT_ID) ?
                getSelectionWithId(uri, selection) : selection;

        count = db.update(
                WORKOUTS_TABLE_NAME,
                values,
                sel_str,
                selectionArgs);
        // notifyChange(uri);
        return count;
    }

    private int getMatchedId(Uri uri)
    {
        int matchedId = uriMatcher.match(uri);
        if (!(matchedId == WORKOUTS || matchedId == WORKOUT_ID))
            throw new IllegalArgumentException("Unsopported URI: " + uri);
        return matchedId;
    }

    private String getIdString(Uri uri)
    {
        return (_ID + " = " + uri.getPathSegments().get(1));
    }

    private String getSelectionWithId(Uri uri, String selection)
    {
        String sel_str = getIdString(uri);
        if (!TextUtils.isEmpty(selection))
            sel_str += " AND (" + selection + ")";
        return sel_str;
    }
}
