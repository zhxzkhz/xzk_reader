package com.zhhz.reader.ui.bookreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.databinding.FragmentBookMenuBinding;

import java.util.LinkedHashMap;
import java.util.Objects;

public class BookMenuFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentBookMenuBinding binding;

    private static BookMenuFragment fragment;

    public static BookMenuFragment getInstance() {
        if (fragment!=null) return fragment;
        return fragment = new BookMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookMenuBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        binding.menuTitle.setText(mViewModel.getBook().getTitle());
        binding.menuBack.setOnClickListener((view)->requireActivity().finish());
        binding.menuTitle.setClickable(true);
        binding.menuMore.setClickable(true);
        binding.menuNextPage.setClickable(true);
        binding.menuSource.setClickable(true);
        binding.menuPreviousPage.setClickable(true);
        binding.menuCatalogue.setOnClickListener(view -> {});


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getChapters().observe(getViewLifecycleOwner(), s -> binding.menuSource.setText(Objects.requireNonNull(mViewModel.getDataCatalogue().getValue()).get(s)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragment = null;
    }
}