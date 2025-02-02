package com.zhhz.reader.ui.bookreader;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zhhz.reader.databinding.FragmentBookreaderBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BookReaderFragment extends BookReaderFragmentBase{

    protected FragmentBookreaderBinding binding;

    public static BookReaderFragment newInstance() {
        return new BookReaderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookreaderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.readerText.setTitle("加载中…");
        binding.readerText.setTitleColor(Color.GRAY);

        //获取字体设置
        binding.readerText.setting(mViewModel.readSetting());

        binding.readerText.setUpdateCallBack(() -> {
            mViewModel.saveProgress(mViewModel.getProgress(), binding.readerText.getTextStart());
            return false;
        });

        binding.readerText.setDownPage(() -> {
            if (mViewModel.isLoading()) return true;
            if (mViewModel.isHaveNextChapters()) {
                mViewModel.setPos(0);
                mViewModel.loadNextChapters();
                return true;
            } else {
                Toast.makeText(requireContext(), "已经没有下一章了", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        binding.readerText.setUpPage(() -> {
            if (mViewModel.isLoading()) return true;
            if (mViewModel.isHavePreviousChapters()) {
                mViewModel.setPos(0);
                mViewModel.loadPreviousChapters();
                return true;
            } else {
                Toast.makeText(requireContext(), "已经是第一章了", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        binding.readerText.setMenuClick(() -> {
            if (container != null) {
                container.callOnClick();
            }
            return false;
        });

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(-2,-2);
        layoutParams.topToTop = binding.progress.getId();
        layoutParams.bottomToBottom = binding.progress.getId();
        layoutParams.leftToLeft = binding.progress.getId();
        layoutParams.rightToRight = binding.progress.getId();
        binding.bookReader.addView(errorRetryButton, layoutParams);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        mViewModel.getFontSetting().observe(getViewLifecycleOwner(), s -> {
                switch (s){
                    case "font_size_sub": {
                        binding.readerText.setTextSize(binding.readerText.getTextSize() - 1);
                        break;
                    }
                    case "font_size_add": {
                        binding.readerText.setTextSize(binding.readerText.getTextSize() + 1);
                        break;
                    }
                    case "font_margin_sub": {
                        binding.readerText.setMarginSpacing(binding.readerText.getMarginSpacing() - 1f);
                        break;
                    }
                    case "font_margin_add": {
                        binding.readerText.setMarginSpacing(binding.readerText.getMarginSpacing() + 1f);
                        break;
                    }
                    case "font_field_spacing_sub": {
                        binding.readerText.setSegmentSpacing(binding.readerText.getSegmentSpacing() - 1f);
                        break;
                    }
                    case "font_field_spacing_add": {
                        binding.readerText.setSegmentSpacing(binding.readerText.getSegmentSpacing() + 1f);
                        break;
                    }
                    case "font_spacing_sub": {
                        binding.readerText.setFontSpacing(binding.readerText.getFontSpacing() - 1f);
                        break;
                    }
                    case "font_spacing_add": {
                        binding.readerText.setFontSpacing(binding.readerText.getFontSpacing() + 1f);
                        break;
                    }
                    case "font_line_spacing_sub": {
                        BigDecimal b = BigDecimal.valueOf(binding.readerText.getLineHeightRatio() - 0.1f);
                        binding.readerText.setLineHeightRatio(b.setScale(1, RoundingMode.HALF_UP).floatValue());
                        break;
                    }
                    case "font_line_spacing_add": {
                        BigDecimal b = BigDecimal.valueOf(binding.readerText.getLineHeightRatio() + 0.1f);
                        binding.readerText.setLineHeightRatio(b.setScale(1, RoundingMode.HALF_UP).floatValue());
                        break;
                    }
                }
            mViewModel.saveSetting(binding.readerText);
        });

        mViewModel.getDataContent().observe(getViewLifecycleOwner(), map -> {
            binding.progress.hide();
            if (map.containsKey("error")) {
                binding.readerText.setText(String.valueOf(map.get("error")));
                errorRetryButton.setVisibility(View.VISIBLE);
            } else {
                //判断是否转跳到文本末尾
                if (map.containsKey("end") && Boolean.parseBoolean(String.valueOf(map.get("end")))) {
                    mViewModel.setPos(String.valueOf(map.get("content")).length()-1);
                }
                binding.readerText.setText(String.valueOf(map.get("content")), mViewModel.getPos());
                mViewModel.saveProgress(mViewModel.getProgress(), binding.readerText.getTextStart());
            }
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), title -> {
            binding.progress.show();
            binding.readerText.setTitle(title);
            binding.readerText.setText(null);
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void upPage() {
        binding.readerText.up_page();
    }

    @Override
    public void downPage() {
        binding.readerText.down_page();
    }
}