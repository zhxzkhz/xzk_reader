package com.zhhz.reader.ui.detailed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
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

            if (bean.getTitle() != null && !bean.getTitle().isEmpty()) {
                binding.detailedTitle.setText(bean.getTitle());
                binding.detailedLayout.itemTitle.setText(bean.getTitle());
            }
            if (bean.getAuthor() != null && !bean.getAuthor().isEmpty()) {
                binding.detailedLayout.itemAuthor.setText(bean.getAuthor());
            }

            if (bean.getCover() != null && !bean.getCover().isEmpty()) {
                GlideApp.with(this)
                        .asBitmap()
                        .load(bean.getCover())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.detailedLayout.itemImage);
            }
            binding.detailedLayout.itemLatest.setText(bean.getLatestChapter());
            binding.detailedIntro.setText("?????????" + bean.getIntro());
            if (bean.getUpdate_time() != null) {
                binding.detailedUpdateTime.setText("?????????????????????:" + bean.getUpdate_time() + "???");
            }
            mViewModel.queryCatalogue(bean.getCatalogue(), searchResultBean, 0);
        });
        mViewModel.getDataCatalogue().observe(getViewLifecycleOwner(), map -> {
            if (map == null) {
                binding.startRead.setText("??????????????????");
                binding.startRead.setOnClickListener(v -> mViewModel.queryCatalogue(bookBean.getCatalogue(), searchResultBean, 0));
            } else {
                catalogueAdapter.setItemData(map);
                catalogueAdapter.notifyDataSetChanged();
                int[] pro = mViewModel.readProgress(bookBean.getBook_id());
                if (pro[0] + pro[1] > 0) {
                    binding.startRead.setText("????????????(" + catalogueAdapter.getTitle().get(pro[0]) + ")");
                } else {
                    binding.startRead.setText("????????????");
                }
                if (map.size() > 0) {
                    binding.startRead.setTextColor(Color.BLACK);
                    binding.startRead.setClickable(true);
                    binding.startRead.setOnClickListener(view1 -> {
                        SQLiteUtil.saveBook(bookBean);
                        mViewModel.saveDirectory(bookBean.getBook_id());
                        mViewModel.saveRule(searchResultBean, bookBean.getBook_id(), 0);
                        Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
                        intent.putExtra("book", bookBean);
                        DetailedFragment.this.startActivity(intent);
                        DetailedFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    });
                } else {
                    binding.startRead.setText("????????????");
                }
            }
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
        if (searchResultBean.getCover() != null && !searchResultBean.getCover().isEmpty()) {
            GlideApp.with(this)
                    .asBitmap()
                    .load(searchResultBean.getCover())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.detailedLayout.itemImage);
        }
        catalogueAdapter = new CatalogueAdapter();
        catalogueAdapter.setHasStableIds(true);
        //??????Item?????????????????????
        binding.detailedRv.setItemAnimator(new DefaultItemAnimator());
        binding.detailedRv.setLayoutManager(new LinearLayoutManager(getContext()));
        //????????????
        binding.detailedRv.setHasFixedSize(true);
        binding.detailedRv.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.detailedRv.setAdapter(catalogueAdapter);

        binding.startRead.setText("???????????????");
        binding.startRead.setTextColor(Color.GRAY);
        binding.startRead.setClickable(false);

        binding.detailedIntro.setOnClickListener(view -> new AlertDialog.Builder(requireContext())
                .setTitle("??????")
                .setMessage(((AppCompatTextView) view).getText())
                .setOnCancelListener(DialogInterface::dismiss)
                .show());

        binding.detailedBack.setOnClickListener((view) -> requireActivity().finish());

        catalogueAdapter.setOnClickListener(view -> {
            SQLiteUtil.saveBook(bookBean);
            Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
            //????????????????????????
            int position = binding.detailedRv.getChildAdapterPosition(view);
            mViewModel.saveDirectory(bookBean.getBook_id());
            mViewModel.saveRule(searchResultBean, bookBean.getBook_id(), 0);
            mViewModel.saveProgress(bookBean.getBook_id(), position);
            intent.putExtra("book", bookBean);
            startActivity(intent);
            DetailedFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        return root;
    }

}