package com.zhhz.reader.ui.bookreader;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.R;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.databinding.FragmentBookreaderBinding;
import com.zhhz.reader.view.ReadTextView;

import java.util.Objects;

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
        binding = FragmentBookreaderBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        binding.readerText.setTitle("加载中…");
        binding.readerText.setTitleColor(Color.GRAY);

        binding.readerText.setUpdateCallBack(() -> {
            mViewModel.saveProgress(mViewModel.getProgress(),binding.readerText.getTextStart());
            return false;
        });

        binding.readerText.setDownPage(() -> {
            if (mViewModel.isHaveNextChapters()){
                mViewModel.setStart(0);
                mViewModel.loadNextChapters();
                return true;
            } else {
                Toast.makeText(requireContext(), "已经没有下一章了", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        binding.readerText.setUpPage(() -> {
            if (mViewModel.isHavePreviousChapters()){
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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getDataContent().observe(getViewLifecycleOwner(), map -> {
            binding.progress.hide();
            if (map.containsKey("error")){
                binding.readerText.setText(String.valueOf(map.get("error")));
                binding.bookReader.addView(error_btn,binding.progress.getLayoutParams());
            } else {
                if (Objects.equals(map.get("end"), "true")) {
                    mViewModel.setStart(String.valueOf(map.get("content")).length());
                }
                binding.readerText.setText(String.valueOf(map.get("content")),mViewModel.getStart());
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

}