package com.example.rfid_mobilapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.net.InetSocketAddress;

public class SocketServerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

}
