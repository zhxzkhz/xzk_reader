package com.zhhz.reader.ui.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhhz.reader.activity.DetailedActivity;
import com.zhhz.reader.adapter.SearchResultAdapter;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.databinding.FragmentSearchResultBinding;
import com.zhhz.reader.view.RecycleViewDivider;

import java.util.ArrayList;

public class SearchResultFragment extends Fragment {

    private static SearchResultFragment searchResultFragment;
    private FragmentSearchResultBinding binding;
    private SearchResultAdapter searchResultAdapter;

    public static SearchResultFragment getInstance() {
        if (searchResultFragment != null) return searchResultFragment;
        return searchResultFragment = new SearchResultFragment();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchViewModel mViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        mViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            binding.progress.setVisibility(View.GONE);
            int size = searchResultAdapter.getItemData().size();
            if (list == null) {
                searchResultAdapter.getItemData().clear();
                searchResultAdapter.notifyDataSetChanged();
                //失败也会显示，等后续优化
                binding.progress.setVisibility(View.VISIBLE);
            } else if (size == 0) {
                searchResultAdapter.setItemData(list);
                searchResultAdapter.notifyDataSetChanged();
            } else {
                searchResultAdapter.getItemData().addAll(list);
                searchResultAdapter.notifyItemRangeInserted(size, list.size());
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        searchResultAdapter = new SearchResultAdapter(getContext());
        searchResultAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.searchResult.setItemAnimator(new DefaultItemAnimator());
        binding.searchResult.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.searchResult.setHasFixedSize(true);
        binding.searchResult.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.searchResult.setAdapter(searchResultAdapter);

        searchResultAdapter.setOnClickListener(view -> {
            Intent intent = new Intent(SearchResultFragment.this.getContext(), DetailedActivity.class);
            //获取点击事件位置
            int position = binding.searchResult.getChildAdapterPosition(view);
            intent.putExtra("book", searchResultAdapter.getItemData().get(position));
            startActivity(intent);
            SearchResultFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchResultFragment = null;
    }


    private static class SearchResultDiffCallback extends DiffUtil.Callback {
        private final ArrayList<SearchResultBean> oldData;
        private final ArrayList<SearchResultBean> newData;

        public SearchResultDiffCallback(ArrayList<SearchResultBean> oldData, ArrayList<SearchResultBean> newData) {
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public int getOldListSize() {
            return oldData.size();
        }

        @Override
        public int getNewListSize() {
            return newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).getTitle().equals(newData.get(newItemPosition).getTitle());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).toString().equals(newData.get(newItemPosition).toString());
        }
    }
}