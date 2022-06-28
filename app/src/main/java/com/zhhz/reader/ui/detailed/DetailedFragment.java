package com.zhhz.reader.ui.detailed;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.adapter.CatalogueAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.SearchResultBean;
import com.zhhz.reader.databinding.FragmentDetailedBinding;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.view.RecycleViewDivider;

public class DetailedFragment extends Fragment {

    private DetailedViewModel mViewModel;

    private FragmentDetailedBinding binding;

    private CatalogueAdapter catalogueAdapter;

    private SearchResultBean searchResultBean;

    private BookBean bookBean;

    public static DetailedFragment newInstance() {
        return new DetailedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DetailedViewModel.class);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getData().observe(getViewLifecycleOwner(), bean -> {
            bookBean = bean;
            System.out.println(bean);
            binding.detailedTitle.setText(bean.getTitle());
            binding.detailedLayout.itemTitle.setText(bean.getTitle());
            if (bean.getCover() != null) {
                GlideApp.with(this)
                        .asBitmap()
                        .load(bean.getCover())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.detailedLayout.itemImage);
            }
            binding.detailedLayout.itemAuthor.setText(bean.getAuthor());
            binding.detailedLayout.itemLatest.setText(bean.getLatestChapter());
            binding.detailedIntro.setText("简介：" + bean.getIntro());
            if (bean.getUpdate_time() != null) {
                binding.detailedUpdateTime.setText("目录（更新时间:" + bean.getUpdate_time() + "）");
            }
            mViewModel.queryCatalogue(bean.getCatalogue(), searchResultBean, 0);
        });
        mViewModel.getDataCatalogue().observe(getViewLifecycleOwner(), map -> {
            catalogueAdapter.setItemData(map);
            catalogueAdapter.notifyDataSetChanged();

            int[] pro = mViewModel.readProgress(bookBean.getBook_id());
            if (pro[0] + pro[1] > 0) {
                binding.startRead.setText("继续阅读");
            }
            binding.startRead.setClickable(true);
        });
        mViewModel.queryDetailed(searchResultBean, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchResultBean = (SearchResultBean) requireActivity().getIntent().getSerializableExtra("book");
        binding.detailedTitle.setText(searchResultBean.getTitle());
        binding.detailedLayout.itemTitle.setText(searchResultBean.getTitle());
        binding.detailedLayout.itemAuthor.setText(searchResultBean.getAuthor());
        binding.detailedLayout.itemLatest.setText(null);

        catalogueAdapter = new CatalogueAdapter(getContext());
        catalogueAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.detailedRv.setItemAnimator(new DefaultItemAnimator());
        binding.detailedRv.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.detailedRv.setHasFixedSize(true);
        binding.detailedRv.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.detailedRv.setAdapter(catalogueAdapter);

        binding.startRead.setText("开始阅读");
        binding.startRead.setClickable(false);

        binding.startRead.setOnClickListener((view) -> {
            SQLiteUtil.saveBook(bookBean);
            mViewModel.saveDirectory(bookBean.getBook_id());
            mViewModel.saveRule(searchResultBean,bookBean.getBook_id(),0);
            Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
            intent.putExtra("book",bookBean);
            startActivity(intent);
            DetailedFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.detailedBack.setOnClickListener((view) -> requireActivity().finish());

        catalogueAdapter.setOnClickListener(view -> {
            SQLiteUtil.saveBook(bookBean);
            Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
            //获取点击事件位置
            int position = binding.detailedRv.getChildAdapterPosition(view);
            mViewModel.saveDirectory(bookBean.getBook_id());
            mViewModel.saveRule(searchResultBean,bookBean.getBook_id(),0);
            mViewModel.saveProgress(bookBean.getBook_id(), position);
            intent.putExtra("book", bookBean);
            startActivity(intent);
            DetailedFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        return root;
    }

}