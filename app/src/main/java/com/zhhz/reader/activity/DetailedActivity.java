package com.zhhz.reader.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhhz.reader.R;
import com.zhhz.reader.ui.detailed.DetailedFragment;

public class DetailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DetailedFragment.newInstance())
                    .commitNow();
        }
    }
}