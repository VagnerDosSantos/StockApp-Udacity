package com.example.android.stockapppt2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.stockapppt2.database.StoreContract.ProductEntry;

public class StoreDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductEntry.TABLE_NAME +
                    " (" + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0, " +
                    ProductEntry.COLUMN_CATEGORY + " INTEGER NOT NULL DEFAULT 0, " +
                    ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                    ProductEntry.COLUMN_PROVIDER_NAME + " TEXT, " +
                    ProductEntry.COLUMN_PROVIDER_PHONE + " TEXT)";

    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}