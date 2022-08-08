package com.zhhz.reader.ui.bookreader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhhz.reader.adapter.CatalogueAdapter;
import com.zhhz.reader.databinding.FragmentBookMenuBinding;
import com.zhhz.reader.view.RecycleViewDivider;

import java.util.Objects;

public class BookMenuFragment extends Fragment {

    private static BookMenuFragment fragment;
    private BookReaderViewModel mViewModel;
    private FragmentBookMenuBinding binding;
    private CatalogueAdapter catalogueAdapter;

    public static BookMenuFragment getInstance() {
        if (fragment != null) return fragment;
        return fragment = new BookMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookMenuBinding.inflate(inflater, container, false);
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

        binding.menuBack.setOnClickListener((view) -> requireActivity().finish());
        binding.menuTitle.setClickable(true);
        binding.menuMore.setClickable(true);
        binding.menuNextPage.setClickable(true);
        binding.menuSource.setClickable(true);
        binding.menuPreviousPage.setClickable(true);
        binding.menuCatalogue.setOnClickListener(view -> {
            if (binding.menuCatalogueList.getVisibility() == View.GONE) {
                binding.menuCatalogueList.setVisibility(View.VISIBLE);
            } else {
                binding.menuCatalogueList.setVisibility(View.GONE);
            }
        });

        catalogueAdapter.setOnClickListener(v -> {
            mViewModel.jumpChapters(binding.menuCatalogueList.getChildAdapterPosition(v));
            binding.menuHide.callOnClick();
            binding.menuCatalogue.callOnClick();
        });

        binding.menuNextPage.setOnClickListener(view -> {
            int progress = mViewModel.current_progress_page(mViewModel.getStart())[0];
            if (mViewModel.isHaveNextChapters(progress)) {
                mViewModel.jumpChapters(progress + 1);
            } else {
                Toast.makeText(requireContext(), "已经没有下一章了", Toast.LENGTH_SHORT).show();
            }
        });

        binding.menuPreviousPage.setOnClickListener(view -> {
            int progress = mViewModel.current_progress_page(mViewModel.getStart())[0];
            if (mViewModel.isHavePreviousChapters(progress)) {
                mViewModel.jumpChapters(progress - 1);
            } else {
                Toast.makeText(requireContext(), "已经是第一章了", Toast.LENGTH_SHORT).show();
            }
        });

        binding.menuHide.setOnClickListener(view -> getParentFragmentManager().beginTransaction().hide(BookMenuFragment.this).commitNow());
        binding.menuCache.setOnClickListener(view -> mViewModel.cacheBook(-1,true));
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
            //binding.menuSource.setText(Objects.requireNonNull(mViewModel.getDataCatalogue().getValue()).get(s));
            binding.menuSource.setText(s);
            int pos = catalogueAdapter.getPos();
            catalogueAdapter.setPos(mViewModel.getProgress());
            Objects.requireNonNull((LinearLayoutManager) binding.menuCatalogueList.getLayoutManager()).scrollToPositionWithOffset(mViewModel.getProgress(), (int) (height * 0.4f));
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