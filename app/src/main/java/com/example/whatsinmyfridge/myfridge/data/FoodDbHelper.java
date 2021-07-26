package com.example.whatsinmyfridge.myfridge.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import com.example.whatsinmyfridge.R;

import com.example.whatsinmyfridge.R;
import com.example.whatsinmyfridge.myfridge.data.FoodContract.FoodEntry;

/**
 * Helper to create and manage versions of database.

 */

public class FoodDbHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "kitchen.db";

    private static final String SQL_CREATE_FOOD_TABLE =
            "CREATE TABLE " + FoodEntry.TABLE_NAME + " (" +
            FoodEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FoodEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            FoodEntry.COLUMN_UNIT + " INTEGER NOT NULL, " +
            FoodEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
            FoodEntry.COLUMN_PRICE_PER + " REAL, " +
            FoodEntry.COLUMN_EXPIRATION + " INTEGER, " +
            FoodEntry.COLUMN_STORE + " TEXT, " +
            FoodEntry.COLUMN_PHOTO + " TEXT);";

    private static final String SQL_CREATE_UNITS_TABLE =
            "CREATE TABLE " + UnitContract.UnitEntry.TABLE_NAME + " (" +
            UnitContract.UnitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UnitContract.UnitEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            UnitContract.UnitEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
            UnitContract.UnitEntry.COLUMN_CONVERT + " REAL NOT NULL);";

    public FoodDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FOOD_TABLE);
        db.execSQL(SQL_CREATE_UNITS_TABLE);

        ArrayList<ContentValues> valueList = new ArrayList<>();
        ContentValues cv = new ContentValues();

        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.item));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.UNIT_ITEM);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_ITEM);
        valueList.add(new ContentValues(cv));

        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.milliliter));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.ML_IN_ML);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_VOLUME);
        valueList.add(new ContentValues(cv));

        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.liter));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.ML_IN_L);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_VOLUME);
        valueList.add(new ContentValues(cv));


        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.gram));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.G_IN_G);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_MASS);
        valueList.add(new ContentValues(cv));

        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.milligram));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.G_IN_MG);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_MASS);
        valueList.add(new ContentValues(cv));

        cv.put(UnitContract.UnitEntry.COLUMN_NAME, mContext.getString(R.string.kilogram));
        cv.put(UnitContract.UnitEntry.COLUMN_CONVERT, UnitContract.UnitEntry.G_IN_KG);
        cv.put(UnitContract.UnitEntry.COLUMN_TYPE, UnitContract.UnitEntry.UNIT_TYPE_MASS);
        valueList.add(new ContentValues(cv));

        for (ContentValues values : valueList) {
            db.insert(UnitContract.UnitEntry.TABLE_NAME, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
