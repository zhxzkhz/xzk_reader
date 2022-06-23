package com.zhhz.reader.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.zhhz.reader.R;
import com.zhhz.reader.databinding.ActivityMainBinding;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteUtil.context = getApplicationContext();

        //设置刷新头
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            //layout.setPrimaryColorsId(new int[]{R.color.colorPrimary, android.R.color.white});
            return new ClassicsHeader(context).setTimeFormat(new SimpleDateFormat("更新于 HH:mm:ss"));
        });

        //添加一个书源临时测试
        try {
            new RuleAnalysis("/storage/emulated/0/星空/config/rule/www_biquxs_la.json",true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}