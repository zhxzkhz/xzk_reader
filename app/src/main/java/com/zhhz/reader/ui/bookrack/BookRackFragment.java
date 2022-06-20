package com.zhhz.reader.ui.bookrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zhhz.reader.adapter.BookAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.databinding.FragmentBookrackBinding;

import java.util.ArrayList;
import java.util.Objects;

public class BookRackFragment extends Fragment {

    private BookRackViewModel bookrackViewModel;
    private FragmentBookrackBinding binding;
    private BookAdapter bookAdapter;
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

        bookrackViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            bookAdapter.setItemData(list);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new BookRackDiffCallback(bookAdapter.getItemData(), list));
            result.dispatchUpdatesTo(bookAdapter);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class BookRackDiffCallback extends DiffUtil.Callback{
        private final ArrayList<BookBean> oldData;
        private final ArrayList<BookBean> newData;
        public BookRackDiffCallback(ArrayList<BookBean> oldData,ArrayList<BookBean> newData) {
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
            return oldData.get(oldItemPosition).getBook_id().equals(newData.get(newItemPosition).getBook_id());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).getBook_id().equals(newData.get(newItemPosition).getBook_id());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(bookrackViewModel.getData().getValue()).clear();
    }
}