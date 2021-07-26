package com.example.whatsinmyfridge.myfridge.data;

import android.provider.BaseColumns;



public final class UnitContract {
    private UnitContract() {}

    public static class UnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "units";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONVERT = "conversion";
        public static final String COLUMN_TYPE = "type";

        public static final int UNIT_TYPE_ITEM = 0;
        public static final int UNIT_TYPE_VOLUME = 1;
        public static final int UNIT_TYPE_MASS = 2;

        public static final double UNIT_ITEM = 1;
        public static final double ML_IN_ML = 1;
        public static final double ML_IN_L = 1000.0;


        public static final double G_IN_G = 1;
        public static final double G_IN_MG = 0.001;
        public static final double G_IN_KG = 1000.0;

    }
}
