package com.zhhz.reader.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.databinding.ActivityMainBinding;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.ui.bookrack.BookRackViewModel;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BookRackViewModel bookrackViewModel;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置刷新头
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            layout.setPrimaryColorsId(android.R.color.white, android.R.color.black);
            return new ClassicsHeader(context).setTimeFormat(new SimpleDateFormat("更新于 HH:mm:ss"));
        });

        //添加一个书源临时测试

        for (RuleBean rule : SQLiteUtil.readRules()) {
            if (rule.isOpen()) {
                try {
                    new RuleAnalysis(rule.getFile(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        bookrackViewModel = new ViewModelProvider(this).get(BookRackViewModel.class);

        //缓存，只支持单本缓存(等待更新)
        binding.itemMenu.getChildAt(0).setEnabled(false);
        //binding.itemMenu.getChildAt(0).setOnClickListener(view -> bookrackViewModel.operationBooks(0));

        //导出书本,只支持单本导出(等待更新)
        binding.itemMenu.getChildAt(1).setEnabled(true);
        binding.itemMenu.getChildAt(1).setOnClickListener(view -> bookrackViewModel.operationBooks(1));

        //删除
        binding.itemMenu.getChildAt(2).setOnClickListener(view -> bookrackViewModel.operationBooks(2));

        binding.itemMenu.getChildAt(3).setEnabled(false);
        binding.itemMenu.getChildAt(4).setEnabled(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


}