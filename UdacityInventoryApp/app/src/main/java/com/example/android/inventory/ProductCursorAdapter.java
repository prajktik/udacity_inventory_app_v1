package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;
import com.example.android.inventory.data.ProductContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter{

    public ProductCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor){


        ImageView ivProductImage = view.findViewById(R.id.list_product_image);
        TextView tvProductName =  view.findViewById(R.id.list_product_name);
        TextView tvProductPrice = view.findViewById(R.id.list_product_price);
        TextView tvProductQuantity = view.findViewById(R.id.list_quantity_value);
        ImageView ivMakeSale = view.findViewById(R.id.iv_list_make_sale);

        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        String imageStr = cursor.getString(imageColumnIndex);

        if(imageStr == null || imageStr.isEmpty()){
            ivProductImage.setImageResource(R.drawable.ic_new_product);
        }else{
            Uri imageUri = Uri.parse(imageStr);
            ivProductImage.setImageURI(imageUri);
        }

        String name = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        if (quantity == 0) {
            tvProductQuantity.setText(context.getString(R.string.out_of_stock));
            ivMakeSale.setEnabled(false);
        } else {
            tvProductQuantity.setText(String.valueOf(quantity));
            ivMakeSale.setEnabled(true);
        }

        tvProductName.setText(name);
        tvProductPrice.setText(R.string.currency_sign);
        tvProductPrice.append(String.valueOf(price));

        ivMakeSale.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,
                        productIdColumnIndex);
                updateQuantity(context, uri, quantity);
            }
        });

    }

    private void updateQuantity(Context context, Uri productUri, int currentQuantity) {

        if(currentQuantity <= 0){
            Toast.makeText(context.getApplicationContext(), R.string.toast_msg_sold_out, Toast
                    .LENGTH_SHORT).show();
            return;
        }

        currentQuantity--;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);

        if (numRowsUpdated <= 0) {

            Toast.makeText(context.getApplicationContext(), R.string.toast_msg_error_quantity_update, Toast.LENGTH_SHORT)
                    .show();
        }
    }


}
