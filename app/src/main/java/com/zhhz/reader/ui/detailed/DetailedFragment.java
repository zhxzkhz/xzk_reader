package com.zhhz.reader.ui.detailed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhhz.reader.R;
import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.adapter.CatalogueAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.bean.ResultListBean;
import com.zhhz.reader.databinding.FragmentDetailedBinding;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.view.RecycleViewDivider;

import cn.hutool.core.util.ObjectUtil;

public class DetailedFragment extends Fragment {

    private DetailedViewModel mViewModel;

    private FragmentDetailedBinding binding;

    private CatalogueAdapter catalogueAdapter;

    private ResultListBean resultListBean;

    private BookBean bookBean;

    private ActivityResultLauncher<Intent> launcher;


    public static DetailedFragment newInstance() {
        return new DetailedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> readCheck());
        mViewModel = new ViewModelProvider(this).get(DetailedViewModel.class);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getData().observe(getViewLifecycleOwner(), bean -> {
            if (bean == null){
                binding.detailedTitle.setText("请求失败");
                binding.detailedLayout.itemTitle.setText("详情界面请求URL为空");
                Toast.makeText(requireContext(),"详情界面请求URL为空",Toast.LENGTH_LONG).show();
                return;
            }

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
                        .placeholder(R.drawable.no_cover)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.detailedLayout.itemImage);
            }
            binding.detailedLayout.itemLatest.setText(bean.getLastChapter());
            binding.detailedIntro.setText("简介：" + bean.getIntro());
            if (bean.getUpdateTime() != null) {
                binding.detailedUpdateTime.setText("目录（更新时间:" + bean.getUpdateTime() + "）");
            }
            if (ObjectUtil.isEmpty(bean.getCatalogue())){
                binding.startRead.setText("目录为空或者获取失败");
                binding.startRead.setOnClickListener(v -> mViewModel.queryDetailed(resultListBean, 0));
            } else {
                mViewModel.queryCatalogue(bean.getCatalogue(), resultListBean, 0);
            }
        });
        mViewModel.getDataCatalogue().observe(getViewLifecycleOwner(), map -> {
            if (map == null || map.isEmpty()) {
                binding.startRead.setText("目录获取失败");
                binding.startRead.setOnClickListener(v -> mViewModel.queryCatalogue(bookBean.getCatalogue(), resultListBean, 0));
            } else {
                catalogueAdapter.setItemData(map);
                catalogueAdapter.notifyDataSetChanged();
                readCheck();
                if (!map.isEmpty()) {
                    binding.startRead.setTextColor(Color.BLACK);
                    binding.startRead.setClickable(true);
                    binding.startRead.setOnClickListener(view1 -> {
                        SQLiteUtil.saveBook(bookBean);
                        mViewModel.saveDirectory(bookBean.getBookId());
                        mViewModel.saveRule(resultListBean, bookBean.getBookId(), 0);
                        Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
                        intent.putExtra("book", (Parcelable)bookBean );
                        //DetailedFragment.this.startActivity(intent);
                        launcher.launch(intent);
                        DetailedFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    });
                } else {
                    binding.startRead.setText("暂无章节");
                }
            }
        });
        mViewModel.queryDetailed(resultListBean, 0);
    }

    @SuppressLint("SetTextI18n")
    public void readCheck() {
        int[] pro = mViewModel.readProgress(bookBean.getBookId());
        if (pro[0] + pro[1] > 0 && catalogueAdapter.getTitleList().size() > pro[0]) {
            binding.startRead.setText("继续阅读(" + catalogueAdapter.getTitleList().get(pro[0]) + ")");
        } else {
            binding.startRead.setText("开始阅读");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        resultListBean = (ResultListBean) requireActivity().getIntent().getSerializableExtra("book");
        assert resultListBean != null;
        binding.detailedTitle.setText(resultListBean.getTitle());
        binding.detailedLayout.itemTitle.setText(resultListBean.getTitle());
        binding.detailedLayout.itemAuthor.setText(resultListBean.getAuthor());
        binding.detailedLayout.itemLatest.setText(null);
        if (resultListBean.getCover() != null && !resultListBean.getCover().isEmpty()) {
            GlideApp.with(this)
                    .asBitmap()
                    .load(resultListBean.getCover())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.detailedLayout.itemImage);
        }
        catalogueAdapter = new CatalogueAdapter();
        catalogueAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.detailedRv.setItemAnimator(new DefaultItemAnimator());
        binding.detailedRv.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.detailedRv.setHasFixedSize(true);
        binding.detailedRv.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.detailedRv.setAdapter(catalogueAdapter);

        binding.startRead.setText("目录获取中");
        binding.startRead.setTextColor(Color.GRAY);
        binding.startRead.setClickable(false);

        binding.detailedIntro.setOnClickListener(view -> new AlertDialog.Builder(requireContext())
                .setTitle("简介")
                .setMessage(((AppCompatTextView) view).getText())
                .setOnCancelListener(DialogInterface::dismiss)
                .show());

        binding.detailedBack.setOnClickListener((view) -> requireActivity().finish());

        catalogueAdapter.setOnClickListener(view -> {
            SQLiteUtil.saveBook(bookBean);
            Intent intent = new Intent(DetailedFragment.this.getContext(), BookReaderActivity.class);
            //获取点击事件位置
            int position = binding.detailedRv.getChildAdapterPosition(view);
            mViewModel.saveDirectory(bookBean.getBookId());
            mViewModel.saveRule(resultListBean, bookBean.getBookId(), 0);
            mViewModel.saveProgress(bookBean.getBookId(), position);
            intent.putExtra("book", (Parcelable)bookBean);
            //startActivity(intent);
            launcher.launch(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        return root;
    }

}