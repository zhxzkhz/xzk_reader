package com.zhhz.reader.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class ManifestUtil {

    /**
     * 检查是否拥有指定的所有权限
     */
    public static boolean checkPermissionAllGranted(Context context,String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 打开 APP 的详情设置
     */
    public static void openAppDetails(Context context, ActivityResultLauncher<Intent> launcher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("软件需要用到网络和储存权限，请到 “应用信息 -> 权限” 中授予！");
        builder.setCancelable(false);
        builder.setPositiveButton("去手动授权", (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            launcher.launch(intent);
        });
        builder.setNegativeButton("退出",(dialog, which) -> {
            System.exit(0);
        });
        builder.show();
    }

}
