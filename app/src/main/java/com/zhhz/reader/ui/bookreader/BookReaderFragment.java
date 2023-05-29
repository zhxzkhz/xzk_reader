package com.zhhz.reader.ui.bookreader;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.databinding.FragmentBookreaderBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BookReaderFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentBookreaderBinding binding;

    private AppCompatButton error_btn;

    public static BookReaderFragment newInstance() {
        return new BookReaderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
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
                mViewModel.setStart(0);
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
                mViewModel.setStart(0);
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



        error_btn = new AppCompatButton(requireContext());
        error_btn.setText("重新加载");
        error_btn.setOnClickListener(view -> mViewModel.getContent());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                    binding.readerText.setLineHeight(binding.readerText.getLineHeight() - 1f);
                    break;
                }
                case "font_field_spacing_add": {
                    binding.readerText.setLineHeight(binding.readerText.getLineHeight() + 1f);
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
                if (error_btn.getParent()!=null){
                    ((android.view.ViewGroup)error_btn.getParent()).removeView(error_btn);
                }
                binding.bookReader.addView(error_btn, binding.progress.getLayoutParams());
            } else {
                //判断是否转跳到文本末尾
                if (map.containsKey("end") && Boolean.parseBoolean(String.valueOf(map.get("end")))) {
                    mViewModel.setStart(String.valueOf(map.get("content")).length()-1);
                }
                binding.readerText.setText(String.valueOf(map.get("content")), mViewModel.getStart());
                mViewModel.saveProgress(mViewModel.getProgress(), binding.readerText.getTextStart());
            }
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), title -> {
            binding.progress.show();
            binding.readerText.setTitle(title);
            binding.readerText.setText(null);
        });

        mViewModel.queryCatalogue();
        int[] r = mViewModel.readProgress();
        mViewModel.setProgress(r[0]);
        mViewModel.setStart(r[1]);
        mViewModel.getContent();

    }

    public boolean onKeyDown(int keyCode, KeyEvent ignoredEvent) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (mViewModel.isLoading()) return true;
            binding.readerText.up_page();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (mViewModel.isLoading()) return true;
            binding.readerText.down_page();
            return true;
        }
        return false;
    }

}