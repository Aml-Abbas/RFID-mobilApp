package com.example.rfid_mobilapp;

import android.util.Log;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class SocketServer extends WebSocketServer {

    private final String TAG = SocketServer.class.getSimpleName();

    public SocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        Log.d(TAG, "new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "received message from " + conn.getRemoteSocketAddress() + ": " + message);
        if (message.equals("ping")) {
            // "command" ping received, sending echo.
            conn.send("echo");
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "server started successfully");
    }


    public static void main(String[] args) {
        String host = "localhost";
        int port = 8887;

        WebSocketServer server = new SocketServer(new InetSocketAddress(host, port));
        server.run();
    }
}