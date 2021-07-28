package com.example.bakhbk.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bakhbk.inventory.data.InventoryContract.InventoryEntry;

import java.util.Locale;

import static android.content.ContentValues.TAG;

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Construct a new {@link InventoryCursorAdapter}.
     *
     * @param context the context
     * @param c       the cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view, No data is set (or bound) to the views yet.
     *
     * @param context - app context
     * @param cursor  - the cursor from which to get the data. The cusor is already
     *                moved to the correct position
     * @param parent  - the parent to which the new view is attached to
     * @return the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (int the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context App context
     * @param cursor  The cursor from which to get the data. The cursor is already moved
     *                to the correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        ImageButton buyButton = view.findViewById(R.id.buy);

        /* Find the column of inventory attributes that we're interested in*/
        final int idColumnIndex = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        /* Read the inventory attributes from the Cursor for the current book*/
        String bookName = cursor.getString(nameColumnIndex);
        final int price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        /* Update the TextViews with the attributes for the current book*/
        nameTextView.setText(bookName);
        priceTextView.setText(String.format(Locale.getDefault(), "%d%s", price, context.getString(R.string.price_simbol)));
        quantityTextView.setText(String.valueOf(quantity));

        buyButton.setOnClickListener(v -> adjustProductQuantity(context, ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, idColumnIndex), quantity));
    }

    /**
     * This method reduced product stock by 1
     *
     * @param context  - Activity context
     * @param mUri     - Uri used to update the stock of a specific product in the ListView
     * @param quantity - current stock of that specific product
     */
    private void adjustProductQuantity(Context context, Uri mUri, int quantity) {

        // Subtract 1 from current value if quantity of product >= 1
        int newQuantityValue = (quantity >= 1) ? quantity - 1 : 0;

        if (quantity == 0) {
            Toast.makeText(context.getApplicationContext(),
                    R.string.out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        // Update table by using new value of quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(mUri,
                contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Show error message in Logs with info about pass update.
            Log.i(TAG, context.getString(R.string.buy_massage_confirm));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_product_in_stock,
                    Toast.LENGTH_SHORT).show();
            // Show error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.error_massage_stock_update));
        }
    }
}