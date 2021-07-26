package com.example.whatsinmyfridge.myfridge.data;

import android.net.Uri;
import android.provider.BaseColumns;


public final class FoodContract {
    private FoodContract() {}

    public static final String CONTENT_AUTHORITY = "myfridge";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FOOD = "food";

    public static class FoodEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FOOD);

        public static final String FOOD_URI_KEY = "data_food_item";

        public static final String TABLE_NAME = "food";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_PRICE_PER = "priceper";
        public static final String COLUMN_STORE = "store";
        public static final String COLUMN_EXPIRATION = "expiration";
        public static final String COLUMN_PHOTO = "photo";
    }
}
