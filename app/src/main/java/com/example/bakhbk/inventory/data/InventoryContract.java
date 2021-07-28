package com.example.bakhbk.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor
     */
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.bakhbk.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.bakhbk.inventory.data/bookstore/ is a valid path for
     * looking at bookstore data. content://com.example.bakhbk.inventory.data/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BOOKSTORE_TABLE = "bookstore";

    /**
     * Inner class that defines constant values for the bookstore database table.
     * Each entry in the table represents a single book.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the bookstore data in the provider
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKSTORE_TABLE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of bookstore.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKSTORE_TABLE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product(book).
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE
                        + "/"
                        + CONTENT_AUTHORITY
                        + "/"
                        + PATH_BOOKSTORE_TABLE;

        /**
         * Name of database table for bookstore
         */
        public final static String TABLE_NAME = "bookstore";

        /**
         * Unique ID number for the bookstore (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product(book).
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Price of the product(book).
         * Type: INTEGER
         */
        public final static String COLUMN_PRICE = "price";

        /**
         * Quantity of the product(book).
         * Type: INTEGER
         */
        public final static String COLUMN_QUANTITY = "quantity";

        /**
         * Supplier of the product(book).
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER = "supplier";

        /**
         * Supplier phone number of the product(book).
         * Type: TEXT
         */
        public final static String COLUMN_PHONE_NUMBER = "phone";
    }
}















