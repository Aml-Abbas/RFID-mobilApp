package com.axiell_exjobb.RFID_mobilapp;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

public class SocketServer extends WebSocketServer {

    private final String TAG = SocketServer.class.getSimpleName();
    private Collection<WebSocket> connections;

    public SocketServer(InetSocketAddress address) {
        super(address);
        this.connections = new ArrayList<>();
        Log.d(TAG, " the constructor ");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        conn.send("Welcome to the server!");
        broadcast("new connection: " + handshake.getResourceDescriptor());
        Log.d(TAG, "new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        Log.d(TAG, "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "received message from " + conn.getRemoteSocketAddress() + ": " + message);
        if (message.equals("ping")) {
            conn.send("echo");
        } else {
            JSONObject jsonObject = Utilities.stringToJson(message);
            Log.d(TAG, "git json: " + jsonObject);
            if (jsonObject != null) {
                String toDo = "";
                String value = "";
                try {
                    toDo = Utilities.getItemFromJson(jsonObject, "toDo");
                    value = Utilities.getItemFromJson(jsonObject, "value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (toDo) {
                    case "write":
                        MainActivity.setItemId(value);
                        break;
                    case "doCheckIn":
                        MainActivity.setDoCheckIn(value);
                        Log.d(TAG, "set docheck in " + value);
                        break;
                    case "doReadTagInfo":
                        MainActivity.setDoReadTagInfo(value);
                        Log.d(TAG, "set doReadTagInfo in " + value);
                        break;
                }
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn!=null){
            Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "server started successfully");
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        Log.d(TAG, "stop: called");
        super.stop();
    }
}