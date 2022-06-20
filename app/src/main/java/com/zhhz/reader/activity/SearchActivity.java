package com.zhhz.reader.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhhz.reader.R;
import com.zhhz.reader.ui.search.SearchFragment;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.search_fragment, SearchFragment.newInstance())
                    .commitNow();
        }
    }
}