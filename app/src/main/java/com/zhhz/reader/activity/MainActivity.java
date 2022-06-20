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
import com.zhhz.reader.sql.SQLiteUtil;

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


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // 将每个菜单ID作为一组ID传递，因为每个菜单应被视为顶级界面。
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bookrack, R.id.navigation_rule, R.id.navigation_setting)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}