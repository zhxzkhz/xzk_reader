package com.zhhz.reader.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BookSqliteHelper extends SQLiteOpenHelper {

    public BookSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String execSQL = "create table bookrack (book_id varchar(32) primary key,title varchar(60),author varchar(60),cover varchar(60),categories varchar(30),latestChapter varchar(120),update_time integer,intro varchar(600))";
        sqLiteDatabase.execSQL(execSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
