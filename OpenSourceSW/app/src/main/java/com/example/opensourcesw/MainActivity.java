package com.example.opensourcesw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import android.graphics.Bitmap;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Blob;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    static int currentIndex;
    static int numImage;
    ImageView imageView = (ImageView) findViewById(R.id.imageView);

    private List<String> pathList = new ArrayList<>();
    private List<Bitmap> imgList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDatabase();
        createTable();



        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button mapButton = (Button) findViewById(R.id.mapButton);
        Button diaryButton = (Button) findViewById(R.id.diaryButton);



        //EditText editTextTag = (EditText) findViewById(R.id.editText);
        //EditText startDate = (EditText) findViewById(R.id.editText2);


        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrevButtonPressed();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonPressed();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapButtonClicked();
            }
        });

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDiaryButtonClicked();
            }
        });

        galleryInfoLink();
        /*
        editTextTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                onTagNameInput(editTextTag);
                return true;
            }
        });

        startDate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                onPeriodInput(startDate);
                return true;
            }
        });
        */

    }

    private static float[] getLatLongFromImageFile(String imagePath) {
        float[] latLong = new float[2];
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            if (exifInterface.getLatLong(latLong)) {
                return latLong;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createDatabase(){
        database = openOrCreateDatabase("os", MODE_PRIVATE, null);
    }

    private void createTable(){
        if(database == null){
            return;
        }
        database.execSQL("create table image" + "(" + "_id integer PRIMARY KEY autoincrement, " +
                " img BLOB, " + " tag text, " + " date DATE, " + " latitude FLOAT, " + " longitude FLOAT, " + " filepath VARCHAR(300))");
    }

    protected void galleryInfoLink(){
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
        };

        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                @SuppressLint("Range")
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                @SuppressLint("Range")
                String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                @SuppressLint("Range")
                Date date_added = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)));
                try{
                    String filePath = data + display_name;
                    File file = new File(filePath);

                    float[] latlong = getLatLongFromImageFile(filePath);
                    float latitude = latlong[0];
                    float longitude = latlong[1];

                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                        ExifInterface exif = new ExifInterface(filePath);
                        String tagValue = exif.getAttribute(ExifInterface.TAG_MAKE);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

                        contentValues.put("_id", id);
                        contentValues.put("img", byteArray);
                        contentValues.put("tag", tagValue);
                        contentValues.put("date", sdf.format(date_added));
                        contentValues.put("latitude", latitude);
                        contentValues.put("longitude", longitude);
                        contentValues.put("filePath", filePath);
                    }
                } catch(Exception e){
                    return;
                }
            }
            cursor.close();
        }

    }

    private void showImageList(int idx){
        if(imgList.size() != 0){
            imageView.setImageBitmap(imgList.get(idx));
        }
    }

    protected void onPrevButtonPressed(){
        if(currentIndex != 0){
            showImageList(--currentIndex);
        }
        else{
            Toast.makeText(null, "첫 번째 이미지입니다.", Toast.LENGTH_SHORT);
        }
    }

    protected void onNextButtonPressed(){
        if(currentIndex != numImage - 1){
            showImageList(++currentIndex);
        }
        else{
            Toast.makeText(null, "마지막 이미지입니다.", Toast.LENGTH_SHORT);
        }
    }

    protected void onTagNameInput(EditText et){
        String inputText = et.getText().toString();
        String sql = "select filePath from image where tag like '%" + inputText + "%'";
        Cursor cursor = database.rawQuery(sql, null);

        numImage = cursor.getCount();
        currentIndex = 0;

        if(imgList.size() != 0){
            imgList = new ArrayList<Bitmap>();
        }

        for(int i = 0; i < numImage; i++){
            Bitmap bitmap = BitmapFactory.decodeFile(pathList.get(i));
            imgList.add(bitmap);
        }

        showImageList(currentIndex);
    }

    protected void onPeriodInput(EditText et){
        String inputText = et.getText().toString();
        try{
            String arr[] = inputText.split("/");
            if(arr.length != 2){
                throw new Exception();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
            Date startDate = sdf.parse(arr[0]);
            Date endDate = sdf.parse(arr[1]);

            String sql = "select display_name from image where date between '" + startDate + "' and '" + endDate + "'";
            Cursor cursor = database.rawQuery(sql, null);
            numImage = cursor.getCount();
            currentIndex = 0;

            if(pathList.size() != 0){
                pathList = new ArrayList<String>();
            }

            for(int i = 0; i < numImage; i++){
                pathList.add(cursor.getString(i));
            }

            if(imgList.size() != 0){
                imgList = new ArrayList<Bitmap>();
            }

            for(int i = 0; i < numImage; i++){
                Bitmap bitmap = BitmapFactory.decodeFile(pathList.get(i));
                imgList.add(bitmap);
            }
            showImageList(currentIndex);
        }catch(Exception e){
            Toast.makeText(null, "Invalid Input", Toast.LENGTH_SHORT);
        }
    }

    protected void onMapButtonClicked(){

    }

    protected void onDiaryButtonClicked(){

    }
}