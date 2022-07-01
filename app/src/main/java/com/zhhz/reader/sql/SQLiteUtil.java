package com.zhhz.reader.sql;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.util.DiskCache;

import java.io.File;
import java.util.ArrayList;

public class SQLiteUtil {

    private static SQLiteOpenHelper helper;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static void saveBook(BookBean book) {
        helper = new BookSqliteHelper(context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("replace into bookrack (book_id,title,author,cover,categories,catalogue,latestChapter,status,update_time,intro) values(?,?,?,?,?,?,?,?,?,?)", new Object[]{book.getBook_id(), book.getTitle(), book.getAuthor(), book.getCover(), book.getCategories(),book.getCatalogue(),book.getLatestChapter(), book.isStatus() ? 1 : 0, book.getUpdate_time(), book.getIntro()});
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
            if (!new File(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBook_id() + File.separator + "chapter").isFile()){
                continue;
            }
            bookBean.setTitle(query.getString(1));
            bookBean.setAuthor(query.getString(2));
            bookBean.setCover(query.getString(3));
            bookBean.setCategories(query.getString(4));
            bookBean.setCatalogue(query.getString(5));
            bookBean.setLatestChapter(query.getString(6));
            bookBean.setStatus(query.getInt(7) == 1);
            bookBean.setUpdateTime(query.getString(8));
            bookBean.setIntro(query.getString(9));
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
        bookBean.setCatalogue(query.getString(5));
        bookBean.setLatestChapter(query.getString(6));
        bookBean.setStatus(query.getInt(7) == 0);
        bookBean.setUpdateTime(query.getString(8));
        bookBean.setIntro(query.getString(9));
        query.close();
        database.close();
        return bookBean;
    }



}
