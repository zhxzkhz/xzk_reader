package com.zhhz.reader.ui.search;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.bean.SearchBean;
import com.zhhz.reader.databinding.FragmentSearchBinding;
import com.zhhz.reader.view.SearchEditText;

import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel mViewModel;
    private FragmentSearchBinding binding;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        mViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<SearchBean>>() {
            @Override
            public void onChanged(List<SearchBean> searchBeans) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.searchClear.setOnClickListener(view -> requireActivity().finish());
        binding.message.setOnSearchFocusListener((v, hasFocus) -> System.out.println(v));
        binding.message.changeSearchLogo(true);
        binding.message.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH  || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                mViewModel.searchBook(textView.getText().toString());
                return true;
            }
            return false;
        });

        return root;
    }

}