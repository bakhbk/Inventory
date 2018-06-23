package com.example.bakhbk.inventory.data;

import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract(){}

    public static final class InventoryEntry implements BaseColumns {

        public final static String TABLE_NAME = "bookstore";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER = "supplier";
        public final static String COLUMN_PHONE_NUMBER = "phone";
    }
}