package edu.sjsu.emerson.alphafitness.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by emersonsjsu on 5/3/18.
 */

class workoutDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "myprovider";
    public static final String WORKOUTS_TABLE_NAME = "workouts";
    public static final String DATE = "date";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String STEPS = "steps";

    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_DB_TABLE =
            " CREATE TABLE " + WORKOUTS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     DATE  + " TEXT NOT NULL, " +
                     DISTANCE + " REAL NOT NULL, " +
                     DURATION + " INTEGER NOT NULL, " +
                     STEPS + " INTEGER NOT NULL);";

    public workoutDbHelper(Context context)
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
