package com.example.bakhbk.inventory;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.bakhbk.inventory.data.InventoryContract.InventoryEntry;


@SuppressWarnings("deprecation")
public class InventoryAppActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the bookstore data loader
     */
    private static final int INVENTORY_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(this, EditorActivity.class)));

        // Find the ListView which will be populated with the pet data
        ListView petListView = findViewById(R.id.list);

        //Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener((adapterView, view, position, id) -> {
            // Create new intent to go to {@link EditorActivity}
            Intent intent = new Intent(InventoryAppActivity.this, EditorActivity.class);

            // Form the content URI that represents the specific book that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // {@link InventoryEntry#CONTENT_URI}.
            // For example, the URI would be "content://com.example.bakhbk.inventory.data/bookstore/2"
            // if the pet with ID 2 was clicked on.
            Uri currentBookUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

            // Set the URI on the data field of the intent
            intent.setData(currentBookUri);

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent);
        });

        // Kick off the loader
        //noinspection deprecation
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertBook() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, getString(R.string.product_name_example_text));
        values.put(InventoryEntry.COLUMN_PRICE, getString(R.string.price_example));
        values.put(InventoryEntry.COLUMN_QUANTITY, getString(R.string.quantity_example));
        values.put(InventoryEntry.COLUMN_SUPPLIER, getString(R.string.supplier_name_example));
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER,
                getString(R.string.supplier_phone_number_example));

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the bookstore database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all bookstore in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryAppActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_inventory.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,         // Provider content URI to query
                projection,                         // Columns to include in the resulting Cursor
                null,                      // No selection clause
                null,                   // No selection arguments
                null);                    // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}