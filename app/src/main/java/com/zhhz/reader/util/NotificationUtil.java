package com.zhhz.reader.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zhhz.reader.MyApplication;
import com.zhhz.reader.R;

import java.util.UUID;

public class NotificationUtil {
    private static final String channelId = UUID.randomUUID().toString();
    private static final String channelName = "通知消息";
    private static final String channelDownloadName = "导出书本消息";

    private static final int messageId = 100;

    public static void sendMessage(String msg) {
        if (ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat notificationManger = NotificationManagerCompat.from(MyApplication.context);
        notificationManger.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.context, channelId);
        builder.setSmallIcon(R.drawable.search_icon);
        builder.setAutoCancel(true);
        builder.setContentTitle("消息通知");
        builder.setContentTitle(msg);
        builder.setWhen(System.currentTimeMillis());
        notificationManger.notify(messageId, builder.build());
    }


    public static void sendProgressMessage(String title, String msg, int progress, int max) {
        if (ActivityCompat.checkSelfPermission(MyApplication.context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat notificationManger = NotificationManagerCompat.from(MyApplication.context);
        notificationManger.createNotificationChannel(new NotificationChannel(channelId, channelDownloadName, NotificationManager.IMPORTANCE_HIGH));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.context, channelId)
                .setSmallIcon(R.drawable.search_icon)
                .setContentTitle(title)
                .setContentText(msg)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        builder.setProgress(max, progress, false);
        notificationManger.notify(1, builder.build());

    }


}
