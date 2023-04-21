package com.zhhz.reader.ui.bookreader;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.R;
import com.zhhz.reader.databinding.FragmentBookReaderSettingBinding;

import java.util.Objects;


public class SettingDialogFragment extends DialogFragment {

    private static SettingDialogFragment fragment;

    private FragmentBookReaderSettingBinding binding;

    private BookReaderViewModel mViewModel;

    public static SettingDialogFragment getInstance() {
        if (fragment != null) return fragment;
        return fragment = new SettingDialogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = FragmentBookReaderSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View.OnClickListener click = v -> mViewModel.setFont_setting(v.getTag().toString());

        binding.fontSizeAdd.setOnClickListener(click);
        binding.fontSizeSub.setOnClickListener(click);
        binding.fontMarginAdd.setOnClickListener(click);
        binding.fontMarginSub.setOnClickListener(click);
        binding.fontFieldSpacingAdd.setOnClickListener(click);
        binding.fontFieldSpacingSub.setOnClickListener(click);
        binding.fontSpacingAdd.setOnClickListener(click);
        binding.fontSpacingSub.setOnClickListener(click);
        binding.fontLineSpacingAdd.setOnClickListener(click);
        binding.fontLineSpacingSub.setOnClickListener(click);

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
        setStyle(STYLE_NO_TITLE, R.style.style_dialog);
    }

    /**
     * 修改布局的大小
     */
    @Override
    public void onStart() {
        super.onStart();
        resizeDialogFragment();

    }
    private void resizeDialogFragment() {
        Dialog dialog = getDialog();
        if (null != dialog) {
            Window window = dialog.getWindow();
            //window.setBackgroundDrawableResource(R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.BOTTOM;
            lp.dimAmount = 0;
            lp.horizontalMargin = 0;
            lp.verticalMargin = 0;
            window.setAttributes(lp);
        }
    }



}
