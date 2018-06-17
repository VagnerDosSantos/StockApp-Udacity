package com.example.android.stockapppt2.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class StoreContract {
    private StoreContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.stockapppt2";
    public static final String PATH_PRODUCT = "produtos";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static abstract class ProductEntry implements BaseColumns{

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        public static final String TABLE_NAME = "produtos";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "nome_produto";
        public static final String COLUMN_PRICE = "preco";
        public static final String COLUMN_CATEGORY = "categoria";
        public static final String COLUMN_QUANTITY = "quantidade";
        public static final String COLUMN_PROVIDER_NAME = "fornecedor";
        public static final String COLUMN_PROVIDER_PHONE = "telefone_fornecedor";

        // Valores para Spinner
        public static final int CAT_DIVERSOS = 0;
        public static final int CAT_ELETRONICOS = 1;
        public static final int CAT_AUTOMOVEIS = 2;
        public static final int CAT_BELEZA = 3;
        public static final int CAT_TELEFONIA = 4;
    }
} 