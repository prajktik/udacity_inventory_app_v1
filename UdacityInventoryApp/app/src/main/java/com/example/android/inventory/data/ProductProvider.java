package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.android.inventory.LogUtil;
import com.example.android.inventory.data.ProductContract.ProductEntry;

public class ProductProvider extends ContentProvider{

    private static final boolean DEBUG = true;
    private static final String LOG_TAG = ProductProvider.class.getName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS,
                PRODUCTS);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS +
                "/#", PRODUCT_ID);
    }

    private ProductDbHelper dbHelper;

    public ProductProvider(){
    }

    @Override
    public boolean onCreate(){

        dbHelper = new ProductDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder){

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);

        switch(match){
            case PRODUCTS:{
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
            }
            break;
            case PRODUCT_ID:{
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs,
                        null, null, sortOrder);

            }
            break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues){
        final int match = uriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertProduct(Uri uri, ContentValues values){

        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if(name == null){
            throw new IllegalArgumentException("Product name required.");
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = database.insert(ProductEntry.TABLE_NAME, null, values);

        if(id == -1){
            if(DEBUG) LogUtil.error(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){

        final int match = uriMatcher.match(uri);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int noOfRowsDeleted = 0;
        switch(match){
            case PRODUCTS:
                noOfRowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;

            case PRODUCT_ID:{

                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                noOfRowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection,
                        selectionArgs);

            }
            break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(noOfRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return noOfRowsDeleted;
    }


    @Override
    public String getType(Uri uri){

        final int match = uriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs){
        final int match = uriMatcher.match(uri);

        switch(match){
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        // If there are no values to update, then don't try to update the database
        if(values.size() == 0){
            return 0;
        }

        // check that the name value is not null.
        if(values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)){
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if(name == null){
                throw new IllegalArgumentException("Product name is required.");
            }
        }

        // check that the price value is valid.
        if(values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)){
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if(price == null){
                throw new IllegalArgumentException("Product price is required");
            }
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int noOfRowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection,
                selectionArgs);
        if(noOfRowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return noOfRowsUpdated;

    }

}
