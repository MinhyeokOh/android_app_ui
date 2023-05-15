package com.example.opensourcesw;
import java.nio.charset.Charset;
import com.example.opensourcesw.R;
import java.net.URLEncoder;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import android.Manifest;
import android.content.Context;
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
import android.database.sqlite.SQLiteOpenHelper;
import com.example.opensourcesw.MyDatabaseHelper;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.os.Environment;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    static SQLiteDatabase database;
    static int currentIndex;
    static int numImage;
    ImageView imageView;
    SQLiteOpenHelper mdh = new MyDatabaseHelper(MainActivity.this);

    static List<String> pathList = new ArrayList<>();
    static List<Bitmap> imgList = new ArrayList<>();
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

        imageView = (ImageView) findViewById(R.id.imageView);

        EditText editTextTag = (EditText) findViewById(R.id.editText);
        EditText DateText = (EditText) findViewById(R.id.editText2);


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



        editTextTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                onTagNameInput(editTextTag);
                return true;
            }
        });

        DateText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                onPeriodInput(DateText);
                return true;
            }
        });

        galleryInfoLink();
        showImageList(0);


    }


    public void createDatabase(){
        database = openOrCreateDatabase("opensource", MODE_PRIVATE, null);
    }

    public void createTable(){
        if(database == null){
            return;
        }
        database.execSQL("create table if not exists image " + "(" + "_id integer PRIMARY KEY autoincrement, " +
                " img BLOB, " + " tag text, " + " date DATE, " + " latitude FLOAT, " + " longitude FLOAT, " + " filepath VARCHAR(300), " + "diary VARCHAR(500));");
    }

    protected void galleryInfoLink(){
        ContentValues contentValues = new ContentValues();
        String filePath = "";

        String tags = "";
        int n = 0;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
        };
        String sortOrder = MediaStore.Images.Media._ID + " DESC";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },  MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }


        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);


        if (cursor != null)
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                @SuppressLint("Range")
                int rd = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH);
                try {
                    filePath = cursor.getString(rd);
                }catch(Exception e){
                    continue;
                }
                @SuppressLint("Range")
                String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                @SuppressLint("Range")
                Date date_taken = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)));
                long timeInMillis = date_taken.getTime();
                try{
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath + display_name;
                    File file = new File(filePath);

                    ExifInterface exifInterface = new ExifInterface(filePath);

                    float latitude;
                    float longitude;
                    try {
                        latitude = Float.parseFloat(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                        longitude = Float.parseFloat(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
                    }catch(Exception e){
                        latitude = 0.0f;
                        longitude = 0.0f;
                    }


                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        contentValues.put("_id", id);
                        contentValues.put("img", byteArray);

                        contentValues.put("date", timeInMillis);
                        contentValues.put("latitude", latitude);
                        contentValues.put("longitude", longitude);
                        contentValues.put("filePath", filePath);

                        database.insertWithOnConflict("image", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                        imgList.add(bitmap);

                    }

                } catch(Exception e){
                    return;
                }
                numImage = imgList.size();
            }

            if(imgList.size() != 0) {
                showImageList(0);
            }
            cursor.close();



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
            Toast toastView = Toast.makeText(this, "첫 번째 이미지입니다.", Toast.LENGTH_SHORT);
            toastView.show();
        }
    }

    protected void onNextButtonPressed(){
        if(currentIndex != numImage - 1){
            showImageList(++currentIndex);
        }
        else{
            Toast toastView = Toast.makeText(this, "마지막 이미지입니다.", Toast.LENGTH_SHORT);
            toastView.show();
        }
    }

    protected void onTagNameInput(EditText et){
        String inputText = et.getText().toString();
        System.out.println(inputText);
        String sql = "SELECT filePath FROM image WHERE tag LIKE '%?%';";
        Cursor cursor = database.rawQuery(sql, new String[]{inputText});
        int n = 0;

        if (cursor.moveToFirst()) {
            n = cursor.getCount();
        }

        if(n == 0){
            Toast toastView = Toast.makeText(this, "해당 태그를 가진 이미지가 없습니다.", Toast.LENGTH_SHORT);
            toastView.show();
            return;
        }
        currentIndex = 0;

        if(imgList.size() != 0){
            imgList.clear();
        }

        for(int i = 0; i < numImage; i++){
            Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(i));
            imgList.add(bitmap);
        }

        currentIndex = 0;
        showImageList(currentIndex);
    }

    protected void onPeriodInput(EditText et){
        String inputText = et.getText().toString();
        try{
            String arr[] = inputText.split("~");
            if(arr.length != 2){
                throw new Exception();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date sd = sdf.parse(arr[0]);
            Date ed = sdf.parse(arr[1]);

            String startDate = sdf.format(sd);
            String endDate = sdf.format(ed);

            String sql = "select filePath from image where date between ? and ?";
            Cursor cursor = database.rawQuery(sql, new String[]{startDate, endDate});
            int n = 0;

            if (cursor.moveToFirst()) {
                n = cursor.getCount();
            }
            if(n == 0){
                Toast toastView = Toast.makeText(this, "해당 기간에 촬영한 이미지가 없습니다.", Toast.LENGTH_SHORT);
                toastView.show();
                return;
            }
            currentIndex = 0;

            if(imgList.size() != 0){
                imgList.clear();
            }

            for(int i = 0; i < numImage; i++){
                pathList.add(cursor.getString(i));
            }

            if(imgList.size() != 0){
                imgList.clear();
            }

            for(int i = 0; i < numImage; i++){
                Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(i));
                imgList.add(bitmap);
            }



            currentIndex = 0;
            showImageList(currentIndex);
        }catch(Exception e){
            Toast toastView = Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT);
            toastView.show();
            e.printStackTrace();
        }
    }

    protected void onMapButtonClicked(){

    }

    protected void onDiaryButtonClicked(){
        Intent intent = new Intent(this, MainActivity2.class);

        Bitmap bitmap = imgList.get(currentIndex);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("imageByte", byteArray);
        startActivity(intent);
    }
}