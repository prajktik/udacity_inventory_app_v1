package com.example.android.inventory;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;
import com.example.android.inventory.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>{

    private static final boolean DEBUG = true;
    private static final String LOG_TAG = EditorActivity.class.getName();


    private static final android.net.Uri GALLERY_URI = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;
    private static final String MIME_TYPE = "image/*";

    private static final int REQUEST_GALLERY = 1;
    private static final int EXISTING_PRODUCT_LOADER = CatalogActivity.PRODUCT_LOADER;

    private static final int MAX_NAME_LENGTH = 3;
    private static final int MAX_PRICE_QTY_LENGTH = 6;
    private static final int MIN_PRICE_QTY_LENGTH = 1;

    private ImageView ivProductImage;
    private Uri productImageUri;

    private EditText etName;
    private EditText etPrice;
    private TextView tvProductQuantity;
    private FloatingActionButton fabReduceButton;
    private FloatingActionButton fabAddButton;
    private Uri inventoryUri;

    private boolean hasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event){
            hasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        inventoryUri = getIntent().getData();

        if(inventoryUri == null){
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
        }else{
            if(DEBUG)LogUtil.verbose(LOG_TAG,"inventoryUri  is "+inventoryUri.toString());
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        etName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_product_price);
        tvProductQuantity = findViewById(R.id.tv_product_quantity);

        FloatingActionButton fabAddImage = findViewById(R.id.fab_add_product_image);
        ivProductImage = findViewById(R.id.iv_product_image);

        etName.setOnTouchListener(touchListener);
        etPrice.setOnTouchListener(touchListener);

        fabReduceButton = findViewById(R.id
                .details_quantity_decrease_button);
        fabAddButton = findViewById(R.id
                .details_quantity_increase_button);

        fabReduceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                int currentQuantity = Integer.parseInt(tvProductQuantity.getText().toString().trim
                        ());
                if(currentQuantity > 0){
                    currentQuantity--;
                    tvProductQuantity.setText(String.valueOf(currentQuantity));
                }
            }

        });

        fabAddButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                int currentQuantity = Integer.parseInt(tvProductQuantity.getText().toString()
                        .trim());

                currentQuantity++;
                tvProductQuantity.setText(String.valueOf(currentQuantity));

            }

        });



        fabAddImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, GALLERY_URI);
                photoPickerIntent.setType(MIME_TYPE);
                startActivityForResult(photoPickerIntent, REQUEST_GALLERY);
                checkPermission();
                openGallery();
            }
        });

    }

    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY);

        }
    }

    private void openGallery(){

        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(MIME_TYPE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        switch(requestCode){
            case REQUEST_GALLERY:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openGallery();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && null != data){
            Uri imageUri = data.getData();
            productImageUri = imageUri;
            ivProductImage.setImageURI(imageUri);
            ivProductImage.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_save:{

                String stringName = etName.getText().toString().trim();
                String stringPrice = etPrice.getText().toString().trim();
                String stringQuantity = tvProductQuantity.getText().toString().trim();

                if((stringName.length() < MAX_NAME_LENGTH) || (stringPrice.length() <
                        MIN_PRICE_QTY_LENGTH || stringPrice.length() > MAX_PRICE_QTY_LENGTH) ||
                        (stringQuantity.length() < MIN_PRICE_QTY_LENGTH || stringQuantity.length
                                () > MAX_PRICE_QTY_LENGTH)){
                    showToast(getString(R.string.all_fields_required));
                }
                if(productImageUri == null){
                    showToast(getString(R.string.select_image));
                }else{
                    saveProduct();
                    finish();
                }
            }
            return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_order:{

                placeOrder(etName.getText().toString().trim());
                return true;
            }

            case android.R.id.home:
                if(!hasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){

                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };

                showUnsavedChangedDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_current_product));
        builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface
                .OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                deleteProduct();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_no), new DialogInterface
                .OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct(){

        if(inventoryUri != null){
            int rowDeleted = getContentResolver().delete(inventoryUri, null, null);

            if(rowDeleted == 0){
                showToast(getString(R.string.delete_fail));

            }else{
                showToast(getString(R.string.delete_success));

            }

            finish();
        }

    }

    private void placeOrder(String productName){

        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.place_order_for));
        builder.append(productName);
        builder.append(getString(R.string.order_message));

        String summary = builder.toString();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, summary);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){
        if(!hasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                        finish();
                    }
                };

        showUnsavedChangedDialog(discardButtonClickListener);
    }

    private void showUnsavedChangedDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.unsaved_changes_dialog_msg));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface
                .OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){

                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveProduct(){

        String stringName = etName.getText().toString().trim();
        String stringPrice = etPrice.getText().toString().trim();
        String stringQuantity = tvProductQuantity.getText().toString().trim();

        if(inventoryUri == null &&
                TextUtils.isEmpty(stringName) && TextUtils.isEmpty(stringPrice)
                && TextUtils.isEmpty(stringQuantity)){
            return;
        }
        int price = Integer.parseInt(stringPrice);
        int quantity = Integer.parseInt(stringQuantity);
        String imageUriString = productImageUri.toString();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, stringName);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageUriString);

        if(inventoryUri == null){

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if(newUri == null){
                showToast(getString(R.string.editor_insert_product_failed));
            }else{
                showToast(getString(R.string.editor_insert_product_successful));
            }
        }else{

            int rowsAffected = getContentResolver().update(inventoryUri, values, null, null);

            if(rowsAffected == 0){
                showToast(getString(R.string.editor_update_product_failed));
            }else{
                showToast(getString(R.string.editor_update_product_successful));
            }
        }
    }

    private void showToast(String string){

        Toast.makeText(EditorActivity.this, string, Toast.LENGTH_LONG).show();

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
                inventoryUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){

        if(data == null || data.getCount() < 1){
            return;
        }

        if(data.moveToFirst()){


            int idColumnIndex = data.getColumnIndex(ProductEntry._ID);
            int id = data.getInt(idColumnIndex);
            if(DEBUG)LogUtil.verbose(LOG_TAG, "onLoadFinished -- cursor ID is = "+id);

            int imageColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

           String imageStr = data.getString(imageColumnIndex);

            if(imageStr == null || imageStr.isEmpty()){
                ivProductImage.setImageResource(R.drawable.ic_new_product);
            }else{
                productImageUri = Uri.parse(imageStr);
                ivProductImage.setImageURI(productImageUri);
                ivProductImage.invalidate();
            }

            String name = data.getString(nameColumnIndex);
            int price = data.getInt(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            etName.setText(name);
            etPrice.setText(String.valueOf(price));
            tvProductQuantity.setText(String.valueOf(quantity));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){

        ivProductImage.setImageResource(R.drawable.ic_new_product);
        etName.setText("");
        etPrice.setText("");
        tvProductQuantity.setText("");

    }

}
