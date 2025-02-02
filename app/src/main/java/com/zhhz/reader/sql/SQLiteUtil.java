package com.zhhz.reader.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.zhhz.reader.MyApplication;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.util.DiskCache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class SQLiteUtil {

    //书架数据库
    private static SQLiteOpenHelper helper1;
    //规则数据库
    private static SQLiteOpenHelper helper2;
    //设置数据库
    private static SQLiteOpenHelper helper3;

    @NonNull
    public static SQLiteOpenHelper getInstance(int i) {
        switch (i) {
            case 1 : {
                if (helper1 == null) helper1 = new BookSqliteHelper(MyApplication.context, "bookrack.db", null, 1);
                return helper1;
            }
            case 2 : {
                if (helper2 == null) helper2 = new BookSqliteHelper(MyApplication.context, "bookrule.db", null, 1);
                return helper2;
            }
            case 3 : {
                if (helper3 == null) helper3 = new BookSqliteHelper(MyApplication.context, "setting.db", null, 1);
                return helper3;
            }
            default: {
                return helper1;
            }
        }

    }

    public static void saveBook(BookBean book) {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase database = getInstance(1).getWritableDatabase();
            database.execSQL("replace into bookrack (book_id,title,author,cover,chapter_update,catalogue,latestChapter,status,update_time,intro,comic) values(?,?,?,?,?,?,?,?,?,?,?)", new Object[]{book.getBookId(), book.getTitle(), book.getAuthor(), book.getCover(), book.getUpdate(), book.getCatalogue(), book.getLastChapter(), book.getStatus() ? 1 : 0, book.getUpdateTime(), book.getIntro(), book.isComic()});
            database.close();
        }).join();
    }

    public static ArrayList<BookBean> readBooks() {
        return CompletableFuture.supplyAsync(() -> {
            
            SQLiteDatabase database = getInstance(1).getWritableDatabase();
            Cursor query = database.rawQuery("select * from bookrack", null);
            ArrayList<BookBean> list = new ArrayList<>();
            while (query.moveToNext()) {
                BookBean bookBean = new BookBean();
                bookBean.setBookId(query.getString(0));
                if (!new File(DiskCache.path + File.separator + "book" + File.separator + bookBean.getBookId() + File.separator + "chapter").isFile()) {
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
                bookBean.setComic(query.getInt(10) == 1);
                list.add(bookBean);
            }
            query.close();
            database.close();
            Collections.reverse(list);
            return list;
        }).join();
    }

    public static BookBean readBook(String id) {
        return CompletableFuture.supplyAsync(() -> {
            
            SQLiteDatabase database = getInstance(1).getWritableDatabase();
            Cursor query = database.rawQuery("select * from bookrack where book_id=?", new String[]{id});
            query.move(1);
            BookBean bookBean = new BookBean();
            bookBean.setBookId(query.getString(0));
            bookBean.setTitle(query.getString(1));
            bookBean.setAuthor(query.getString(2));
            bookBean.setCover(query.getString(3));
            bookBean.setUpdate(query.getInt(4) == 1);
            bookBean.setCatalogue(query.getString(5));
            bookBean.setLastChapter(query.getString(6));
            bookBean.setStatus(query.getInt(7) == 0);
            bookBean.setUpdateTime(query.getString(8));
            bookBean.setIntro(query.getString(9));
            bookBean.setComic(query.getInt(10) == 1);
            query.close();
            database.close();
            return bookBean;
        }).join();
    }

    public static void removeBooks(String[] ids) {
        CompletableFuture.runAsync(() -> {
            
            SQLiteDatabase database = getInstance(1).getWritableDatabase();
            for (String id : ids) {
                database.delete("bookrack", "book_id=?", new String[]{id});
            }
        }).join();
    }

    public static RuleBean readRule(String id) {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase database = getInstance(2).getWritableDatabase();
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
        }).join();
    }

    public static ArrayList<RuleBean> readRules() {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase database = getInstance(2).getWritableDatabase();
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
        }).join();
    }

    public static void saveRule(RuleBean ruleBean) {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase database = getInstance(2).getWritableDatabase();
            database.execSQL("replace into bookrule (id,name,file,comic,open) values(?,?,?,?,?)", new Object[]{ruleBean.getId(), ruleBean.getName(), ruleBean.getFile(), ruleBean.isComic(), ruleBean.isOpen()});
            database.close();
        }).join();
    }

    public static void removeRules(String[] ids) {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase database = getInstance(2).getWritableDatabase();
            for (String id : ids) {
                database.delete("bookrule", "id=?", new String[]{id});
            }
        }).join();
    }

    public static String readSetting(String name) {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase database = getInstance(3).getWritableDatabase();
            Cursor query = database.rawQuery("select * from setting where name=?", new String[]{name});
            StringBuilder s = new StringBuilder();
            if (query.moveToNext()) {
                s.append(query.getString(1));
            } else {
                s.append("{}");
            }
            query.close();
            database.close();
            return s.toString();
        }).join();
    }

    public static void SaveSetting(String name, String value) {
        CompletableFuture.runAsync(() -> {
            SQLiteDatabase database = getInstance(3).getWritableDatabase();
            database.execSQL("replace into setting (name,value) values(?,?)", new Object[]{name, value});
            database.close();
        }).join();
    }

}
