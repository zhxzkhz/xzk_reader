package com.zhhz.reader.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.R;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.ui.bookreader.BookMenuFragment;
import com.zhhz.reader.ui.bookreader.BookReaderFragment;
import com.zhhz.reader.ui.bookreader.BookReaderViewModel;
import com.zhhz.reader.ui.bookreader.ComicReaderFragment;

public class BookReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookreader);
        BookReaderViewModel mViewModel = new ViewModelProvider(this).get(BookReaderViewModel.class);
        mViewModel.setBook((BookBean) getIntent().getSerializableExtra("book"));
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mViewModel.isComic() ? ComicReaderFragment.newInstance() : BookReaderFragment.newInstance(),mViewModel.isComic()?"ComicReaderFragment":"BookReaderFragment")
                    .commitNow();
        }

        findViewById(R.id.container).setOnClickListener(view -> {
            if (BookMenuFragment.getInstance().isVisible()) {
                getSupportFragmentManager().beginTransaction().hide(BookMenuFragment.getInstance()).commitNow();
            } else {
                if (BookMenuFragment.getInstance().isAdded()) {
                    getSupportFragmentManager().beginTransaction().show(BookMenuFragment.getInstance()).commitNow();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, BookMenuFragment.getInstance())
                            .commitNow();
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("BookReaderFragment");
        if (fragment!=null){
            boolean bool = ((BookReaderFragment)fragment).onKeyDown(keyCode, event);
            if (bool) return true;
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}