package com.cse5236.bowlbuddy.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.cse5236.bowlbuddy.models.Building;

import java.util.ArrayList;
import java.util.List;

public class BuildingDBSingleton {
    private static BuildingDBSingleton instance = null;
    private BuildingDbHelper helper;
    private SQLiteDatabase db;

    private BuildingDBSingleton(Context context) {
        helper = new BuildingDbHelper(context);
        db = helper.getWritableDatabase();
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = new BuildingDBSingleton(context);
        }

        return instance.db;
    }

    public static boolean addBuilding(Context context, Building building) {
        ContentValues values = new ContentValues();

        values.put(BuildingDbHelper.Columns._ID, building.getId());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_FLOORS, building.getFloors());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_ADDRESS, building.getAddress());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_NAME, building.getName());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_OPEN_TIME, building.getOpenTime());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_CLOSE_TIME, building.getCloseTime());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_LONGITUDE, building.getLongitude());
        values.put(BuildingDbHelper.Columns.COLUMN_NAME_LATITUDE, building.getLatitude());

        long rowID = getDatabase(context).insertOrThrow(BuildingDbHelper.Columns.TABLE_NAME, null, values);

        return rowID != -1;
    }

    public static List<Building> getAllBuildings(Context context) {
        List<Building> buildingList = new ArrayList<>();
        Cursor cursor = getDatabase(context).rawQuery("SELECT * FROM " + BuildingDbHelper.Columns.TABLE_NAME, null);

        while(cursor.moveToNext()) {
            buildingList.add(new Building(
                    cursor.getInt(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns._ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_FLOORS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_OPEN_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_CLOSE_TIME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_LONGITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BuildingDbHelper.Columns.COLUMN_NAME_LATITUDE))
            ));
        }
        cursor.close();

        return buildingList;
    }
}
