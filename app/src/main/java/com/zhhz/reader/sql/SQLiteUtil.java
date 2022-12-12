package com.zhhz.reader.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhhz.reader.MyApplication;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.util.DiskCache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SQLiteUtil {

    private static SQLiteOpenHelper helper;

    public static void saveBook(BookBean book) {
        helper = new BookSqliteHelper(MyApplication.context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("replace into bookrack (book_id,title,author,cover,chapter_update,catalogue,latestChapter,status,update_time,intro) values(?,?,?,?,?,?,?,?,?,?)", new Object[]{book.getBook_id(), book.getTitle(), book.getAuthor(), book.getCover(), book.getUpdate(), book.getCatalogue(), book.getLastChapter(), book.getStatus() ? 1 : 0, book.getUpdateTime(), book.getIntro()});
        database.close();
    }

    public static ArrayList<BookBean> readBooks() {
        helper = new BookSqliteHelper(MyApplication.context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrack", null);
        ArrayList<BookBean> list = new ArrayList<>();
        while (query.moveToNext()) {
            BookBean bookBean = new BookBean();
            bookBean.setBook_id(query.getString(0));
            if (!new File(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBook_id() + File.separator + "chapter").isFile()) {
                continue;
            }
            bookBean.setTitle(query.getString(1));
            bookBean.setAuthor(query.getString(2));
            bookBean.setCover(query.getString(3));
            bookBean.setUpdate(query.getInt(4) == 1);
            bookBean.setCatalogue(query.getString(5));
            bookBean.setLastChapter(query.getString(6));
            bookBean.setStatus(query.getInt(7) == 1);
            bookBean.setUpdateTime(query.getString(8));
            bookBean.setIntro(query.getString(9));
            list.add(bookBean);
        }
        query.close();
        database.close();
        Collections.reverse(list);
        return list;
    }

    public static BookBean readBook(String id) {
        helper = new BookSqliteHelper(MyApplication.context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrack where book_id=?", new String[]{id});
        query.move(1);
        BookBean bookBean = new BookBean();
        bookBean.setBook_id(query.getString(0));
        bookBean.setTitle(query.getString(1));
        bookBean.setAuthor(query.getString(2));
        bookBean.setCover(query.getString(3));
        bookBean.setUpdate(query.getInt(4) == 1);
        bookBean.setCatalogue(query.getString(5));
        bookBean.setLastChapter(query.getString(6));
        bookBean.setStatus(query.getInt(7) == 0);
        bookBean.setUpdateTime(query.getString(8));
        bookBean.setIntro(query.getString(9));
        query.close();
        database.close();
        return bookBean;
    }

    public static void removeBooks(String[] ids) {
        helper = new BookSqliteHelper(MyApplication.context, "bookrack.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        for (String id : ids) {
            database.delete("bookrack", "book_id=?", new String[]{id});
        }
    }

    public static RuleBean readRule(String id) {
        helper = new RuleSqliteHelper(MyApplication.context, "bookrule.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrule where id=?", new String[]{id});
        query.move(1);
        RuleBean ruleBean = new RuleBean();
        ruleBean.setId(query.getString(0));
        ruleBean.setName(query.getString(1));
        ruleBean.setFile(query.getString(2));
        ruleBean.setComic(query.getInt(3) == 1);
        ruleBean.setOpen(query.getInt(4) == 1);
        query.close();
        database.close();
        return ruleBean;
    }

    public static ArrayList<RuleBean> readRules() {
        helper = new RuleSqliteHelper(MyApplication.context, "bookrule.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor query = database.rawQuery("select * from bookrule", null);
        ArrayList<RuleBean> list = new ArrayList<>();
        while (query.moveToNext()) {
            if (!new File(query.getString(2)).isFile()) {
                continue;
            }
            RuleBean ruleBean = new RuleBean();
            ruleBean.setId(query.getString(0));
            ruleBean.setName(query.getString(1));
            ruleBean.setFile(query.getString(2));
            ruleBean.setComic(query.getInt(3) == 1);
            ruleBean.setOpen(query.getInt(4) == 1);
            list.add(ruleBean);
        }
        query.close();
        database.close();

        //新增规则排序
        list.sort((o1, o2) -> {
            if (o1.isOpen() == o2.isOpen()) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            } else if (o1.isOpen()) {
                return -1;
            } else if (o2.isOpen()) {
                return 1;
            }
            return 0;
        });

        return list;
    }

    public static void saveRule(RuleBean ruleBean) {
        helper = new RuleSqliteHelper(MyApplication.context, "bookrule.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("replace into bookrule (id,name,file,comic,open) values(?,?,?,?,?)", new Object[]{ruleBean.getId(), ruleBean.getName(), ruleBean.getFile(), ruleBean.isComic(), ruleBean.isOpen()});
        database.close();
    }

    public static void removeRules(String[] ids) {
        helper = new BookSqliteHelper(MyApplication.context, "bookrule.db", null, 1);
        SQLiteDatabase database = helper.getWritableDatabase();
        for (String id : ids) {
            database.delete("bookrule", "id=?", new String[]{id});
        }
    }

}
