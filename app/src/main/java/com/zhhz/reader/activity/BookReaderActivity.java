package com.zhhz.reader.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.zhhz.reader.R;
import com.zhhz.reader.bean.BookBean;

public class BookReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_reader);
        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        BookBean book=(BookBean)bundle.getSerializable("book");
        ((AppCompatTextView) findViewById(R.id.text_reader)).setText(book.toString());
    }
}