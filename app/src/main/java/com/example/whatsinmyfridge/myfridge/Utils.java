package com.example.whatsinmyfridge.myfridge;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.whatsinmyfridge.myfridge.data.FoodDbHelper;
import com.example.whatsinmyfridge.myfridge.data.UnitContract;




public final class Utils {
    private Utils() {}


    public static double convert(double amount, int unit, boolean forStorage, Context context) {
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_CONVERT};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(unit) };
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        double conversionFactor = c.getDouble(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_CONVERT));

        c.close();
        db.close();

        if (forStorage) {
            return amount * conversionFactor;
        } else {
            return amount / conversionFactor;
        }
    }


    public static String getUnitString(int unit, Context context) {
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{UnitContract.UnitEntry.COLUMN_NAME};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(unit)};
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        String unitString = c.getString(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_NAME));

        c.close();
        db.close();

        return unitString;
    }



    public static int getUnitType(int unit, Context context) {
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_TYPE};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(unit)};
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        int unitType = c.getInt(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_TYPE));

        c.close();
        db.close();

        return unitType;
    }
}
