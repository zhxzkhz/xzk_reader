package com.zhhz.reader.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zhhz.reader.R;
import com.zhhz.reader.ui.bookreader.BookReaderFragment;

public class BookReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookreader);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BookReaderFragment.newInstance())
                    .commitNow();
        }
    }
}