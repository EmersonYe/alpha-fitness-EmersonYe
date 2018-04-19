package edu.sjsu.emerson.alphafitness;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;


public class LocationContentProvider extends ContentProvider
{
    private final static String TAG = LocationContentProvider.class.getSimpleName();

    static final String PROVIDER = "edu.sjsu.emerson.alphafitness.provider";
    static final String DATABASE_NAME = "provider";
    static final String URL = "content://" + PROVIDER + "/workouts";
    static final Uri URI = Uri.parse(URL);

    static final String WORKOUTS_TABLE_NAME = "workouts";
    static final String _ID = "_id";
    static final String START_DATE = "start_date";
    static final String DISTANCE = "distance";
    static final String DURATION = "duration";
    static final String STEP_COUNT = "step_count";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + WORKOUTS_TABLE_NAME +
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + START_DATE + " INTEGER NOT NULL, " +
                    " " + DISTANCE + " INTEGER NOT NULL, " +
                    " " + DURATION + " INTEGER NOT NULL, " +
                    " " + STEP_COUNT + " INTEGER NOT NULL);";

    static final int WORKOUTS = 1;
    static final int WORKOUT_ID = 2;
    static final UriMatcher uriMatcher;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER, "workouts", WORKOUT_ID);
        uriMatcher.addURI(PROVIDER, "workouts/#", WORKOUT_ID);
    }


    private static HashMap<String, String> WORKOUTS_PROJECTION_MAP;
    private SQLiteDatabase db;
    Context mContext;

    public LocationContentProvider()
    {
    }

    /**
     * Initalizes data base operations.
     * - Create/opens a database for reading and writing by calling
     * DB.getWritableDatabase()
     * - Once opened successfully, db is cached so it can be used any time when
     * you need to write to the database
     *
     * @return status of initialization
     */
    @Override
    public boolean onCreate()
    {
        Log.v(TAG, "content provider: onCreate()");

        mContext = getContext();
        if (mContext == null) {
            Log.e(TAG, "Failed to retrieve the context.");
            return false;
        }
        DB dbHelper = new DB(mContext);
        db = dbHelper.getWritableDatabase();
        if (db == null) {
            Log.e(TAG, "Failed to create a writable database");
            return false;
        }
        return true;
    }

    /**
     * - Configure options for SQLiteQueryBuilder to retrieve a Cursor object
     * - Cursor is an interface that provides random read/write access to the
     * result set retrieved by a database query
     * - The query is an asynchronous process so you can use setNotificationUrl
     * to specify a content URI to watch for changes
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return Cursor object retrieved from SQLiteQueryBuilder
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        Log.v(TAG, "content provider: query()");

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(WORKOUTS_TABLE_NAME);

        if (getMatchedID(uri) == WORKOUTS) {
            sqLiteQueryBuilder.setProjectionMap(WORKOUTS_PROJECTION_MAP);
        } else {
            sqLiteQueryBuilder.appendWhere( getIdString(uri));
        }

        if (sortOrder == null || sortOrder == ""){
            sortOrder = _ID;
        }
        Cursor c = sqLiteQueryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;
    }

    /**
     * Call SQLiteDatabase's update() and notify all content observers
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return seems to always return 0
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        Log.v(TAG, "content provider: update()");

        int count = 0;
        int matchedID = getMatchedID(uri);

        String sel_str = (matchedID == WORKOUT_ID) ?
                getSelectionWithID(uri, selection) : selection;

        count = db.update(
                WORKOUTS_TABLE_NAME,
                values,
                sel_str,
                selectionArgs);

        notifyChange(uri);
        return count;
    }

    /**
     * Call SQLiteDatabase's insert() and notify all content observers
     *
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Log.v(TAG, "content provider: insert()");

        long row = db.insert(WORKOUTS_TABLE_NAME, "", values);

        if (row > 0) {
            Uri _uri = ContentUris.withAppendedId(URI, row);
            notifyChange(_uri);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    /**
     *
     * @param uri the uri to query (NOTE: must never be null)
     * @return a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(Uri uri)
    {
        Log.v(TAG, "content provider: getType()");

        // NOTE: these may be wrong!
        // notes say to return "vnd.android.cursor.dir/vnd.wearable.workouts";
        if (getMatchedID(uri) == WORKOUTS)
            return "vnd.android.cursor.dir/vnd.sjsu.emerson.workouts";
        else
            return "vnd.android.cursor.item/vnd.sjsu.emerson.workouts";

    }

    /**
     * Call SQLiteDatabase's delete() method and notify the change
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        Log.v(TAG, "content provider: delete()");

        int count = 0;
         String sel_str = (getMatchedID(uri) == WORKOUTS) ?
                 getSelectionWithID(uri, selection) : selection;

         count = db.delete(
                 WORKOUTS_TABLE_NAME,
                 sel_str,
                 selectionArgs);
         notifyChange(uri);
         return count;
    }

    private void notifyChange(Uri uri)
    {
        ContentResolver resolver = mContext.getContentResolver();
        if (resolver != null) resolver.notifyChange(uri, null);
    }

    /**
     * Checks if the uri matches with either workoutS or workout_ID and thows
     * an exception if the current URI is not supported
     *
     * @param uri
     * @return
     */
    private int getMatchedID(Uri uri)
    {
        int matchedID = uriMatcher.match(uri);
        if (!(matchedID == WORKOUTS || matchedID == WORKOUT_ID))
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        return matchedID;
    }

    /**
     * Helper method that helps generate the strings for ContentProvider
     * library calls
     *
     * @param uri
     * @return uri path segment prepended with '_ID'
     */
    private String getIdString(Uri uri)
    {
        return (_ID + " = " + uri.getPathSegments().get(1));
    }

    /**
     * TODO: document what @return is
     * Helper method that helps generate the strings for ContentProvider
     *
     * @param uri
     * @param selection
     * @return
     */
    private String getSelectionWithID(Uri uri, String selection)
    {
        String sel_str = getIdString(uri);
        if (!TextUtils.isEmpty(selection))
            sel_str += " AND (" + selection + ")";
        return sel_str;
    }

    /**
     * Database helper
     * Adds SQL commands for creating a new database table
     */
    private static class DB extends SQLiteOpenHelper
    {
        DB(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WORKOUTS_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
