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
import android.content.ClipboardManager;
import android.content.ClipData;

public class MainActivity2 extends AppCompatActivity {
    private ImageView imgView2;
    private EditText et, et2;
    SQLiteDatabase db = MainActivity.database;
    private String filePath;
    Button storeButton;
    Button modifyButton;
    Button deleteButton;
    Button createTagsButton;
    String selectedWord;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        storeButton = (Button) findViewById(R.id.button6);
        modifyButton = (Button) findViewById(R.id.button7);
        deleteButton = (Button) findViewById(R.id.button8);
        createTagsButton = (Button) findViewById(R.id.button9);

        createTagsButton.setEnabled(false);

        et = (EditText) findViewById(R.id.editTextTextMultiLine);
        et2 = (EditText) findViewById(R.id.editTextTextMultiLine2);

        filePath = getIntent().getStringExtra("filePath");

        Cursor cursor = null;
        cursor = db.rawQuery("SELECT diary from image where filePath = ?", new String[]{filePath});
        String diary;
        if (cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex("diary");
            diary = cursor.getString(columnIndex);

        }
        else{
            diary = "";
        }

        try {
            if (diary.length() < 100) {
                et.setText(diary);
            } else {
                String str = diary.substring(0, 100);
                et.setText(str);
                String str2 = diary.substring(100, diary.length());
                et2.setText(str2);
            }
        }catch(Exception e){

        }

        et.setEnabled(false);
        et2.setEnabled(false);

        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        et2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        imgView2 = (ImageView) findViewById(R.id.imageView2);

        byte[] byteArray = getIntent().getByteArrayExtra("imageByte");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imgView2.setImageBitmap(bitmap);



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


    et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

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
                            String diaryText = "";
                            try {
                                String inputText2 = et2.getText().toString();

                                diaryText = inputText + inputText2;
                                db.beginTransaction();
                                String sql = "UPDATE image set diary = '" + diaryText + "' where filepath = '" + filePath + "'";
                                db.execSQL(sql);
                                db.setTransactionSuccessful();
                                db.endTransaction();

                            }catch(Exception e){
                                diaryText = inputText;
                                db.beginTransaction();
                                String sql = "UPDATE image set diary = '" + diaryText + "' where filepath = '" + filePath +"'";
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
                            db.beginTransaction();
                            String sql = "UPDATE image SET diary = '' where filePath = '" + filePath + "'";
                            db.execSQL(sql);
                            db.setTransactionSuccessful();
                            db.endTransaction();
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

    private void onCreateTagsClicked() {
        try {
            if(selectedWord == null){
                throw new Exception();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String message = selectedWord + " 키워드를 태그로 추가하겠습니까?(yes/no)";

            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.beginTransaction();
                            String sql = "UPDATE image SET tag = COALESCE(tag, '') || '" + selectedWord + "' WHERE filePath = '" + filePath + "'";
                            db.execSQL(sql);
                            db.setTransactionSuccessful();
                            db.endTransaction();
                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();


        }catch(Exception e){
            Toast toastView = Toast.makeText(this, "키워드를 선택하지 않았습니다.", Toast.LENGTH_SHORT);
            toastView.show();
            e.printStackTrace();
            return;
        }
    }

    private void checkBoxChecked(boolean isChecked){
        if(isChecked){ // 키워드 선택 기능
            Toast toastView = Toast.makeText(this, "일기에서 키워드를 선택하여 복사하고 create tags를 클릭하세요.", Toast.LENGTH_SHORT);
            toastView.show();

            et.setEnabled(true);
            et2.setEnabled(true);

            storeButton.setEnabled(false);
            modifyButton.setEnabled(false);
            deleteButton.setEnabled(false);
            createTagsButton.setEnabled(true);

            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    if(clipData != null && clipData.getItemCount() > 0) {
                        selectedWord = clipData.getItemAt(0).getText().toString();
                    }
                }
            });


        } else{ // 일기 작성 기능
            et.setEnabled(false);
            et2.setEnabled(false);

            storeButton.setEnabled(true);
            modifyButton.setEnabled(true);
            deleteButton.setEnabled(true);
            createTagsButton.setEnabled(false);
        }
    }

}