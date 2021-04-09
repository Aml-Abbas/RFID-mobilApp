package com.example.rfid_mobilapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.se.omapi.Session;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.rfid_mobilapp.MainActivity;
import com.example.rfid_mobilapp.R;
import com.example.rfid_mobilapp.SocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketServerService extends Service {
    NotificationManager notificationManager;
    SocketServer server;
    InetSocketAddress listenAddress;
    private Thread serverThread;
    private final String host = "localhost";
    private final int port = 8888;
    private final String TAG = SocketServerService.class.getSimpleName();
    private  final  String channelId = "channel";
    @Override
    public void onCreate() {
        serverThread=  new Thread(() -> {
            InetSocketAddress listenAddress = new InetSocketAddress(host, port);
            server = new SocketServer(listenAddress, this);
        });
        serverThread.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (server != null) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("READ_TAG") && intent.getExtras() != null) {
                String itemId = intent.getExtras().getString("itemId");
                server.sendToAll(itemId);
            }
        } else {
            createNotificationChannel();
            createNotification();
            Thread thread = new Thread(() -> {
                String host = "localhost";
                int port = 8888;
                try {
                    listenAddress = new InetSocketAddress(host, port);
                    server = new SocketServer(listenAddress, this);
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
            serverThread.interrupt();
            serverThread.interrupt();
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
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel= new NotificationChannel(channelId, "channelName", importance);
            channel.setDescription("My channel");
            NotificationManager notificationManager= getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void openApp(){
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(openAppIntent);
    }
}