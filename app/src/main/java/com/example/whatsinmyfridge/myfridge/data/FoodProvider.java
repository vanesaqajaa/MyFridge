package com.example.whatsinmyfridge.myfridge.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.whatsinmyfridge.myfridge.Utils;



public class FoodProvider extends ContentProvider {
    private FoodDbHelper mDbHelper;

    private static final String ID_SELECTION = FoodContract.FoodEntry._ID + "=?";

    private static final int FOOD = 100;
    private static final int FOOD_ID = 101;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(FoodContract.CONTENT_AUTHORITY, FoodContract.PATH_FOOD, FOOD);
        sUriMatcher.addURI(FoodContract.CONTENT_AUTHORITY, FoodContract.PATH_FOOD + "/#", FOOD_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FoodDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                break;
            case FOOD_ID:
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        Cursor cursor = db.query(FoodContract.FoodEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        validateData(values, true);

        values = convertForStorage(values);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                long id = db.insert(FoodContract.FoodEntry.TABLE_NAME, null, values);

                if (id != -1) getContext().getContentResolver().notifyChange(uri, null);

                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                // delete all the entries

                break;
            case FOOD_ID:
                // delete a single entry
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int rowsAffected = db.delete(FoodContract.FoodEntry.TABLE_NAME, selection, selectionArgs);

        if (rowsAffected > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        validateData(values, false);

        values = convertForStorage(values);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD_ID:
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int rowsAffected = db.update(FoodContract.FoodEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsAffected > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    /**
     * Converts amount and price to "absolute" values before storing.
     */
    private ContentValues convertForStorage(ContentValues values) {
        if (values.containsKey(FoodContract.FoodEntry.COLUMN_UNIT) &&
                values.containsKey(FoodContract.FoodEntry.COLUMN_AMOUNT)) {
            double amount = values.getAsDouble(FoodContract.FoodEntry.COLUMN_AMOUNT);
            int unit = values.getAsInteger(FoodContract.FoodEntry.COLUMN_UNIT);

            if (values.containsKey(FoodContract.FoodEntry.COLUMN_PRICE_PER)) {
                double price = values.getAsDouble(FoodContract.FoodEntry.COLUMN_PRICE_PER);
                price /= amount;
                values.put(FoodContract.FoodEntry.COLUMN_PRICE_PER, Utils.convert(price, unit, false, getContext()));
            }

            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, Utils.convert(amount, unit, true, getContext()));
        }
        return values;
    }

    /**
     * Validates the data before inserting into the db.
     */
    private void validateData(ContentValues values, boolean inserting) {

        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_NAME)) {
            String nameString = values.getAsString(FoodContract.FoodEntry.COLUMN_NAME);
            if (nameString == null || TextUtils.isEmpty(nameString))
                throw new IllegalArgumentException("Name must have a value.");
        }

        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_AMOUNT)) {
            Float amount = values.getAsFloat(FoodContract.FoodEntry.COLUMN_AMOUNT);
            if (amount == null || amount < 0)
                throw new IllegalArgumentException("Amount must be non-negative.");
        }

        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_UNIT)) {
            Integer unit = values.getAsInteger(FoodContract.FoodEntry.COLUMN_UNIT);
            if (unit == null)
                throw new IllegalArgumentException("Unit must have a value.");
        }
    }
}
