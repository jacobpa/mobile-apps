package com.cse5236.bowlbuddy.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class BuildingDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "buildings.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Columns.TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY," +
                    Columns.COLUMN_NAME_FLOORS + " INTEGER," +
                    Columns.COLUMN_NAME_ADDRESS + " TEXT," +
                    Columns.COLUMN_NAME_NAME + " TEXT," +
                    Columns.COLUMN_NAME_OPEN_TIME + " INTEGER," +
                    Columns.COLUMN_NAME_CLOSE_TIME + " INTEGER," +
                    Columns.COLUMN_NAME_LONGITUDE + " REAL," +
                    Columns.COLUMN_NAME_LATITUDE + " REAL)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Columns.TABLE_NAME;

    public BuildingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public class Columns implements BaseColumns {
        static final String TABLE_NAME = "building";
        static final String COLUMN_NAME_FLOORS = "floors";
        static final String COLUMN_NAME_ADDRESS = "address";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_OPEN_TIME = "open_time";
        static final String COLUMN_NAME_CLOSE_TIME = "close_time";
        static final String COLUMN_NAME_LONGITUDE = "longitude";
        static final String COLUMN_NAME_LATITUDE = "latitude";
    }
}
