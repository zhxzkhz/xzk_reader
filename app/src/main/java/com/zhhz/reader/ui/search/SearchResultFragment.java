package com.zhhz.reader.ui.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.zhhz.reader.activity.DetailedActivity;
import com.zhhz.reader.adapter.SearchResultAdapter;
import com.zhhz.reader.databinding.FragmentSearchResultBinding;
import com.zhhz.reader.view.RecycleViewDivider;

public class SearchResultFragment extends Fragment {

    private static SearchResultFragment searchResultFragment;
    private FragmentSearchResultBinding binding;
    private SearchViewModel mViewModel;
    private SearchResultAdapter searchResultAdapter;

    private ActivityResultLauncher<Intent> launcher;

    public static SearchResultFragment getInstance() {
        if (searchResultFragment != null) return searchResultFragment;
        return searchResultFragment = new SearchResultFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {});
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //监听搜索完成
        mViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            binding.progress.setVisibility(View.GONE);
            binding.refreshLayout.finishLoadMore();

            if (list == null) {
                searchResultAdapter.getItemData().clear();
                searchResultAdapter.notifyDataSetChanged();
                //失败也会显示，等后续优化
                //binding.progress.setVisibility(View.VISIBLE);
            } else {
                searchResultAdapter.getItemData().addAll(list);
                searchResultAdapter.notifyItemRangeInserted(searchResultAdapter.getItemData().size(), list.size());
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);

        //禁用下拉刷新，上拉加载
        binding.refreshLayout.setEnableRefresh(false);
        binding.refreshLayout.setEnableLoadMore(true);
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));//设置Footer
        binding.refreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        //加载下一页
        binding.refreshLayout.setOnLoadMoreListener(refreshLayout -> mViewModel.nextPage());

        searchResultAdapter = new SearchResultAdapter(requireContext());
        searchResultAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.searchResult.setItemAnimator(new DefaultItemAnimator());
        binding.searchResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        //固定高度
        binding.searchResult.setHasFixedSize(true);
        binding.searchResult.addItemDecoration(new RecycleViewDivider(requireContext(), 1));
        binding.searchResult.setAdapter(searchResultAdapter);

        searchResultAdapter.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), DetailedActivity.class);
            //获取点击事件位置
            int position = binding.searchResult.getChildAdapterPosition(view);
            intent.putExtra("book", searchResultAdapter.getItemData().get(position));
            //startActivity(intent);
            launcher.launch(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchResultFragment = null;
    }


}