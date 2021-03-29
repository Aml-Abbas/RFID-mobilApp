package com.example.rfid_mobilapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.net.InetSocketAddress;

public class SocketServerService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        Thread thread = new Thread(() -> {
            String host = "localhost";
            int port = 8888;
            try {
                InetSocketAddress listenAddress = new InetSocketAddress(host, port);
                SocketServer server = new SocketServer(listenAddress);
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
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

        NotificationCompat.Builder SocketServerServiceNotification = new NotificationCompat.Builder(this, "q")
                .setSmallIcon(R.drawable.message_24)
                .setContentTitle("SocketServerService")
                .setContentText("The service is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.back_24, "Back to the app", openAppPendingIntent)
                .setOngoing(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, SocketServerServiceNotification.build());
    }

}
