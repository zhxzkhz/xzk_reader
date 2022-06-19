package com.zhhz.reader.sql;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhhz.reader.bean.BookBean;

import java.util.ArrayList;

public class SQLiteUtil {

    private static SQLiteOpenHelper helper;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static void insertBook(BookBean book) {
        helper = new BookSqliteHelper(context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("insert into bookrack (book_id,title,author,cover,categories,latestChapter,update_time,intro) values(?,?,?,?,?,?,?,?)", new Object[]{book.getBook_id(), book.getTitle(), book.getAuthor(), book.getCover(), book.getCategories(), book.getLatestChapter(), book.isUpdate() ? 0 : 1, book.getIntro()});
        database.close();
    }

    public static ArrayList<BookBean> readBooks() {
        helper = new BookSqliteHelper(context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrack", null);
        ArrayList<BookBean> list = new ArrayList<>();
        while (query.moveToNext()) {
            BookBean bookBean = new BookBean();
            bookBean.setBook_id(query.getString(0));
            bookBean.setTitle(query.getString(1));
            bookBean.setAuthor(query.getString(2));
            bookBean.setCover(query.getString(3));
            bookBean.setCategories(query.getString(4));
            bookBean.setLatestChapter(query.getString(5));
            bookBean.setUpdate(query.getInt(6) == 0);
            bookBean.setIntro(query.getString(7));
            list.add(bookBean);
        }
        query.close();
        database.close();
        return list;
    }

    public static BookBean readBook(BookBean book) {
        helper = new BookSqliteHelper(context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrack where book_id=?", new String[]{book.getBook_id()});
        query.move(1);
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(query.getString(0));
        bookBean.setTitle(query.getString(1));
        bookBean.setAuthor(query.getString(2));
        bookBean.setCover(query.getString(3));
        bookBean.setCategories(query.getString(4));
        bookBean.setLatestChapter(query.getString(5));
        bookBean.setUpdate(query.getInt(6) == 0);
        bookBean.setIntro(query.getString(7));
        query.close();
        database.close();
        return bookBean;
    }

}
