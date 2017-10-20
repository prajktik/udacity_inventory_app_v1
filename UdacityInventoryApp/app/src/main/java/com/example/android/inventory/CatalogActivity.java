package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventory.data.ProductContract;
import com.example.android.inventory.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>{

    private static final boolean DEBUG = true;
    private static final String LOG_TAG = CatalogActivity.class.getName();

    static final int PRODUCT_LOADER = 0;
    private ProductCursorAdapter productCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab_add_product);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView petListView = findViewById(R.id.list_view_products);

        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        productCursorAdapter = new ProductCursorAdapter(this, null);
        petListView.setAdapter(productCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri contentUri = ContentUris.withAppendedId(ProductEntry
                        .CONTENT_URI, id);
                if(DEBUG)LogUtil.verbose(LOG_TAG,"OnItemClick id is "+id );
                intent.setData(contentUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertProduct(){

        ContentValues values = new ContentValues();
        Uri defaultImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_new_product)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_new_product) + '/' +
                getResources().getResourceEntryName(R.drawable.ic_new_product));

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Camera");
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, defaultImageUri.toString());
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, "30");
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 7);

        Uri rowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

        Log.i(LOG_TAG, "insertProduct rowURI = " + rowUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            case R.id.action_insert_dummy_data:{
                insertProduct();
                return true;
            }

            case R.id.action_delete_all_entries:{

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete_all_dialog_msg));
                builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface
                        .OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        deleteAllProducts();
                    }
                });

                builder.setNegativeButton(getString(R.string.dialog_no), new DialogInterface
                        .OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int i){
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts(){

        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI,
                null, null);
        if(DEBUG) LogUtil.verbose(LOG_TAG, rowsDeleted + " rows deleted from inventory");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        productCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        productCursorAdapter.swapCursor(null);
    }
}