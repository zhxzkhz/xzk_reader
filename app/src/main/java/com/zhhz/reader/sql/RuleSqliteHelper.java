package com.zhhz.reader.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class RuleSqliteHelper extends SQLiteOpenHelper {

    public RuleSqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String execSQL = "create table bookrule (id varchar(32) primary key,name varchar(60),file varchar(60),comic boolean,open boolean);";
        sqLiteDatabase.execSQL(execSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
