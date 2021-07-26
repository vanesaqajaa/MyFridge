package com.example.whatsinmyfridge.myfridge;

import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.CursorLoader;
import androidx.core.content.FileProvider;
import android.app.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.loader.app.LoaderManager.LoaderCallbacks;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.example.whatsinmyfridge.R;
import com.example.whatsinmyfridge.myfridge.data.FoodContract;
import com.example.whatsinmyfridge.myfridge.data.FoodDbHelper;
import com.example.whatsinmyfridge.myfridge.data.UnitContract;





public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {
    private final String LOG_TAG = EditorActivity.class.getName();

    private Uri mUri;

    private FoodDbHelper mDbHelper;

    private EditText mNameTextView;
    private EditText mAmountTextView;
    private EditText mStoreTextView;
    private EditText mPriceTextView;
    private EditText mExpTextView;
    private ArrayList<EditText> mEditTexts;
    private Spinner mUnitSpinner;
    private ImageView mPhotoView;

    private int mUnit;
    private double mAmount;
    private long mExpDate;
    private String mPhotoPath;

    private Button mPhotoButton;

    private static final int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mDbHelper = new FoodDbHelper(this);

        mNameTextView = (EditText) findViewById(R.id.edit_item_name);
        mAmountTextView = (EditText) findViewById(R.id.edit_item_amount);
        mStoreTextView = (EditText) findViewById(R.id.edit_item_store);
        mPriceTextView = (EditText) findViewById(R.id.edit_item_price);
        mExpTextView = (EditText) findViewById(R.id.edit_item_expiration);
        mUnitSpinner = (Spinner) findViewById(R.id.edit_item_unit);
        mPhotoView = (ImageView) findViewById(R.id.edit_item_photo);

        mEditTexts = new ArrayList<>();
        mEditTexts.add(mNameTextView);
        mEditTexts.add(mAmountTextView);
        mEditTexts.add(mStoreTextView);
        mEditTexts.add(mPriceTextView);
        mEditTexts.add(mExpTextView);

        View.OnClickListener expClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd =
                        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                                EditorActivity.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        };

        mExpTextView.setOnClickListener(expClickListener);

        mPhotoButton = (Button) findViewById(R.id.action_add_photo);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error creating file for photo.", e);
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(EditorActivity.this, ".myfridge.fileprovider", photoFile);
                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(pictureIntent, IMAGE_REQUEST_CODE);
                    }
                }
            }
        });

        setupSpinner();

        Uri uri = getIntent().getParcelableExtra(FoodContract.FoodEntry.FOOD_URI_KEY);
        if (uri != null) {
            mUri = uri;
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.editor_save:
                boolean saved = saveItem();
                if (saved) {
                    finish();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, null, null, null, null);
    }




    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_NAME));
            mNameTextView.setText(name);

            mUnit = data.getInt(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_UNIT));
            mUnitSpinner.setSelection(mUnit-1);

            double amount = data.getDouble(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_AMOUNT));
            mAmount = Utils.convert(amount, mUnit, false, this);
            BigDecimal amountBd = new BigDecimal(mAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            String amountString = amountBd.toString();
            mAmountTextView.setText(amountString);

            String store = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_STORE));
            if (!TextUtils.isEmpty(store)) {
                mStoreTextView.setText(store);
            }
            String expString = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_EXPIRATION));
            if (!TextUtils.isEmpty(expString)) {
                Calendar now = Calendar.getInstance();
                mExpDate = Long.parseLong(expString);
                now.setTimeInMillis(mExpDate);
                String dateString = (now.get(Calendar.MONTH)+1) + "/" + now.get(Calendar.DAY_OF_MONTH) + "/" + now.get(Calendar.YEAR);
                mExpTextView.setText(dateString);
            }

            String priceString = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_PRICE_PER));
            if (!TextUtils.isEmpty(priceString)) {
                double price = Double.parseDouble(priceString);
                Log.d(LOG_TAG, "absolute price before conversion: " + price);
                price = mAmount * Utils.convert(price, mUnit, true, this);
                BigDecimal priceBd = new BigDecimal(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                mPriceTextView.setText(priceBd.toString());
            }

            String photoPath = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_PHOTO));
            if (!TextUtils.isEmpty(photoPath)) {
                setPhotoView(photoPath);
                mPhotoButton.setText(R.string.button_photo_change);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        for (EditText view : mEditTexts) {
            view.setText(null);
        }
    }


    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;
        mExpTextView.setText(date);
        Calendar now = Calendar.getInstance();
        now.set(year, monthOfYear, dayOfMonth);
        mExpDate = now.getTimeInMillis();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            setPhotoView(mPhotoPath);
            mPhotoButton.setText(R.string.button_photo_change);
        }
    }


    private boolean saveItem() {
        String name = mNameTextView.getText().toString();
        String store = mStoreTextView.getText().toString();
        String priceString = mPriceTextView.getText().toString();
        String amountString = mAmountTextView.getText().toString();
        String expiration = mExpTextView.getText().toString();

        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(name)) {
            values.put(FoodContract.FoodEntry.COLUMN_NAME, name);
        } else {
            Toast.makeText(this, "The item must have a name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(amountString)) {
            Toast.makeText(this, "The item must have an amount!", Toast.LENGTH_SHORT).show();
            return false;
        }
        values.put(FoodContract.FoodEntry.COLUMN_UNIT, mUnit);

        if (!TextUtils.isEmpty(expiration)) {
            values.put(FoodContract.FoodEntry.COLUMN_EXPIRATION, mExpDate);
        }

        if (!TextUtils.isEmpty(store)) {
            values.put(FoodContract.FoodEntry.COLUMN_STORE, store);
        }

        if (!TextUtils.isEmpty(mPhotoPath)) {
            values.put(FoodContract.FoodEntry.COLUMN_PHOTO, mPhotoPath);
        }

        float amount = Float.parseFloat(amountString);
        if (Math.abs(amount - mAmount) > 0.005) {
            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, amount);
        } else {
            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, mAmount);
        }

        if (!TextUtils.isEmpty(priceString)) {
            float price = Float.parseFloat(priceString);
            values.put(FoodContract.FoodEntry.COLUMN_PRICE_PER, price);
        }

        if (mUri == null) {
            getContentResolver().insert(FoodContract.FoodEntry.CONTENT_URI, values);
        } else {
            getContentResolver().update(mUri, values, null, null);
        }

        return true;
    }


    private void setupSpinner() {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_NAME};
        Cursor cursor = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, null, null, null, null, UnitContract.UnitEntry._ID);
        final ArrayList<String> unitArray = new ArrayList<>();
        while (cursor.moveToNext()) {
            unitArray.add(cursor.getString(cursor.getColumnIndex(UnitContract.UnitEntry.COLUMN_NAME)));
        }
        cursor.close();
        db.close();
        Log.d(LOG_TAG, "First three units: " + unitArray.get(0) + ", " + unitArray.get(1) + ", " + unitArray.get(2));

        ArrayAdapter unitSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, unitArray);

        unitSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dialog_item);

        mUnitSpinner.setAdapter(unitSpinnerAdapter);

        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Log.d(LOG_TAG, "Position: " + position + ", ID: " + l);
                mUnit = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mUnit = 1;
            }
        });
    }


    private void setPhotoView(String photoPath) {

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case 6:
                    matrix.postRotate(90);
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 8:
                    matrix.postRotate(270);
                    break;
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            Log.e(LOG_TAG, "problem loading image", e);
        }

        mPhotoView.setImageBitmap(bitmap);
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FOOD_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        mPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }
}
