package com.zhhz.reader.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.databinding.FragmentSearchBinding;

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
        mViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

}