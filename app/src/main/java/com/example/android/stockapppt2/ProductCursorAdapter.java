package com.example.android.stockapppt2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockapppt2.database.StoreContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {
    ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productName = view.findViewById(R.id.descricao_produto);
        TextView price = view.findViewById(R.id.preco_produto);
        TextView quantity = view.findViewById(R.id.estoque_produto);
        ImageView cart = view.findViewById(R.id.cart);

        // Extrai os valores das colunas do BD
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int productColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);

        String nome = cursor.getString(productColumnIndex);
        String preco = cursor.getString(priceColumnIndex);
        final int quantidade = cursor.getInt(quantityColumnIndex);
        final int productId = cursor.getInt(idColumnIndex);

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = quantidade - 1;
                String mSelectionClause = ProductEntry._ID + "=?";
                String[] mSelectionArg = {String.valueOf(productId)};

                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_QUANTITY, i);

                if (quantidade <= 0) {
                    Toast.makeText(context.getApplicationContext(), context.getString
                            (R.string.alert_produto_sem_estoque), Toast.LENGTH_SHORT).show();
                } else {
                    context.getContentResolver().update(ProductEntry.CONTENT_URI, values, mSelectionClause, mSelectionArg);
                }
            }
        });

        productName.setText(nome);
        price.setText(preco);
        if (quantidade == 0) {
            quantity.setText(context.getString(R.string.sem_estoque));
        } else {
            quantity.setText(String.valueOf(quantidade));
        }
    }
}