package com.example.rfid_mobilapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (server != null) {
            String jsonString= "";
            if (intent != null && intent.getAction() != null && intent.getExtras() != null) {
                if (intent.getAction().equals("READ_ITEM_ID")) {
                    String itemId = intent.getExtras().getString("itemId");
                    jsonString= Utilities.createJsonString("read_item_id", itemId);
                    server.broadcast(jsonString);
                } else if (intent.getAction().equals("WRITE_ITEM_ID")) {
                    String status = intent.getExtras().getString("itemId");
                    jsonString= Utilities.createJsonString("write_item_id", status);
                    server.broadcast(jsonString);
                } else if (intent.getAction().equals("CHECK")) {
                    String status = intent.getExtras().getString("doCheckIn");
                    jsonString= Utilities.createJsonString("check", status);
                    server.broadcast(jsonString);
                }else if (intent.getAction().equals("READ")) {
                String status = intent.getExtras().getString("read_tag");
                jsonString= Utilities.createJsonString("read_tag", status);
                server.broadcast(jsonString);
            }

        }
        } else if (MainActivity.isServerOn()){
            createNotificationChannel();
            createNotification();
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

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        try {
            server.stop();
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
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(this, 0, openAppIntent, 0);
        NotificationCompat.Builder SocketServerServiceNotification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.message_24)
                .setContentTitle(getResources().getString(R.string.socket_server_service))
                .setContentText(getResources().getString(R.string.service_running))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.back_24, getResources().getString(R.string.back_app), openAppPendingIntent)
                .setOngoing(true);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, SocketServerServiceNotification.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, "channelName", importance);
            channel.setDescription("My channel");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}