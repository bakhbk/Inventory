package com.example.bakhbk.inventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.bakhbk.inventory.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

  /** Identifier for the bookstore data loader */
  private static final int EXISTING_BOOK_LOADER = 0;

  /** Content URI for the existing pet (null if it's a new book(product)) */
  private Uri mCurrentBookUri;

  // EditText field to enter the product (books) name
  private EditText mProductNameEditText;

  // EditText field to enter the book price
  private EditText mPriceEditText;

  // EditText field to enter the book quantity
  private EditText mQuantityEditText;

  // EditText field to enter the book's supplier name
  private EditText mSupplierNameEditText;

  // EditText field to enter the book's supplier phone number
  private EditText mPhoneNumberEditText;

  private ImageButton mIncrementButton;

  private ImageButton mDecrementButton;

  private int mQuantity;

  private static final int PERMISSION_REQUEST_CALL_PHONE = 0;

  /**
   * Boolean flag that keeps track of whether the book has been edited (true) or
   * not (false)
   */
  private boolean mBookHasChanged = false;

  // Boolean status for required fields,TRUE if these fields have been populated
  boolean hasValues = false;

  /**
   * OnTouchListener that listens for any user touches on a View, implying that they are
   * modifying the view, and we change the mBookHasChanged boolean to true.
   */
  private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      mBookHasChanged = true;
      return false;
    }
  };

  @SuppressLint("ClickableViewAccessibility") @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);

    /**Examine the intent that was used to launch this activity,
     * in order to figure out if we're creating a new pet or editing an existing one.*/
    Intent intent = getIntent();
    mCurrentBookUri = intent.getData();

    /**If the intent DOES NOT contain a pet content URI, then we know that we are
     * creating a new book.*/
    if (mCurrentBookUri == null) {
      // This is a new pet, so change the app bar to say "Add a Book"
      setTitle(getString(R.string.editor_activity_title_new_book));

      /**Invalidate the options menu, so the "Delete" menu option can be hidden.
       * (It doesn't make sense to delete a pet that hasn't been created yet.)*/
      invalidateOptionsMenu();
    } else {
      /** Otherwise this is an existing pet, so change app bar to say "Edit Book"*/
      setTitle(getString(R.string.editor_activity_title_edit_book));

      /**Initialize a loader to read the pet data from the database
       * and display the current values in the editor*/
      getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    /** Find all relevant views that we will need to read user input from*/
    mProductNameEditText = findViewById(R.id.edit_product_name);
    mPriceEditText = findViewById(R.id.edit_price);
    mQuantityEditText = findViewById(R.id.edit_quantity);
    mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
    mPhoneNumberEditText = findViewById(R.id.edit_supplier_phone_number);
    mDecrementButton = findViewById(R.id.decrement);
    mIncrementButton = findViewById(R.id.increment);

    /**Setup OnTouchListeners on all the input fields, so we can determine if the user
     * has touched or modified them. This will let us know if there are unsaved changes
     * or not, if the user tries to leave the editor without saving.*/
    mProductNameEditText.setOnTouchListener(mTouchListener);
    mPriceEditText.setOnTouchListener(mTouchListener);
    mQuantityEditText.setOnTouchListener(mTouchListener);
    mSupplierNameEditText.setOnTouchListener(mTouchListener);
    mPhoneNumberEditText.setOnTouchListener(mTouchListener);
    mDecrementButton.setOnTouchListener(mTouchListener);
    mIncrementButton.setOnTouchListener(mTouchListener);

    mIncrementButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        incrementImageButton(v);
      }
    });

    mDecrementButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        decrementImageButton(v);
      }
    });
  }

  public void displayQuantity() {
    mQuantityEditText.setText(String.valueOf(mQuantity));
  }

  public void incrementImageButton(View view) {
    mQuantity++;
    displayQuantity();
  }

  public void decrementImageButton(View view) {
    if (mQuantity == 0) {
      Toast.makeText(this, "Can't decrease quantity", Toast.LENGTH_SHORT).show();
    } else {
      mQuantity--;
      displayQuantity();
    }
  }

  /** Get user input from editor and save new book into database. */
  private boolean saveBook() {
    /** Read from input fields
     * Use trim to eliminate leading or trailing white space*/
    String nameString = mProductNameEditText.getText().toString().trim();
    String priceString = mPriceEditText.getText().toString().trim();
    String quantityString = mQuantityEditText.getText().toString().trim();
    String supplierNameString = mSupplierNameEditText.getText().toString().trim();
    String phoneNumberString = mPhoneNumberEditText.getText().toString().trim();

    /**Check if this is supposed to be a new pet
     * and check if all the fields in the editor are blank*/
    if (mCurrentBookUri == null &&
        TextUtils.isEmpty(nameString) &&
        TextUtils.isEmpty(priceString) &&
        TextUtils.isEmpty(quantityString) &&
        TextUtils.isEmpty(supplierNameString) &&
        TextUtils.isEmpty(phoneNumberString)) {
      /**Since no fields were modified, we can return early without creating a new pet.
       * No need to create ContentValues and no need to do any ContentProvider operations.*/
      hasValues = true;
      return hasValues;
    }

    /** Create a Content object where column names are the keys,
     * and books attributes from the editor are the values.*/
    ContentValues values = new ContentValues();

    // Section for checking has a value or not
    if (TextUtils.isEmpty(nameString)) {
      Toast.makeText(this, getString(R.string.enter_the_name_of_the_product), Toast.LENGTH_SHORT)
          .show();
      return hasValues;
    } else {
      values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
    }

    if (TextUtils.isEmpty(priceString)) {
      Toast.makeText(this, getString(R.string.enter_the_price_of_the_product), Toast.LENGTH_SHORT)
          .show();
      return hasValues;
    } else {
      int price = Integer.parseInt(priceString);
      values.put(InventoryEntry.COLUMN_PRICE, price);
    }

    if (TextUtils.isEmpty(quantityString)) {
      Toast.makeText(this, getString(R.string.enter_the_quantity_of_the_product),
          Toast.LENGTH_SHORT).show();
      return hasValues;
    } else {
      int quantity = Integer.parseInt(quantityString);
      values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
    }

    // Section for checking has a value or not
    if (TextUtils.isEmpty(supplierNameString)) {
      Toast.makeText(this, getString(R.string.enter_the_supplier_name), Toast.LENGTH_SHORT).show();
      return hasValues;
    } else {
      values.put(InventoryEntry.COLUMN_SUPPLIER, supplierNameString);
    }

    // Section for checking has a value or not
    if (TextUtils.isEmpty(phoneNumberString)) {
      Toast.makeText(this, getString(R.string.enter_the_supplier_phone_number), Toast.LENGTH_SHORT)
          .show();
      return hasValues;
    } else {
      values.put(InventoryEntry.COLUMN_PHONE_NUMBER, phoneNumberString);
    }

    /** Determine if this is a new or existing book by checking if mCurrentBookUri is null or not*/
    if (mCurrentBookUri == null) {
      /** This is a NEW book, so insert a new book into the provider,
       * returning the content URI for the new book.*/
      Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

      /** Show a toast message depending on whether or not the insertion was successful.*/
      if (newUri == null) {
        /**If the new content URI is null, then there was an error with insertion.*/
        Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
            Toast.LENGTH_SHORT).show();
      } else {
        /**Otherwise, the insertion was successful and we can display a toast.*/
        Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
            Toast.LENGTH_SHORT).show();
      }
    } else {
      /** Otherwise this is an EXISTING book, so updateBook the book with content URI: mCurrentBookUri
       * and pass in the new ContentValues. Pass in null for the selection and selection args
       * because mCurrentBookUri will already identify the correct row in the database that
       * we want to modify.*/
      int rowsAffected = getContentResolver().update(mCurrentBookUri,
          values, null, null);

      /** Show a toast message depending on whether or not the updateBook was successful.*/
      if (rowsAffected == 0) {
        /** If no rows were affected, then there was an error with the updateBook.*/
        Toast.makeText(this, getString(R.string.editor_update_pet_failed),
            Toast.LENGTH_SHORT).show();
      } else {
        /** Otherwise, the updateBook was successful and we can display a toast.*/
        Toast.makeText(this, getString(R.string.editor_update_pet_successful),
            Toast.LENGTH_SHORT).show();
      }
    }

    hasValues = true;
    return hasValues;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    /** Inflate the menu options from the res/menu/menu_editor.xml file.
     * This adds menu items to the app bar.*/
    getMenuInflater().inflate(R.menu.menu_editor, menu);
    return true;
  }

  /**
   * This method is called after invalidateOptionsMenu(), so that the
   * menu can be updated (some menu items can be hidden or made visible).
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    /** If this is a new book, hide the "Delete" menu item.*/
    if (mCurrentBookUri == null) {
      MenuItem menuItem = menu.findItem(R.id.action_delete);
      menuItem.setVisible(false);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    /** User clicked on a menu option in the app bar overflow menu*/
    switch (item.getItemId()) {
      /** Respond to a click on the "Save" menu option*/
      case R.id.action_save:
        /** Save book to database*/
        saveBook();
        if (hasValues == true) {
          // Exit activity
          finish();
        }
        return true;
      /**Respond to a click on the "Delete" menu option*/
      case R.id.action_delete:
        // Pop up confirmation dialog for deletion
        showDeleteConfirmationDialog();
        return true;

      // Respond to a click on the "Special Order" menu option
      case R.id.special_order:
        specialOrder();
        return true;

      // Respond to a click on the "Call the store" menu option
      case R.id.call_the_store:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
          // Permission is already available, start callTheStore
          callTheStore();
        } else {
          // Permission is missing and must be requested.
          requestCallPermission();
        }
        return true;

      /**Respond to a click on the "Up" arrow button in the app bar*/
      case android.R.id.home:
        /**Navigate back to parent activity (EditorActivity - this)*/
        if (!mBookHasChanged) {
          NavUtils.navigateUpFromSameTask(EditorActivity.this);
          return true;
        }

        /** Otherwise if there are unsaved changes, setup a dialog to warn the user.
         * Create a click listener to handle the user confirming that
         * changes should be discarded.*/
        DialogInterface.OnClickListener discardButtonClickListener =
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                /**User clicked "Discard" button, navigate to parent activity.*/
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
              }
            };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /** Intent mailTo */
  public void specialOrder() {
    Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
    intent.setType(getString(R.string.special_order_for_text_plain));
    intent.setData(Uri.parse(getString(R.string.special_order_for_mail_to) + getString(
        R.string.special_order_for_email_example)));
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
        getString(R.string.special_order_for_new_order) + " " +
            mProductNameEditText.getText().toString().trim());
    String message = getString(R.string.special_order_for_mail_text) + " " +
        mProductNameEditText.getText().toString().trim() + " " +
        mQuantityEditText.getText().toString().trim() + " pcs., \n" +
        getString(R.string.special_order_for_mail_text_1) + " " +
        mPriceEditText.getText().toString().trim() + " $" +
        "\n" + getString(R.string.special_order_for_mail_text_2) +
        "\n" +
        "\n" + getString(R.string.special_order_for_best_regards);
    intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
    startActivity(intent);
  }

  /**
   * This method is called when the back button is pressed.
   */
  @Override
  public void onBackPressed() {
    /** If the pet hasn't changed, continue with handling back button press*/
    if (!mBookHasChanged) {
      super.onBackPressed();
      return;
    }

    /** Otherwise if there are unsaved changes, setup a dialog to warn the user.
     * Create a click listener to handle the user confirming that changes should be discarded.*/
    DialogInterface.OnClickListener discardButtonClickListener =
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            /** User clicked "Discard" button, close the current activity.*/
            finish();
          }
        };

    /**Show dialog that there are unsaved changes*/
    showUnsavedChangesDialog(discardButtonClickListener);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    /** Since the editor shows all pet attributes, define a projection that contains
     * all columns from the pet table*/
    String[] projection = {
        InventoryEntry._ID,
        InventoryEntry.COLUMN_PRODUCT_NAME,
        InventoryEntry.COLUMN_PRICE,
        InventoryEntry.COLUMN_QUANTITY,
        InventoryEntry.COLUMN_SUPPLIER,
        InventoryEntry.COLUMN_PHONE_NUMBER
    };

    /** This loader will execute the ContentProvider's query method on a background thread*/
    return new CursorLoader(this,   /** Parent activity context*/
        mCurrentBookUri,                    /** Query the content URI for the current book*/
        projection,                         /** Columns to include in the resulting Cursor*/
        null,                      /** No selection clause*/
        null,                   /**No selection arguments*/
        null);                    /**Default sort order*/
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    /** Bail early if the cursor is null or there is less than 1 row in the cursor*/
    if (cursor == null || cursor.getCount() < 1) {
      return;
    }

    /** Proceed with moving to the first row of the cursor and reading data from it
     * (This should be the only row in the cursor)*/
    if (cursor.moveToFirst()) {
      /** Find the columns of book attributes that we're interested in*/
      int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
      int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
      int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
      int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER);
      int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_NUMBER);

      /** Extract out the value from the Cursor for the given column index*/
      String nameLF = cursor.getString(nameColumnIndex);
      int priceLF = cursor.getInt(priceColumnIndex);
      int quantityLF = cursor.getInt(quantityColumnIndex);
      String supplierLF = cursor.getString(supplierColumnIndex);
      String phoneSupplierLF = cursor.getString(phoneColumnIndex);
      mQuantity = quantityLF;

      /** Update the views on the screen with the values from the database*/
      mProductNameEditText.setText(nameLF);
      mPriceEditText.setText(Integer.toString(priceLF));
      mQuantityEditText.setText(Integer.toString(quantityLF));
      mSupplierNameEditText.setText(supplierLF);
      mPhoneNumberEditText.setText(phoneSupplierLF);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    /** If the loader is invalidated, clear out all the data from the input fields.*/
    mProductNameEditText.setText("");
    mPriceEditText.setText("");
    mQuantityEditText.setText("");
    mSupplierNameEditText.setText("");
    mPhoneNumberEditText.setText("");
  }

  /**
   * Show a dialog that warns the user there are unsaved changes that will be lost
   * if they continue leaving the editor.
   *
   * @param discardButtonClickListener is the click listener for what to do when
   * the user confirms they want to discard their changes
   */
  private void showUnsavedChangesDialog(
      DialogInterface.OnClickListener discardButtonClickListener) {
    /**Create an AlertDialog.Builder and set the message, and click listeners
     * for the positive and negative buttons on the dialog.*/
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.unsaved_changes_dialog_msg);
    builder.setPositiveButton(R.string.discard, discardButtonClickListener);
    builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        /** User clicked the "Keep editing" button, so dismiss the dialog
         * and continue editing the book.*/
        if (dialog != null) {
          dialog.dismiss();
        }
      }
    });

    /**Create and show the AlertDialog*/
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  /** Prompt the user to confirm that they want to delete this book. */
  private void showDeleteConfirmationDialog() {
    /** Create an AlertDialog.Builder and set the message, and click listeners
     * for the positive and negative buttons on the dialog.*/
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.delete_dialog_msg);
    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        /** User clicked the "Delete" button, so delete the pet.*/
        deletePet();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        /** User clicked the "Cancel" button, so dismiss the dialog
         * and continue editing the book.*/
        if (dialog != null) {
          dialog.dismiss();
        }
      }
    });
    /** Create and show the AlertDialog*/
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  /** Perform the deletion of the pet in the database. */
  private void deletePet() {
    /** Only perform the delete if this is an existing book.*/
    if (mCurrentBookUri != null) {
      /** Call the ContentResolver to delete the pet at the given content URI.
       * Pass in null for the selection and selection args because the mCurrentBookUri
       * content URI already identifies the pet that we want.*/
      int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

      /** Show a toast message depending on whether or not the delete was successful.*/
      if (rowsDeleted == 0) {
        /** If no rows were deleted, then there was an error with the delete.*/
        Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
            Toast.LENGTH_SHORT).show();
      } else {
        /** Otherwise, the delete was successful and we can display a toast.*/
        Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
            Toast.LENGTH_SHORT).show();
      }
    }

    /** Close the activity*/
    finish();
  }

  /** Request Call permission for method callTheStore */
  private void requestCallPermission() {
    // Permission has not been granted and must be requested.
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.CALL_PHONE)) {
      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      Toast.makeText(this, R.string.toast_calls_permission, Toast.LENGTH_LONG).show();
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.CALL_PHONE }, PERMISSION_REQUEST_CALL_PHONE);
    } else {
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.CALL_PHONE }, PERMISSION_REQUEST_CALL_PHONE);
    }
  }

  /** Intent call the store */
  public void callTheStore() {
    Intent call = new Intent();
    call.setAction(Intent.ACTION_CALL);
    call.setData(Uri.parse("tel:" + mPhoneNumberEditText.getText().toString().trim()));
    startActivity(call);
  }
}