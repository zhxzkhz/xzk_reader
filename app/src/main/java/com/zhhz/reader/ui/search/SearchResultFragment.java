package com.zhhz.reader.ui.search;

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
import androidx.recyclerview.widget.GridLayoutManager;

import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.activity.DetailedActivity;
import com.zhhz.reader.adapter.SearchResultAdapter;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.databinding.FragmentSearchResultBinding;

import java.util.ArrayList;

public class SearchResultFragment extends Fragment {

    private FragmentSearchResultBinding binding;
    private SearchResultAdapter searchResultAdapter;
    private static SearchResultFragment searchResultFragment;

    public static SearchResultFragment getInstance() {
        if (searchResultFragment!=null) return searchResultFragment;
        return searchResultFragment = new SearchResultFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchViewModel mViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        mViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            searchResultAdapter.setItemData(list);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new SearchResultDiffCallback(searchResultAdapter.getItemData(), list));
            result.dispatchUpdatesTo(searchResultAdapter);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        searchResultAdapter  = new SearchResultAdapter(SearchResultFragment.this.getContext());
        searchResultAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.searchResult.setItemAnimator(new DefaultItemAnimator());
        binding.searchResult.setLayoutManager(new GridLayoutManager(SearchResultFragment.this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        //固定高度
        binding.searchResult.setHasFixedSize(true);

        searchResultAdapter.setOnClickListener(view -> {
            Intent intent = new Intent(SearchResultFragment.this.getContext(), DetailedActivity.class);
            Bundle bundle = new Bundle();
            //获取点击事件位置
            int position = binding.searchResult.getChildAdapterPosition(view);
            bundle.putSerializable("book",searchResultAdapter.getItemData().get(position));
            intent.putExtras(bundle);
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

    private static class SearchResultDiffCallback extends DiffUtil.Callback{
        private final ArrayList<SearchResultBean> oldData;
        private final ArrayList<SearchResultBean> newData;
        public SearchResultDiffCallback(ArrayList<SearchResultBean> oldData,ArrayList<SearchResultBean> newData) {
            this.oldData=oldData;
            this.newData=newData;
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