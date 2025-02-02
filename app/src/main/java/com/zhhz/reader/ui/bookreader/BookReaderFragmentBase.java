package com.zhhz.reader.ui.bookreader;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public abstract class BookReaderFragmentBase extends Fragment{

    protected BookReaderViewModel mViewModel;
    protected AppCompatButton errorRetryButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);

        errorRetryButton = new AppCompatButton(requireContext());
        errorRetryButton.setText("重新加载");
        errorRetryButton.setOnClickListener(view -> {
            view.setVisibility(View.INVISIBLE);
            mViewModel.getContent();
        });
        errorRetryButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.queryCatalogue();
        int[] r = mViewModel.readProgress();
        mViewModel.setProgress(r[0]);
        mViewModel.setStart(r[1]);
        mViewModel.getContent();
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) return true;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (mViewModel.isLoading()) return true;
            upPage();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (mViewModel.isLoading()) return true;
            downPage();
            return true;
        }
        return false;
    }

    /**
     * 上一页
     */
    public abstract void upPage();

    /**
     * 下一页
     */
    public abstract void downPage();



}