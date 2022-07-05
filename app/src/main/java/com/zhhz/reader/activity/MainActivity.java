package com.zhhz.reader.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.zhhz.reader.R;
import com.zhhz.reader.bean.RuleBean;
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
            layout.setPrimaryColorsId(android.R.color.white, android.R.color.black);
            return new ClassicsHeader(context).setTimeFormat(new SimpleDateFormat("更新于 HH:mm:ss"));
        });

        //添加一个书源临时测试
        //new RuleAnalysis("/storage/emulated/0/星空/config/rule/www_biquxs_la.json",true);
        //new RuleAnalysis(JSONObject.parseObject("{\"name\":\"笔趣阁\",\"version\":101,\"url\":\"www.cyewx.com\",\"encode\":true,\"remarks\":null,\"image_replace\":{\"rule\":null,\"map\":null},\"search\":{\"url\":\"https://www.biquxs.la/modules/article/search.php@post->searchkey=${key}&searchtype=articlename\",\"charset\":\"utf8\",\"list\":\".grid #nr\",\"name\":\"td:eq(0)\",\"author\":\"td:eq(2)\",\"cover\":null,\"detail\":\"td:eq(0) > a\"},\"detail\":{\"name\":\"meta[name=og:novel:book_name]@attr->content\",\"author\":\"meta[name=og:novel:author]@attr->content\",\"cover\":\"div#fmimg>img\",\"summary\":\"#intro\",\"update\":\"meta[property=og:novel:update_time]@attr->content\",\"lastChapter\":\"meta[property=og:novel:latest_chapter_name]@attr->content\",\"catalog\":null},\"catalog\":{\"list\":\"div#list > dl > dd:gt(9)\",\"name\":\"a\",\"chapter\":\"a\",\"booklet\":null},\"chapter\":{\"filter\":[\"@b\"],\"purify\":[],\"page\":null,\"content\":\"#acontent\"}}"),true);
        //new RuleAnalysis(JSONObject.parseObject("{\"name\":\"6漫画\",\"version\":101,\"url\":\"www.sixmh7.com\",\"encode\":true,\"type\":0,\"header\":\"{\\\"User-Agent\\\":\\\"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36\\\"}\",\"comic\":true,\"img_header\":{\"reuse\":true,\"header\":\"{\\\"Referer\\\":\\\"https://www.sixmh7.com/\\\"}\"},\"image_replace\":{\"rule\":null,\"map\":true},\"search\":{\"url\":\"http://www.sixmh7.com/search.php?keyword=${key}\",\"charset\":\"utf8\",\"list\":\".cy_list_mh > ul\",\"name\":\".title\",\"author\":null,\"cover\":\"img\",\"detail\":\".title > a\"},\"detail\":{\"name\":\".cy_title\",\"author\":\"meta[property=og:author]@attr->content\",\"summary\":\"#comic-description\",\"update\":\"meta[property=og:date]@attr->content\",\"lastChapter\":\"meta[property=og:novel:latest_chapter_name]@attr->content\",\"cover\":\"meta[property=og:image]@attr->content\",\"catalog\":\"js@dmFyIGdlbmdkdW9fZHQxID1lbGVtZW50LnNlbGVjdCgiI3poYW5rYWkiKTsKcmVzdWx0PSJodHRwOi8vd3d3LnNpeG1oNy5jb20vYm9va2NoYXB0ZXIvQHBvc3QtPmlkPSIgKyBnZW5nZHVvX2R0MS5hdHRyKCJkYXRhLWlkIikgKyAiJmlkMj0iICsgZ2VuZ2R1b19kdDEuYXR0cigiZGF0YS12aWQiKSArICIkaGVhZGVye1wiWC1SZXF1ZXN0ZWQtV2l0aFwiOlwiWE1MSHR0cFJlcXVlc3RcIn0i\"},\"catalog\":{\"js\":\"dmFyIEpTT049Y29tLmFsaWJhYmEuZmFzdGpzb24uSlNPTgp2YXIgTGlua2VkSGFzaE1hcD1qYXZhLnV0aWwuTGlua2VkSGFzaE1hcAp2YXIgQ29sbGVjdGlvbnM9amF2YS51dGlsLkNvbGxlY3Rpb25zCnZhciBsaG0gPSBMaW5rZWRIYXNoTWFwKCkKdmFyIHRleHQgPSBKU09OLnBhcnNlKGVsZW1lbnQuc2VsZWN0KCJib2R5IikudGV4dCgpKQpDb2xsZWN0aW9ucy5yZXZlcnNlKHRleHQpCml0ZXJhdG9yID0gdGV4dC5pdGVyYXRvcigpCndoaWxlIChpdGVyYXRvci5oYXNOZXh0KCkpIHsKCXZhciB0ID0gaXRlcmF0b3IubmV4dCgpCiAgICBsaG0ucHV0KHQuY2hhcHRlcm5hbWUsImh0dHA6Ly8iICsgeGx1YV9ydWxlLmdldFVybCgpICsgIi8iICsgdXJsLm1hdGNoKCJpZD0oLispJiIpWzFdICsgIi8iICsgdC5jaGFwdGVyaWQgKyAiLmh0bWwiKQp9Cgp2YXIgdHAgPSAiaHR0cDovLyIgKyB4bHVhX3J1bGUuZ2V0VXJsKCkgKyAiLyIgKyB1cmwubWF0Y2goImlkPSguKykmIilbMV0gICsgIi8iCgoKeGx1YV9ydWxlLkh0dHBfR2V0KHRwLGZ1bmN0aW9uKGRhdGFfeCxtc2csbGFiZWxfeCl7CgoJaWYgKGRhdGFfeCA9PSBudWxsKSB7CgkJcmV0dXJuIGNhbGxiYWNrLnJ1bihudWxsLG1zZyxsYWJlbF94KQoJfQoJdGV4dCA9IGRhdGFfeC5zZWxlY3QoIiNtaC1jaGFwdGVyLWxpc3Qtb2wtMCA+IGxpIikKCUNvbGxlY3Rpb25zLnJldmVyc2UodGV4dCkKCWl0ZXJhdG9yID0gdGV4dC5pdGVyYXRvcigpCgl3aGlsZSAoaXRlcmF0b3IuaGFzTmV4dCgpKSB7CgkJdmFyIHQgPSBpdGVyYXRvci5uZXh0KCkKCQlsaG0ucHV0KHQuc2VsZWN0KCJwIikudGV4dCgpLCJodHRwOi8vIiArIHhsdWFfcnVsZS5nZXRVcmwoKSArIHQuc2VsZWN0KCJhIikuYXR0cigiaHJlZiIpKQkKICAgIH0KCWNhbGxiYWNrLnJ1bihsaG0sbXNnLGxhYmVsX3gpCn0p\",\"list\":\".links-of-books > li\",\"name\":\"a\",\"chapter\":\"a\",\"inverted\":true,\"page\":null,\"booklet\":null},\"chapter\":{\"filter\":[],\"purify\":[],\"page\":null,\"content\":\"script\",\"js\":\"ZXZhbChkYXRhLm1hdGNoKCdldmFsXCgoLispXCknKVswXSk7cmVzdWx0ID0gbmV3SW1nczs=\"}}"), true);

        for (RuleBean rule : SQLiteUtil.readRules()) {
            if (rule.isOpen()) {
                try {
                    new RuleAnalysis(rule.getFile(), true);
                } catch (IOException e) {
                    Snackbar.make(binding.getRoot(), rule.getName() + " -> 导入失败", Snackbar.LENGTH_SHORT).setAction("查看详细", v -> new AlertDialog.Builder(this)
                            .setTitle("错误提示")
                            .setMessage(e.getMessage())
                            .setOnCancelListener(DialogInterface::dismiss)
                            .show()).show();
                }
            }
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