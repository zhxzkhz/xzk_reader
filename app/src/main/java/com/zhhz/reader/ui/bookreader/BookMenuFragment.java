package com.zhhz.reader.ui.bookreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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


        binding.menuTitle.setClickable(true);

        binding.menuBack.setOnClickListener((view) -> requireActivity().finish());

        if (mViewModel.isComic()){
            binding.menuRefresh.setVisibility(View.GONE);
        } else {
            binding.menuRefresh.setOnClickListener(view -> mViewModel.clearCurrentCache());
        }

        binding.menuSetting.setOnClickListener(v -> SettingDialogFragment.getInstance().show(requireActivity().getSupportFragmentManager(), "SettingDialogFragment"));

        binding.menuCatalogue.setOnClickListener(view -> {
            if (binding.menuCatalogueList.getVisibility() == View.GONE) {
                binding.menuCatalogueList.setVisibility(View.VISIBLE);
            } else {
                binding.menuCatalogueList.setVisibility(View.GONE);
            }
        });


        catalogueAdapter.setOnClickListener(v -> {
            if (mViewModel.isLoading()) {
                Toast.makeText(requireContext(), "章节加载中", Toast.LENGTH_SHORT).show();
                return;
            }
            mViewModel.jumpChapters(binding.menuCatalogueList.getChildAdapterPosition(v));
            binding.menuHide.callOnClick();
            binding.menuCatalogue.callOnClick();
        });

        binding.menuNextPage.setOnClickListener(view -> {
            int progress = mViewModel.isComic() ? mViewModel.current_progress_page(mViewModel.getStart())[0] : mViewModel.getProgress();
            if (mViewModel.isHaveNextChapters(progress)) {
                mViewModel.jumpChapters(progress + 1);
            } else {
                Toast.makeText(requireContext(), "已经没有下一章了", Toast.LENGTH_SHORT).show();
            }
        });

        binding.menuPreviousPage.setOnClickListener(view -> {
            int progress = mViewModel.isComic() ? mViewModel.current_progress_page(mViewModel.getStart())[0] : mViewModel.getProgress();
            if (mViewModel.isHavePreviousChapters(progress)) {
                mViewModel.jumpChapters(progress - 1);
            } else {
                Toast.makeText(requireContext(), "已经是第一章了", Toast.LENGTH_SHORT).show();
            }
        });

        binding.menuHide.setOnClickListener(view -> getParentFragmentManager().beginTransaction().hide(BookMenuFragment.this).commitNow());

        if (mViewModel.isLocalBooks()) {
            binding.menuCache.setClickable(false);
        } else {
            binding.menuCache.setOnClickListener(view -> mViewModel.cacheBook(-1, true));
        }
        binding.menuSource.setOnClickListener(view -> {
            String url = Objects.requireNonNull(mViewModel.getDataCatalogue().getValue()).get(binding.menuSource.getText().toString());
            if (url != null && url.startsWith("http")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //请注意下面的选择器。如果没有匹配的应用程序，
                // Android会显示系统消息。因此无需尝试捕获。
                startActivity(Intent.createChooser(intent, "请选择浏览器"));
            }
        });

        return root;
    }

    //显示菜单时更新标题和目录章节位置
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //显示时更新标题
            int tmp_progress = mViewModel.isComic() ? mViewModel.current_progress_page(mViewModel.getStart())[0] : mViewModel.getProgress();
            int pos = catalogueAdapter.getPos();
            catalogueAdapter.setPos(tmp_progress);
            int height = requireActivity().getResources().getDisplayMetrics().heightPixels;
            Objects.requireNonNull((LinearLayoutManager) binding.menuCatalogueList.getLayoutManager()).scrollToPositionWithOffset(tmp_progress, (int) (height * 0.4f));
            catalogueAdapter.notifyItemChanged(pos);
            catalogueAdapter.notifyItemChanged(tmp_progress);
            binding.menuSource.setText(mViewModel.getCatalogue().get(tmp_progress));
        }
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

        mViewModel.getChapters().observe(getViewLifecycleOwner(), s -> onHiddenChanged(false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragment = null;
    }
}