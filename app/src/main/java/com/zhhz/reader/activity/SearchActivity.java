package com.zhhz.reader.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.R;
import com.zhhz.reader.databinding.ActivitySearchBinding;
import com.zhhz.reader.ui.search.SearchFragment;
import com.zhhz.reader.ui.search.SearchResultFragment;
import com.zhhz.reader.ui.search.SearchViewModel;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.search_fragment, SearchFragment.newInstance())
                    .commitNow();
        }

        SearchViewModel mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        binding.searchClear.setOnClickListener(view -> finish());
        binding.message.setOnSearchFocusListener((v, hasFocus) -> System.out.println(v));
        binding.message.changeSearchLogo(true);
        binding.message.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH  || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                mViewModel.searchBook(textView.getText().toString());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.search_fragment, SearchResultFragment.getInstance(),"SearchResultFragment")
                        .commitNow();
                return true;
            }
            return false;
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}