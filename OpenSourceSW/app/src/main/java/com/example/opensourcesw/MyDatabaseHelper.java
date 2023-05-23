package com.example.opensourcesw;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class MyDatabaseHelper extends SQLiteOpenHelper {


    public MyDatabaseHelper(Context context) {
        super(context, "opensource", null, 3);
    }


    public void onUpgrade(SQLiteDatabase sd, int a,int b){
        return;
    }

    public void onCreate(SQLiteDatabase sd){

    }
}