package com.example.opensourcesw;
import java.nio.charset.Charset;
import com.example.opensourcesw.R;
import java.net.URLEncoder;
import android.content.DialogInterface;
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
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.example.opensourcesw.MainActivity.*;
import androidx.appcompat.app.AlertDialog;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.Editable;

public class MainActivity2 extends AppCompatActivity {
    private ImageView imgView2;
    private EditText et, et2;
    SQLiteDatabase db = MainActivity.database;
    private String filePath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button storeButton = (Button) findViewById(R.id.button6);
        Button modifyButton = (Button) findViewById(R.id.button7);
        Button deleteButton = (Button) findViewById(R.id.button8);
        Button createTagsButton = (Button) findViewById(R.id.button9);

        et = (EditText) findViewById(R.id.editTextTextMultiLine);
        et2 = (EditText) findViewById(R.id.editTextTextMultiLine2);

        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        et2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        et.setEnabled(false);
        et2.setEnabled(false);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        imgView2 = (ImageView) findViewById(R.id.imageView2);

        byte[] byteArray = getIntent().getByteArrayExtra("imageByte");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imgView2.setImageBitmap(bitmap);

        filePath = getIntent().getStringExtra("filePath");



        try {
            Cursor cursor = db.rawQuery("SELECT diary from image where filepath = '?'", new String[]{filePath});
            String diary = cursor.getString(0);
                String str1;
                String str2;
                if (diary.length() < 100) {
                    str1 = diary.substring(0, diary.length());
                    et.setText(str1);
                } else {
                    str1 = diary.substring(0, 100);
                    str2 = diary.substring(100, diary.length());
                    et.setText(str1);
                    et.setText(str2);
                }


        }catch(Exception e){

        }


        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStoreButtonClicked();
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onModifyButtonClicked();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteButtonClicked();
            }
        });

        createTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateTagsClicked();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            Toast toastView;
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxChecked(isChecked);
            }
        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 100) {
                    et2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void onStoreButtonClicked() {
        String inputText = et.getText().toString();
        if(inputText == null){
            return;
        }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("일기를 저장하시겠습니까?(yes/no)")
                    .setCancelable(false)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                String inputText2 = et2.getText().toString();
                                String diaryText = "";

                                diaryText = inputText + inputText2;
                                db.beginTransaction();
                                String sql = "UPDATE image set diary = '" + diaryText + "' where filepath = '" + filePath + "'";
                                db.execSQL(sql);
                                db.setTransactionSuccessful();
                                db.endTransaction();

                            }catch(Exception e){
                                String diaryText = inputText;
                                db.beginTransaction();
                                String sql = "UPDATE image set diary = '" + diaryText + "' where filepath = '" + filePath + "'";
                                db.execSQL(sql);
                                db.setTransactionSuccessful();
                                db.endTransaction();

                            }
                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
    }

    private void onModifyButtonClicked() {
        Toast toastView = Toast.makeText(this, "일기 작성 및 수정 시작", Toast.LENGTH_SHORT);
        toastView.show();
        et.setEnabled(true);
        et2.setEnabled(true);

    }

    private void onDeleteButtonClicked() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("일기를 삭제하시겠습니까?(yes/no)")
                    .setCancelable(false)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            db.beginTransaction();
            String sql = "UPDATE image SET diary = null where filepath = '" + filePath + "'";
            db.execSQL(sql);
            db.setTransactionSuccessful();
            db.endTransaction();

    }

    private void onCreateTagsClicked() {

    }

    private void checkBoxChecked(boolean isChecked){
        if(isChecked){
            // 키워드 생성 기능 구현하기
        } else{
            // 일기 작성 기능 구현하기
        }
    }

}