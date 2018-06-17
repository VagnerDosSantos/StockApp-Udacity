package com.example.android.stockapppt2.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.stockapppt2.R;
import com.example.android.stockapppt2.database.StoreContract.ProductEntry;

public class StoreProvider extends ContentProvider {

    private StoreDbHelper mDbHelper;

    /**
     * URI matcher para todos os produtos da tabela
     */
    private static final int PRODUCT = 100;

    /**
     * URI matcher para apenas 1 produto da tabela
     */
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Inicializador static. Isso é executado na primeira vez que qualquer coisa é chamada dessa classe.
    static {
        // As chamadas para addURI () são feitas aqui, para todos os padrões de URI de conteúdo que o provedor
        // deve reconhecer. Todos os caminhos adicionados ao UriMatcher possuem um código correspondente para retornar
        // quando uma correspondência é encontrada.
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCT, PRODUCT);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Para o código PRODUCT, consulte a tabela de produtos diretamente com o dado
                cursor = database.query(ProductEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Isto irá realizar uma consulta na tabela e trazer dados da linha que contenha o _ID informado
                // Cursor contendo essa linha da tabela.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Não é possivel consultar um URI desconhecido " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("URI Desconhecido " + uri + " Com match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("A inserção não é suportada por " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // Checa valores
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Requer o nome do produto");
        }

        double price = values.getAsDouble(ProductEntry.COLUMN_PRICE);
        if (Double.valueOf(price).toString().isEmpty()) {
            throw new IllegalArgumentException("Requer o preço de venda");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);

        // Se o ID é -1, então a inserção falhou. Logue um error e retorne nulo.
        if (id == -1) {
            Log.e("TAG", "Falha ao inserir registro pela uri " + uri);
            return null;
        } else {
            Toast.makeText(getContext(), R.string.produto_salvo, Toast.LENGTH_SHORT).show();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri, null);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_ID:
                // Deleta um único registro dado pelo ID na URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("A exclusão não é suportada para a uri " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("A Atualização não é suportada para a URI " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Executa a atualização no banco de dados e obtém o número de linhas afetadas
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // Se 1 ou mais linhas foram atualizadas, então notifica todos os listeners que os dados na
        // dada URI mudaram
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Retorna o número de registros do banco de dados afetados pelo comando update
        return rowsUpdated;
    }
}