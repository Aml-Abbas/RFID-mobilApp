package com.axiell_exjobb.RFID_mobilapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketServerService extends Service {
    NotificationManager notificationManager;
    SocketServer server;
    InetSocketAddress listenAddress;
    Thread thread;
    private final String host = "localhost";
    private final int port = 8888;
    private final String channelId = "channel";
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
        startForeground(110, notification);
        thread = new Thread(() -> {
            try {
                listenAddress = new InetSocketAddress(host, port);
                server = new SocketServer(listenAddress);
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String jsonString = "";
        if (intent != null && intent.getAction() != null && intent.getExtras() != null) {
            if (intent.getAction().equals("READ_ITEM_ID")) {
                String itemId = intent.getExtras().getString("itemId");
                jsonString = Utilities.createJsonString("read_item_id", itemId);
            } else if (intent.getAction().equals("WRITE_ITEM_ID")) {
                String status = intent.getExtras().getString("itemId");
                jsonString = Utilities.createJsonString("write_item_id", status);
            } else if (intent.getAction().equals("CHECK")) {
                String status = intent.getExtras().getString("doCheckIn");
                jsonString = Utilities.createJsonString("check", status);
            } else if (intent.getAction().equals("READ")) {
                String status = intent.getExtras().getString("read_tag");
                if (status == null) {
                    status = getResources().getString(R.string.failed_read);
                }
                jsonString = Utilities.createJsonString("read_tag", status);
            }
            if (jsonString != null) {
                server.broadcast(jsonString);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        try {
            if (server != null) {
                server.stop();
            }
            thread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(this, 0, openAppIntent, 0);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.message_24)
                .setContentTitle(getResources().getString(R.string.socket_server_service))
                .setContentText(getResources().getString(R.string.service_running))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.back_24, getResources().getString(R.string.back_app), openAppPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String getNotificationChannel(NotificationManager notificationManager) {
        String channelName = getResources().getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

}