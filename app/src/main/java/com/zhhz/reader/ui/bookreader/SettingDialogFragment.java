package com.zhhz.reader.ui.bookreader;

import android.app.Dialog;
import android.os.Bundle;
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

import com.alibaba.fastjson.JSONObject;
import com.zhhz.reader.R;
import com.zhhz.reader.databinding.FragmentBookReaderSettingBinding;
import com.zhhz.reader.sql.SQLiteUtil;

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

    private void setting(JSONObject s){
        binding.fontSize.setText(s.getString("textSize"));
        binding.fontMargin.setText(String.valueOf(s.getIntValue("marginSpacing")));
        binding.fontFieldSpacing.setText(String.valueOf(s.getIntValue("segmentSpacing")));
        binding.fontSpacing.setText(s.getString("fontSpacing"));
        binding.fontLineSpacing.setText(s.getString("lineHeightRatio"));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getFontSettingText().observe(getViewLifecycleOwner(), s -> {
            binding.fontSizeAdd.setEnabled(s.getIntValue("textSize") < 46);
            binding.fontSizeSub.setEnabled(s.getIntValue("textSize") > 12);
            binding.fontMarginAdd.setEnabled(s.getIntValue("marginSpacing") < 80);
            binding.fontMarginSub.setEnabled(s.getIntValue("marginSpacing") > 28);
            binding.fontFieldSpacingAdd.setEnabled(s.getIntValue("segmentSpacing") < 24);
            binding.fontFieldSpacingSub.setEnabled(s.getIntValue("segmentSpacing") > 0);
            binding.fontSpacingAdd.setEnabled(s.getIntValue("fontSpacing") < 32);
            binding.fontSpacingSub.setEnabled(s.getIntValue("fontSpacing") > 0);
            binding.fontLineSpacingAdd.setEnabled(s.getFloatValue("lineHeightRatio") < 2);
            binding.fontLineSpacingSub.setEnabled(s.getFloatValue("lineHeightRatio") > 1);
            setting(s);
        });
        setting(mViewModel.readSetting());
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
