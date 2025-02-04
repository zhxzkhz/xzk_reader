package com.zhhz.reader.activity;

import android.content.Context;
import android.content.Intent;
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
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.databinding.ActivitySearchBinding;
import com.zhhz.reader.rule.Analysis;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.ui.search.SearchResultFragment;
import com.zhhz.reader.ui.search.SearchViewModel;
import com.zhhz.reader.util.ToastUtil;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(this));

        setContentView(binding.getRoot());


        SearchViewModel mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        binding.searchClear.setOnClickListener(view -> finish());
        binding.searchText.setOnSearchFocusListener((v, hasFocus) -> {
        });
        binding.searchText.changeSearchLogo(true);
        binding.searchText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                return true;
            }

            if (RuleAnalysis.analyses_map.isEmpty()) {
                ToastUtil.show("请设置书源");
                return true;
            }

            if (!textView.getText().toString().startsWith("http")) {
                //没有搜索源则为true，给予提示
                boolean bool = true;
                for (Analysis value : RuleAnalysis.analyses_map.values()) {
                    if (value.isHaveSearch()) {
                        bool = false;
                        break;
                    }
                }
                if (bool) {
                    ToastUtil.show("请设置有搜索的书源");
                }
            }

            if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                binding.searchText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                SearchResultBean bean = mViewModel.isUrl(textView.getText().toString());

                if (bean != null) {
                    Intent intent = new Intent(SearchActivity.this, DetailedActivity.class);
                    intent.putExtra("book", bean);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    if (mViewModel.getData().getValue() != null) {
                        //搜索前清除上次搜索记录
                        mViewModel.getData().setValue(null);
                    }
                    //每次搜索设置页数为1
                    mViewModel.setPage(1);
                    mViewModel.searchBook(textView.getText().toString());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.search_fragment, SearchResultFragment.getInstance(), "SearchResultFragment")
                            .commitNow();
                }

                return true;
            }
            return false;
        });
        binding.searchText.setFocusable(true);
        binding.searchText.setFocusableInTouchMode(true);
        binding.searchText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        binding.searchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}