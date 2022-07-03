package com.zhhz.reader.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.R;
import com.zhhz.reader.databinding.ActivitySearchBinding;
import com.zhhz.reader.rule.RuleAnalysis;
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
/*            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.search_fragment, SearchFragment.newInstance())
                    .commitNow();*/
        }

        SearchViewModel mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        binding.searchClear.setOnClickListener(view -> finish());
        binding.searchText.setOnSearchFocusListener((v, hasFocus) -> System.out.println(v));
        binding.searchText.changeSearchLogo(true);
        binding.searchText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (RuleAnalysis.analyses_map.size() == 0) {
                Toast.makeText(this, "请设置书源", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                binding.searchText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                if (mViewModel.getData().getValue() != null)
                    mViewModel.getData().getValue().clear();
                mViewModel.searchBook(textView.getText().toString());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.search_fragment, SearchResultFragment.getInstance(), "SearchResultFragment")
                        .commitNow();
                return true;
            }
            return false;
        });
        binding.searchText.setFocusable(true);
        binding.searchText.setFocusableInTouchMode(true);
        binding.searchText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}