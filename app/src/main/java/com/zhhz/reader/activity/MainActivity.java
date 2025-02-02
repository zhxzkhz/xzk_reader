package com.zhhz.reader.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.zhhz.reader.R;
import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.databinding.ActivityMainBinding;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.ui.bookrack.BookRackFragment;
import com.zhhz.reader.ui.bookrack.BookRackViewModel;
import com.zhhz.reader.util.LogUtil;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BookRackViewModel bookrackViewModel;

    private long lastBackPressedTime = 0;
    private Toast toast;

    private void requestPermissions(Activity context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions(this);

        //加载规则
        for (RuleBean rule : SQLiteUtil.readRules()) {
            if (rule.isOpen()) {
                try {
                    new RuleAnalysis(rule.getFile(), true);
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        bookrackViewModel = new ViewModelProvider(this).get(BookRackViewModel.class);

        //缓存，只支持单本缓存(等待更新)
        binding.itemMenu.getChildAt(0).setEnabled(true);
        binding.itemMenu.getChildAt(0).setOnClickListener(view -> bookrackViewModel.operationBooks(BookRackViewModel.Operation.CACHE));

        //导出书本,只支持单本导出(等待更新)
        binding.itemMenu.getChildAt(1).setEnabled(true);
        binding.itemMenu.getChildAt(1).setOnClickListener(view -> bookrackViewModel.operationBooks(BookRackViewModel.Operation.PACK));

        //删除
        binding.itemMenu.getChildAt(2).setOnClickListener(view -> bookrackViewModel.operationBooks(BookRackViewModel.Operation.DELETE));

        binding.itemMenu.getChildAt(3).setEnabled(false);
        binding.itemMenu.getChildAt(4).setEnabled(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment fragment = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main)).getChildFragmentManager().getFragments().get(0);

        if (fragment instanceof BookRackFragment) {
            boolean bool = ((BookRackFragment) fragment).onKeyDown(keyCode, event);
            if (bool) return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackPressedTime < 1000) {
                toast.cancel();
                finish();
            } else {
                // 提示用户再按一次退出
                if (toast != null) {
                    toast.cancel(); // 取消之前的 Toast
                }
                toast = Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT);
                toast.show();
                lastBackPressedTime = currentTime; // 更新最后按下的时间
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}