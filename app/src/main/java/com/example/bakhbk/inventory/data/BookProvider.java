package com.example.bakhbk.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.util.Log;
import com.example.bakhbk.inventory.data.InventoryContract.InventoryEntry;

public class BookProvider extends ContentProvider {

  /**
   * Tag for the log messages
   */
  public static final String TAG = BookProvider.class.getSimpleName();

  /**
   * URI matcher code for the content URI for the bookstore table
   */
  private static final int BOOKS = 100;

  /**
   * URI matcher code for the content URI for a single book in the bookstore table
   */
  private static final int BOOK_ID = 101;

  /**
   * UriMatcher object to match a content URI to a corresponding code
   * The input passed into the constructor represents the code to return for the root URI.
   * It's common to use NO_MATCH ad the input for this case.
   */
  private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  /** Static initializer. This is run the first time anything is called from this class.*/
  static {
    /** The calls to addURI() go here, for all of the content URI patterns that the provider
     * should recognize. All paths added to the UriMatcher have a corresponding code to return
     * when a match s found.
     *
     * The content URI of the form "content://com.example.bakhbk.inventory.data/bookstore"
     * will map to the integer code {@link #BOOKS}. This URI is used to provide access
     * to MULTIPLE rows of the bookstore table*/
    sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKSTORE_TABLE,
        BOOKS);

    /** The content URI of the form "content://com.example.bakhbk.inventory.data/bookstore/#" will map to the
     * integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row
     * of the bookstore table.
     *
     * In this case, the "#" wildcard is used where "#" can be substituted for an integer.
     * For example, "content://com.example.bakhbk.inventory.data/bookstore/3" matches, but
     * "content://com.example.bakhbk.inventory.data/bookstore" (without a number at the end) doesn't match.*/
    sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
        InventoryContract.PATH_BOOKSTORE_TABLE + "/#", BOOK_ID);
  }

  /**
   * Database helper object
   */
  private InventoryDbHelper mDbHelper;

  @Override
  public boolean onCreate() {
    mDbHelper = new InventoryDbHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    /** Get readable database*/
    SQLiteDatabase database = mDbHelper.getReadableDatabase();

    /**This cursor will hold the result of the query*/
    Cursor cursor;

    /**Figure out if the URI matcher can match the URI to a specific code*/
    int match = sUriMatcher.match(uri);
    switch (match) {
      case BOOKS:
        /** For the BOOKS code, query the bookstore table directly with the given
         * projection, selection, selection arguments, and sort order. The cursor
         * could contain multiple rows of the bookstore table.*/
        cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
            null, null, sortOrder);
        break;
      case BOOK_ID:
        /** For the BOOK_ID code, extract out the ID from the URI.
         * For an example URI such as "content://com.example.bakhbk.inventory.data/bookstore/3",
         * the selection will be "_id=?" and the selection argument will be a
         * String array containing the actual ID of 3 in this case.
         *
         * For every "?" in the selection, we need to have an element in the selection
         * arguments that will fill in the "?". Since we have 1 question mark in the
         * selection, we have 1 String in the selection arguments' String array.*/
        selection = InventoryEntry._ID + "=?";
        selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

        /** This will perform a query on the bookstore table where the _id equals 3 to return a
         *  Cursor containing that row of the table.*/
        cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
            null, null, sortOrder);
        break;
      default:
        throw new IllegalArgumentException("Cannot query unknown URI " + uri);
    }

    /**Set notification URI on the Cursor,
     *  so we know what content URI the Cursor was created for.
     *  If the data at this URI changes, then we know we need to updateBook the Cursor.*/
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    /** Return the cursor*/
    return cursor;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case BOOKS:
        return insertBook(uri, values);
      default:
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }
  }

  /**
   * Insert a pet into the database with the given content values. Return the new content URI
   * for that specific row in the database.
   */
  private Uri insertBook(Uri uri, ContentValues values) {
    /** Check that the name is not null*/
    String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
    if (name == null) {
      throw new IllegalArgumentException("Book requires a name");
    }

    /** Check that the price is not null*/
    Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
    if (price != null && price < 0) {
      throw new IllegalArgumentException("Book requires valid price");
    }

    /** Check that the quantity is not null*/
    Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
    if (quantity != null && quantity < 0) {
      throw new IllegalArgumentException("Book requires valid number");
    }

    /** Check that the supplier is not null*/
    String supplier = values.getAsString(InventoryEntry.COLUMN_SUPPLIER);
    if (supplier == null) {
      throw new IllegalArgumentException("Book requires a supplier name");
    }

    /** Check that the phone is not null*/
    String phone = values.getAsString(InventoryEntry.COLUMN_PHONE_NUMBER);
    if (phone == null) {
      throw new IllegalArgumentException("Book requires a valid supplier phone number");
    }

    /**Get writable database*/
    SQLiteDatabase database = mDbHelper.getWritableDatabase();

    /**Insert the new book with the given values*/
    long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
    // If the ID is -1, then the insertion failed. Log an error and return null.
    if (id == -1) {
      Log.e(TAG, "Failed to insert row for " + uri);
      return null;
    }

    /**Notify all listeners that the data has changed for the bookstore content URI*/
    getContext().getContentResolver().notifyChange(uri, null);

    /**Return the new URI with the ID (of the newly inserted row) appended at the end*/
    return ContentUris.withAppendedId(uri, id);
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String selection,
      String[] selectionArgs) {
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case BOOKS:
        return updateBook(uri, contentValues, selection, selectionArgs);
      case BOOK_ID:
        // For the PET_ID code, extract out the ID from the URI,
        // so we know which row to update. Selection will be "_id=?" and selection
        // arguments will be a String array containing the actual ID.
        selection = InventoryEntry._ID + "=?";
        selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
        return updateBook(uri, contentValues, selection, selectionArgs);
      default:
        throw new IllegalArgumentException("Update is not supported for " + uri);
    }
  }

  public int updateBook(Uri uri, ContentValues values,
      String selection, String[] selectionArgs) {
    /** If the {@link InventoryEntry#COLUMN_PRODUCT_NAME} key is present,
     * check that the name value is not null.*/
    if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
      String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
      if (name == null) {
        throw new IllegalArgumentException("Book requires a name");
      }
    }

    /** If the {@link InventoryEntry#COLUMN_PRICE} key is present,
     * check that the price value is not null.*/
    if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
      Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
      if (price != null && price < 0) {
        throw new IllegalArgumentException("Book requires valid price");
      }
    }

    /** If the {@link InventoryEntry#COLUMN_QUANTITY} key is present,
     * check that the quantity value is not null.*/
    if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
      Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
      if (quantity != null && quantity < 0) {
        throw new IllegalArgumentException("Book requires valid quantity");
      }
    }

    /** If the {@link InventoryEntry#COLUMN_SUPPLIER} key is present,
     * check that the supplier value is not null.*/
    if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER)) {
      String supplier = values.getAsString(InventoryEntry.COLUMN_SUPPLIER);
      if (supplier == null) {
        throw new IllegalArgumentException("Book requires a supplier name");
      }
    }

    /** If the {@link InventoryEntry#COLUMN_PHONE_NUMBER} key is present,
     * check that the phone number value is not null.*/
    if (values.containsKey(InventoryEntry.COLUMN_PHONE_NUMBER)) {
      String phone = values.getAsString(InventoryEntry.COLUMN_PHONE_NUMBER);
      if (phone == null) {
        throw new IllegalArgumentException("Book requires valid quantity");
      }
    }

    /** If there are no values to updateBook, then don't try to updateBook the database*/
    if (values.size() == 0) {
      return 0;
    }

    /** Otherwise, get writable database to updateBook the data*/
    SQLiteDatabase database = mDbHelper.getWritableDatabase();

    /** Perform the updateBook on the database and get the number of rows affected*/
    int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

    /** If 1 or more rows were updated, then notify all listeners that the data at the
     * given URI has changed*/
    if (rowsUpdated != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    /** Return the number of rows updated*/
    return rowsUpdated;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    /**Get writable database*/
    SQLiteDatabase database = mDbHelper.getWritableDatabase();

    /** Track the number of rows that were deleted*/
    int rowsDeleted;

    final int match = sUriMatcher.match(uri);
    switch (match) {
      case BOOKS:
        /** Delete all rows that match the selection and selection args*/
        rowsDeleted = database.delete(InventoryEntry.TABLE_NAME,
            selection, selectionArgs);
        break;
      case BOOK_ID:
        // Delete a single row given by the ID in the URI
        selection = InventoryEntry._ID + "=?";
        selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
        rowsDeleted = database.delete(InventoryEntry.TABLE_NAME,
            selection, selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Deletion is not supported for " + uri);
    }

    /**If 1 or more rows were deleted, then notify all listeners that the data at the
     *  given URI has changed*/
    if (rowsDeleted != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    /** Return the number of rows deleted*/
    return rowsDeleted;
  }

  @Override
  public String getType(Uri uri) {
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case BOOKS:
        return InventoryEntry.CONTENT_LIST_TYPE;
      case BOOK_ID:
        return InventoryEntry.CONTENT_ITEM_TYPE;
      default:
        throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
    }
  }
}