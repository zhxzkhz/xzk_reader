package com.zhhz.reader.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zhhz.reader.MyApplication;
import com.zhhz.reader.R;

import java.util.UUID;

public class NotificationUtil {
    private static final String channelId = UUID.randomUUID().toString();
    private static final String channelName = "通知消息";

    public static void sendMessage(String msg){
        NotificationManagerCompat notificationManger = NotificationManagerCompat.from(MyApplication.context);
        notificationManger.createNotificationChannel(new NotificationChannel(channelId,channelName, NotificationManager.IMPORTANCE_HIGH));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.context,channelId);
        builder.setSmallIcon(R.drawable.search_icon);
        builder.setAutoCancel(true);
        builder.setContentTitle("消息通知");
        builder.setContentTitle(msg);
        builder.setWhen(System.currentTimeMillis());
        notificationManger.notify(1,builder.build());
    }

}