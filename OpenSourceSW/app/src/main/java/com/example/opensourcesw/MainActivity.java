package com.example.opensourcesw;
import android.content.Intent;
import android.location.Geocoder;
import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.location.Address;
import java.util.List;
import java.util.Locale;
import android.graphics.Bitmap;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.SupportMapFragment;
import android.location.Geocoder;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.KeyEvent;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;


    private Button nextButton;
    private Button mapButton;
    private Button diaryButton;
    private Button prevButton;

    private ArrayList<Bitmap> backUpList = new ArrayList<Bitmap>();

    static ConstraintLayout constraintLayout;
    static SQLiteDatabase database;
    static int currentIndex;
    static int numImage;
    ImageView imageView;
    SQLiteOpenHelper mdh = new MyDatabaseHelper(MainActivity.this);

    static ArrayList<String> pathList = new ArrayList<>();
    static ArrayList<Bitmap> imgList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDatabase();
        createTable();

        prevButton = (Button) findViewById(R.id.prevButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        mapButton = (Button) findViewById(R.id.mapButton);
        diaryButton = (Button) findViewById(R.id.diaryButton);

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
                public void onClick(View v) { onMapButtonClicked(); }
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
        database.setPageSize(4096);
    }

    public void createTable(){
        if(database == null) {
            return;
        }
        database.execSQL("create table if not exists image " + "(" + "_id integer PRIMARY KEY autoincrement, " +
                " img BLOB, " + " tag text, " + " date_taken DATETIME, " + " latitude REAL, " + " longitude REAL, " + " filepath VARCHAR(300), " + "diary VARCHAR(600));");
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
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DESCRIPTION
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
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                String dateString = cursor.getString(columnindex);

                tags = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
                if(tags == ""){
                    tags = "";
                }

                double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));


                try{
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath + display_name;
                    File file = new File(filePath);



                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        contentValues.put("_id", id);
                        contentValues.put("img", byteArray);
                        contentValues.put("date_taken", dateString);
                        contentValues.put("tag", tags);
                        contentValues.put("latitude", latitude);
                        contentValues.put("longitude", longitude);
                        contentValues.put("filePath", filePath);
                        contentValues.put("diary", " ");


                        database.insertWithOnConflict("image", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

                        imgList.add(bitmap);
                        pathList.add(filePath);
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
        if(currentIndex != numImage - 1 && numImage > 1){
            showImageList(++currentIndex);
        }
        else{
            Toast toastView = Toast.makeText(this, "마지막 이미지입니다.", Toast.LENGTH_SHORT);
            toastView.show();
        }
    }

    protected void onTagNameInput(EditText et){
        String inputText = et.getText().toString();

        String sql = "SELECT filePath FROM image WHERE tag LIKE ?";
        String[] selectionArgs = {"%" + inputText + "%"};
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, selectionArgs);
            currentIndex = 0;
            if(cursor.getCount() == 0){
                throw new Exception();
            }
            imgList.clear();
            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    int columnIndex = cursor.getColumnIndex("filepath");
                    Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(columnIndex));
                    imgList.add(bitmap);
                }while (cursor.moveToNext());
            }


        currentIndex = 0;
        numImage = imgList.size();

        showImageList(currentIndex);
        }catch(Exception e){
            Toast toastView = Toast.makeText(this, "해당 태그를 가진 이미지가 존재하지 않습니다.", Toast.LENGTH_SHORT);
            toastView.show();
            e.printStackTrace();
            return;
        }
    }

    protected void onPeriodInput(EditText et){
        String inputText = et.getText().toString();
        try{
            String arr[] = inputText.split("~");
            if(arr.length != 2){
                throw new Exception();
            }

            String sd = arr[0];
            String ed = arr[1];

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date sd2 = sdf.parse(sd);
            Date ed2 = sdf.parse(ed);

            String startDate = sdf.format(sd2);
            String endDate = sdf.format(ed2);

            String sql = "SELECT filePath FROM image WHERE strftime('%Y-%m-%d', datetime(date_taken / 1000, 'unixepoch')) >= ? AND strftime('%Y-%m-%d', datetime(date_taken / 1000, 'unixepoch')) <= ?";
            String[] selectionArgs = new String[]{ startDate, endDate };
            Cursor cursor = null;
            cursor = database.rawQuery(sql, selectionArgs);
            if(cursor.getCount() == 0){
                Toast tv = Toast.makeText(this, "해당 기간에 촬영한 이미지가 없습니다.", Toast.LENGTH_SHORT);
                tv.show();
                return;
            }
                currentIndex = 0;


                imgList.clear();

                int index = 0;
            if (cursor.moveToFirst()) {

                do {
                    int columnIndex = cursor.getColumnIndex("filepath");
                    Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(columnIndex));
                    imgList.add(bitmap);
                }while (cursor.moveToNext());
            }


            currentIndex = 0;
            numImage = imgList.size();
            showImageList(currentIndex);


        }catch(Exception e){
            Toast toastView = Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT);
            toastView.show();
            e.printStackTrace();
        }
    }

    protected void onMapButtonClicked() {

        Geocoder geocoder = new Geocoder(this);

        nextButton.setVisibility(View.GONE);
        mapButton.setVisibility(View.GONE);
        diaryButton.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);

        MapsFragment fragment = new MapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        backUpList.clear();

        for(int i = 0; i < imgList.size(); i++){
            backUpList.add(imgList.get(i));
        }

        transaction.commit();
            fragmentManager.addOnBackStackChangedListener(
                    new FragmentManager.OnBackStackChangedListener() {

                        @Override
                        public void onBackStackChanged() {

                            imgList.clear();
                            if (fragmentManager.getBackStackEntryCount() == 0) {

                                String selectedAdd = fragment.getAddressName();
                                System.out.println(selectedAdd + " 를 선택함");

                                nextButton.setVisibility(View.VISIBLE);
                                mapButton.setVisibility(View.VISIBLE);
                                diaryButton.setVisibility(View.VISIBLE);
                                prevButton.setVisibility(View.VISIBLE);

                                String sql = "SELECT filePath, latitude, longitude FROM image";
                                Cursor cursor = null;
                                cursor = database.rawQuery(sql, null);

                                String sarr1[];
                                try {
                                    sarr1 = selectedAdd.split(" ");
                                } catch (Exception e) {
                                    return;
                                }
                                if (cursor.moveToFirst()) {
                                    int num = 0;
                                    do {

                                        int latIndex = cursor.getColumnIndex("latitude");
                                        double lat = cursor.getDouble(latIndex);

                                        int longIndex = cursor.getColumnIndex("longitude");
                                        double lng = cursor.getDouble(longIndex);


                                        LatLng latLng = new LatLng(lat, lng);
                                        List<Address> addlist;
                                        try {
                                            addlist = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            return;
                                        }
                                        if (addlist.size() > 0) {
                                            Address add = addlist.get(0);
                                            StringBuilder fullAdd = new StringBuilder();
                                            for (int i = 0; i <= add.getMaxAddressLineIndex(); i++) {
                                                fullAdd.append(add.getAddressLine(i));
                                                if (i < add.getMaxAddressLineIndex()) {
                                                    fullAdd.append(", ");
                                                }
                                            }
                                            String addName = fullAdd.toString();

                                            String sarr2[];
                                            try {
                                                System.out.println(addName);
                                                sarr2 = addName.split(" ");
                                            }catch(NullPointerException e){
                                                continue;
                                            }

                                            boolean same = true;
                                            if (sarr1.length < 3 || sarr2.length < 3) {
                                                int shorter = Math.min(sarr1.length, sarr2.length);

                                                for (int i = 0; i < shorter; i++) {
                                                    if (!(sarr1[i].equals(sarr2[i]))) {
                                                        same = false;
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < 3; i++) {
                                                    if (!(sarr1[i].equals(sarr2[i]))) {
                                                        same = false;
                                                    }
                                                }
                                            }
                                            if (same == true) {
                                                int columnIndex = cursor.getColumnIndex("filepath");
                                                Bitmap bitmap = BitmapFactory.decodeFile(cursor.getString(columnIndex));
                                                imgList.add(bitmap);
                                            }
                                        }


                                    } while (cursor.moveToNext());
                                    System.out.println("num: " + num);
                                }
                                try{
                                    if (imgList.size() == 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    for (int i = 0; i < backUpList.size(); i++) {
                                        imgList.add(backUpList.get(i));
                                    }
                                    showToast();
                                }finally {
                                    currentIndex = 0;
                                    numImage = imgList.size();
                                    showImageList(currentIndex);
                                }
                            }

                        }

                    });


        }




    protected void onDiaryButtonClicked(){

        Intent intent = new Intent(this, MainActivity2.class);

        Bitmap bitmap = imgList.get(currentIndex);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("imageByte", byteArray);

        String filePath = pathList.get(currentIndex);
        intent.putExtra("filePath", filePath);

        startActivity(intent);
    }

    private void showToast(){
        Toast.makeText(this, "해당 위치에서 촬영한 사진이 없습니다.", Toast.LENGTH_SHORT).show();

    }
}