package com.zhhz.reader.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.zhhz.reader.R;
import com.zhhz.reader.service.LogMonitorService;
import com.zhhz.reader.util.ManifestUtil;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private ActivityResultLauncher<Intent> launcher;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        @NonNull SwitchPreferenceCompat log = Objects.requireNonNull(findPreference("log"));

        log.setOnPreferenceChangeListener((preference, newValue) -> {
            if (Boolean.getBoolean(newValue.toString())){
                boolean isAllGranted = Settings.canDrawOverlays(getContext());
                if (!isAllGranted){
                    ManifestUtil.openAppDetails(requireContext(), launcher);
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            //第 1 步: 检查是否有相应的权限，根据自己需求，进行添加相应的权限
            boolean isAllGranted = Settings.canDrawOverlays(getContext());
            //如果没有赋予权限，着取消勾选
            if (isAllGranted) {
                ((SwitchPreferenceCompat) Objects.requireNonNull(findPreference("log"))).setChecked(true);
            }
        });
    }
}