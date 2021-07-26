package com.example.whatsinmyfridge.myfridge;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.example.whatsinmyfridge.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import 	androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsinmyfridge.myfridge.data.FoodContract;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private FoodCursorAdapter mCursorAdapter;
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mCursorAdapter = new FoodCursorAdapter(this, null);

        ListView foodList = (ListView) findViewById(R.id.list_view);
        foodList.setAdapter(mCursorAdapter);

        mEmptyView = findViewById(R.id.empty_view);
        foodList.setEmptyView(mEmptyView);

        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Uri uri = ContentUris.withAppendedId(FoodContract.FoodEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, FoodViewActivity.class);
                intent.putExtra(FoodContract.FoodEntry.FOOD_URI_KEY, uri);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_delete_all_entries) {
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[] {
                FoodContract.FoodEntry._ID,
                FoodContract.FoodEntry.COLUMN_NAME,
                FoodContract.FoodEntry.COLUMN_AMOUNT,
                FoodContract.FoodEntry.COLUMN_UNIT
        };
        return new CursorLoader(this, FoodContract.FoodEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
        }
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    private void showDeleteDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.delete_all_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        ((TextView) view.findViewById(R.id.delete_all_dialog_instruct))
                .setText(String.format(getString(R.string.delete_all_dialog_instruct), getString(R.string.confirm_delete_keyword)));
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllItems();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) dialogInterface.dismiss();
            }
        });
        final EditText input = (EditText) view.findViewById(R.id.delete_all_prompt_input);
        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String entered = editable.toString().toLowerCase();
                if (entered.equals(getString(R.string.confirm_delete_keyword)))
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
    }


    private void deleteAllItems() {
        int rowsAffected = getContentResolver().delete(FoodContract.FoodEntry.CONTENT_URI, null, null);
        if (rowsAffected > 0) {
            Toast.makeText(this, "All items deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error deleting items. No changes made.", Toast.LENGTH_SHORT).show();
        }
    }
}
