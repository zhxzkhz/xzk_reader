package com.zhhz.reader.ui.bookrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.OnContextClickListener;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.activity.SearchActivity;
import com.zhhz.reader.adapter.BookAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.databinding.FragmentBookrackBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookRackFragment extends Fragment {

    private BookRackViewModel bookrackViewModel;
    private FragmentBookrackBinding binding;
    private BookAdapter bookAdapter;
    SelectionTracker<String> tracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bookrackViewModel = new ViewModelProvider(this).get(BookRackViewModel.class);

        binding = FragmentBookrackBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bookAdapter = new BookAdapter(BookRackFragment.this.getContext());
        bookAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.rv.setItemAnimator(new DefaultItemAnimator());
        binding.rv.setLayoutManager(new GridLayoutManager(BookRackFragment.this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        //固定高度
        binding.rv.setHasFixedSize(true);
        binding.rv.setAdapter(bookAdapter);


        //设置点击事件
        binding.searchView.setOnClickListener(view -> {
            Intent intent = new Intent(BookRackFragment.this.getContext(), SearchActivity.class);
            startActivity(intent);
            BookRackFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.refreshLayout.setOnRefreshListener(refreshLayout -> bookrackViewModel.updateCatalogue());

        binding.bookrackSetting.setOnClickListener(view -> {

        });

        bookrackViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            ArrayList<String> lists = new ArrayList<>();
            for (BookBean bookBean : list) {
                lists.add(bookBean.getBook_id());
            }

            tracker = new SelectionTracker.Builder<>(
                    "my-selection-id",
                    binding.rv,
                    new StringItemKeyProvider(1, lists),
                    new MyDetailsLookup(binding.rv),
                    StorageStrategy.createStringStorage())
                    .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                    .withOnItemActivatedListener((item, e) -> {
                        Intent intent = new Intent(BookRackFragment.this.getContext(), BookReaderActivity.class);
                        //获取点击事件位置
                        int position = item.getPosition();
                        bookAdapter.getItemData().get(position).setUpdate(false);
                        bookrackViewModel.updateBook(bookAdapter.getItemData().get(position));
                        bookAdapter.notifyItemChanged(position);
                        intent.putExtra("book", bookAdapter.getItemData().get(position));
                        startActivity(intent);
                        BookRackFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        return true;
                    })
                    .build();
            bookAdapter.setSelectionTracker(tracker);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new BookRackDiffCallback(bookAdapter.getItemData(), list));
            bookAdapter.setItemData(list);
            result.dispatchUpdatesTo(bookAdapter);
            //bookrackViewModel.updateCatalogue();
        });

        //更新目录回调
        bookrackViewModel.getCatalogue().observe(getViewLifecycleOwner(), bookBean -> {
            if (bookBean != null) {
                int pos = bookAdapter.getItemData().indexOf(bookBean);
                if (pos > -1) {
                    bookAdapter.notifyItemChanged(pos);
                }
            }
            binding.refreshLayout.finishRefresh();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(bookrackViewModel.getData().getValue()).clear();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState);
    }

    private static class BookRackDiffCallback extends DiffUtil.Callback {
        private final ArrayList<BookBean> oldData;
        private final ArrayList<BookBean> newData;

        public BookRackDiffCallback(ArrayList<BookBean> oldData, ArrayList<BookBean> newData) {
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
            return oldData.get(oldItemPosition).getBook_id().equals(newData.get(newItemPosition).getBook_id());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).getBook_id().equals(newData.get(newItemPosition).getBook_id());
        }
    }

    public static class StringItemKeyProvider extends ItemKeyProvider<String> {

        private final List<String> items;

        public StringItemKeyProvider(int scope, List<String> items) {
            super(scope);
            this.items = items;
        }

        @Nullable
        @Override
        public String getKey(int position) {
            return items.get(position);
        }

        @Override
        public int getPosition(@NonNull String key) {
            return items.indexOf(key);
        }
    }

    private static class MyDetailsLookup extends ItemDetailsLookup<String>  {

        private final RecyclerView mRecyclerView;

        public MyDetailsLookup(RecyclerView rv) {
            mRecyclerView = rv;
        }

        @Nullable
        @Override
        public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                if (holder instanceof BookAdapter.ViewHolder)
                return ((BookAdapter.ViewHolder) holder).getItemDetails();
            }
            return null;
        }
    }
}