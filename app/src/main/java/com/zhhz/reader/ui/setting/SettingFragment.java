package com.zhhz.reader.ui.setting;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zhhz.reader.databinding.FragmentSettingBinding;
import com.zhhz.reader.service.LogMonitorService;
import com.zhhz.reader.util.ManifestUtil;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModelViewModel;
    private FragmentSettingBinding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            //第 1 步: 检查是否有相应的权限，根据自己需求，进行添加相应的权限
            boolean isAllGranted = ManifestUtil.checkPermissionAllGranted(requireContext(),
                    new String[]{
                            Manifest.permission.SYSTEM_ALERT_WINDOW
                    }
            );
            //如果没有赋予权限，着弹窗对话框
            if (isAllGranted) {
                requireActivity().startService(new Intent(requireActivity(), LogMonitorService.class));
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                ManifestUtil.openAppDetails(requireContext(),launcher);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingViewModelViewModel =
                new ViewModelProvider(this).get(SettingViewModel.class);

        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        settingViewModelViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}