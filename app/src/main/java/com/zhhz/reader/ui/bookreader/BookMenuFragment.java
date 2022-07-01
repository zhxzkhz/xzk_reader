package com.zhhz.reader.ui.bookreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhhz.reader.adapter.CatalogueAdapter;
import com.zhhz.reader.databinding.FragmentBookMenuBinding;
import com.zhhz.reader.view.RecycleViewDivider;

import java.util.Objects;

public class BookMenuFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentBookMenuBinding binding;

    private CatalogueAdapter catalogueAdapter;

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

        catalogueAdapter = new CatalogueAdapter();
        catalogueAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.menuCatalogueList.setItemAnimator(null);
        binding.menuCatalogueList.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.menuCatalogueList.setHasFixedSize(true);
        binding.menuCatalogueList.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.menuCatalogueList.setAdapter(catalogueAdapter);

        binding.menuBack.setOnClickListener((view)->requireActivity().finish());
        binding.menuTitle.setClickable(true);
        binding.menuMore.setClickable(true);
        binding.menuNextPage.setClickable(true);
        binding.menuSource.setClickable(true);
        binding.menuPreviousPage.setClickable(true);
        binding.menuCatalogue.setOnClickListener(view -> {
            if (binding.menuCatalogueList.getVisibility() == View.GONE) {
                binding.menuCatalogueList.setVisibility(View.VISIBLE);
            }else {
                binding.menuCatalogueList.setVisibility(View.GONE);
            }
        });

        catalogueAdapter.setOnClickListener(v -> {
            mViewModel.jumpChapters(binding.menuCatalogueList.getChildAdapterPosition(v));

        });

        binding.menuHide.setOnClickListener(view -> getParentFragmentManager().beginTransaction().hide(BookMenuFragment.this).commitNow());
        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int height = requireActivity().getResources().getDisplayMetrics().heightPixels;

        mViewModel.getDataCatalogue().observe(getViewLifecycleOwner(), map -> {
            catalogueAdapter.setItemData(map);
            catalogueAdapter.notifyDataSetChanged();
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), s -> {
            binding.menuSource.setText(Objects.requireNonNull(mViewModel.getDataCatalogue().getValue()).get(s));
            int pos = catalogueAdapter.getPos();
            catalogueAdapter.setPos(mViewModel.getProgress());
            Objects.requireNonNull((LinearLayoutManager)binding.menuCatalogueList.getLayoutManager()).scrollToPositionWithOffset(mViewModel.getProgress(),(int)(height * 0.4f));
            catalogueAdapter.notifyItemChanged(pos);
            catalogueAdapter.notifyItemChanged(mViewModel.getProgress());
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragment = null;
    }
}