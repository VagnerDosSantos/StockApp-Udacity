package com.example.android.stockapppt2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.stockapppt2.database.StoreContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mProductName;
    private EditText mPrice;
    private EditText mQuantity;
    private EditText mProviderName;
    private EditText mProviderPhone;
    private ImageView mCallButton;
    private Spinner mCatSpinner;
    private Button mIncreaseButton;
    private Button mDecreaseButton;
    private int mQuantityValue;

    private int mCat = 0;
    private Uri mCurrentUri;
    private int URL_LOADER = 1;
    private boolean mCampoAlterado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mCallButton = findViewById(R.id.call);
        mProductName = findViewById(R.id.product_name);
        mPrice = findViewById(R.id.price);
        mQuantity = findViewById(R.id.quantity);
        mProviderName = findViewById(R.id.provider_name);
        mProviderPhone = findViewById(R.id.provider_phone);
        mIncreaseButton = findViewById(R.id.addition_button);
        mDecreaseButton = findViewById(R.id.decrease_button);

        mProductName.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mProviderName.setOnTouchListener(mTouchListener);
        mProviderPhone.setOnTouchListener(mTouchListener);
        mDecreaseButton.setOnTouchListener(mTouchListener);
        mIncreaseButton.setOnTouchListener(mTouchListener);

        // Recebe dados da MainActivity
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            setTitle(getString(R.string.cadastro_produtos_title));
            mCallButton.setVisibility(View.GONE);
            mIncreaseButton.setVisibility(View.GONE);
            mDecreaseButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.editar_produtos_title));
        }

        mCatSpinner = findViewById(R.id.cat_spinner);
        setupSpinner();

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private void setupSpinner() {
        ArrayAdapter catSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_cat_options, android.R.layout.simple_spinner_item);
        catSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mCatSpinner.setAdapter(catSpinnerAdapter);

        mCatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.cat_eletronicos))) {
                        mCat = ProductEntry.CAT_ELETRONICOS; // Eletronicos
                    } else if (selection.equals(getString(R.string.cat_automoveis))) {
                        mCat = ProductEntry.CAT_AUTOMOVEIS; // Automóveis
                    } else if (selection.equals(getString(R.string.cat_beleza))) {
                        mCat = ProductEntry.CAT_BELEZA; // Beleza
                    } else if (selection.equals(getString(R.string.cat_telefonia))) {
                        mCat = ProductEntry.CAT_TELEFONIA; // Telefonia
                    } else {
                        mCat = ProductEntry.CAT_DIVERSOS; // Diversos
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCat = 0; // Diversos
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                // Se houver alterações não salvas, exibe uma mensagem para o usuario
                DialogInterface.OnClickListener deleteButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Se o usuario clicou em Deletar, finaliza a activity
                                finish();
                            }
                        };
                showDeleteConfirmationDialog(deleteButtonClickListener);
                return true;
            case android.R.id.home:
                // Se nada mudou na activty, continua o fluxo normal
                if (!mCampoAlterado) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Se houver alterações não salvas, exibe um diálogo para alertar o usuário.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Usuário clidou no botão descartar
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Mostra um diálogo que notifica o usuário de que há alterações não salvas
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String name = mProductName.getText().toString().trim();
        double price = 0;
        int quantity = 0;
        if (!mPrice.getText().toString().isEmpty()) {
            price = Double.parseDouble(mPrice.getText().toString());
        }
        if (!mQuantity.getText().toString().isEmpty()) {
            quantity = Integer.parseInt(mQuantity.getText().toString());
        }
        String providerName = mProviderName.getText().toString().trim();
        String providerPhone = mProviderPhone.getText().toString();

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_CATEGORY, mCat);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PROVIDER_NAME, providerName);
        values.put(ProductEntry.COLUMN_PROVIDER_PHONE, providerPhone);

        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.alert_preencha_nome_produto), Toast.LENGTH_SHORT).show();
        } else if (mPrice.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.alert_informar_preco), Toast.LENGTH_SHORT).show();
        } else {
            if (mCurrentUri == null) {
                getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                finish();
            } else {
                getContentResolver().update(mCurrentUri, values, null, null);
                Toast.makeText(getApplicationContext(), R.string.produto_atualizado, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BaseColumns._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_CATEGORY,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PROVIDER_NAME,
                ProductEntry.COLUMN_PROVIDER_PHONE
        };
        if (mCurrentUri != null) {
            return new CursorLoader(this, mCurrentUri, projection, null, null, null);
        } else {
            return null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Acha as colunas de atributos em que estamos interessados
            int productColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int categoryColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_CATEGORY);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int providerNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_NAME);
            int providerPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PHONE);

            // Extrai o valor do Cursor para o índice de coluna dado
            final String name = cursor.getString(productColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int category = cursor.getInt(categoryColumnIndex);
            mQuantityValue = cursor.getInt(quantityColumnIndex);
            String providerName = cursor.getString(providerNameColumnIndex);
            final String providerPhone = cursor.getString(providerPhoneColumnIndex);

            // Atualiza as views na tela com os valores do banco de dados
            mProductName.setText(name);
            mPrice.setText(Double.toString(price));
            mQuantity.setText(Integer.toString(mQuantityValue));
            mProviderName.setText(providerName);
            mProviderPhone.setText(providerPhone);

            mCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + providerPhone));
                    startActivity(intent);
                }
            });

            // Configuração dos botões que diminui ou aumenta quantidade do produto
            mIncreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mQuantityValue++;
                    mQuantity.setText(String.valueOf(mQuantityValue));
                }
            });

            mDecreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mQuantityValue == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.alert_produto_sem_estoque),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mQuantityValue--;
                    mQuantity.setText(String.valueOf(mQuantityValue));
                }
            });
            // Marcar a opção correta no Spinner de acordo com o valor definido no BD.
            switch (category) {
                case ProductEntry.CAT_ELETRONICOS:
                    mCatSpinner.setSelection(1);
                    break;
                case ProductEntry.CAT_AUTOMOVEIS:
                    mCatSpinner.setSelection(2);
                    break;
                case ProductEntry.CAT_BELEZA:
                    mCatSpinner.setSelection(3);
                    break;
                case ProductEntry.CAT_TELEFONIA:
                    mCatSpinner.setSelection(4);
                    break;
                default:
                    mCatSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void delete() {
        getContentResolver().delete(mCurrentUri, null, null);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCampoAlterado = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Cria um AlertDialog.Builder e configura a mensagem
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.descartar_mudancas);
        builder.setPositiveButton(R.string.descartar, discardButtonClickListener);
        builder.setNegativeButton(R.string.continuar_editando, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Se usuário clicou no botão "Continuar editando" fecha a caixa de diálogo
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e mostra o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteButtonClickListener) {
        // Cria um AlertDialog e mostra mensagens para o usuario
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deletar_produto);
        builder.setPositiveButton(R.string.deletar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Se o usuario clicar em deletar
                delete();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Se o usuario clicar em cancelar
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e exibe o alertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mCampoAlterado) {
            super.onBackPressed();
            return;
        }

        // Se houver alterações não salvas, exibe uma caixa de diálogo para alertar o usuário.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicou no botão descartar, finaliza a activity e retorna para a MainActivity
                        finish();
                    }
                };

        // Mostra o diálogo que diz que há mudanças não salvas
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}