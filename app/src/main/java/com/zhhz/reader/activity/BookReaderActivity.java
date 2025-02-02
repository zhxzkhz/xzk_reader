package com.zhhz.reader.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.zhhz.reader.R;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.ui.bookreader.BookMenuFragment;
import com.zhhz.reader.ui.bookreader.BookReaderFragment;
import com.zhhz.reader.ui.bookreader.BookReaderFragmentBase;
import com.zhhz.reader.ui.bookreader.BookReaderFragmentX;
import com.zhhz.reader.ui.bookreader.BookReaderViewModel;
import com.zhhz.reader.ui.bookreader.ComicReaderFragment;
import com.zhhz.reader.view.XReadTextView;

import java.util.Objects;

public class BookReaderActivity extends AppCompatActivity implements XReadTextView.CallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookreader);
        BookReaderViewModel mViewModel = new ViewModelProvider(this).get(BookReaderViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mViewModel.setBook(Objects.requireNonNull(getIntent().getSerializableExtra("book", BookBean.class)));
        } else {
            mViewModel.setBook((BookBean) Objects.requireNonNull(getIntent().getSerializableExtra("book")));
        }
        if (savedInstanceState == null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            //是否开启日志悬浮窗
            boolean bool = sharedPrefs.getBoolean("test_read", false);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mViewModel.isComic() ? ComicReaderFragment.newInstance() : (bool ? BookReaderFragmentX.Companion.newInstance() : BookReaderFragment.newInstance()), mViewModel.isComic() ? "ComicReaderFragment" : "BookReaderFragment")
                    .commitNow();
        }

        findViewById(R.id.container).setOnClickListener(view -> {
            if (BookMenuFragment.getInstance().isVisible()) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(BookMenuFragment.getInstance())
                        .commitNow();
            } else {
                if (BookMenuFragment.getInstance().isAdded()) {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show(BookMenuFragment.getInstance())
                            .commitNow();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .add(R.id.container, BookMenuFragment.getInstance())
                            .commitNow();
                }
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("BookReaderFragment");
        if (fragment != null) {
            boolean bool = ((BookReaderFragmentBase) fragment).dispatchKeyEvent(event);
            if (bool) return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private BookReaderFragmentX getFragmentX() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("BookReaderFragment");
        if (fragment != null) {
            return (BookReaderFragmentX) fragment;
        }
        return null;
    }

    @Override
    public int getHeaderHeight() {
        return Objects.requireNonNull(getFragmentX()).getHeaderHeight();
    }

    @Override
    public void updateSelectedStart(float x, float y, float top) {
        Objects.requireNonNull(getFragmentX()).updateSelectedStart(x, y, top);
    }

    @Override
    public void upSelectedEnd(float x, float y) {
        Objects.requireNonNull(getFragmentX()).upSelectedEnd(x, y);
    }

    @Override
    public void showTextActionMenu() {
        Objects.requireNonNull(getFragmentX()).showTextActionMenu();
    }

    @Override
    public void showBookMenu() {
        findViewById(R.id.container).callOnClick();
    }

    @Override
    public void onCancelSelect() {
        Objects.requireNonNull(getFragmentX()).onCancelSelect();
    }

    @Override
    public void saveProgress() {
        Objects.requireNonNull(getFragmentX()).saveProgress();
    }

    @Override
    public void switchChapter(int i) {
        Objects.requireNonNull(getFragmentX()).switchChapter(i);
    }
}