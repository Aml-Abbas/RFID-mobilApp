package com.example.rfid_mobilapp;

import android.util.Log;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

public class SocketServer extends WebSocketServer {

    private final String TAG = SocketServer.class.getSimpleName();
    SocketServerService socketServerService;

    public SocketServer(InetSocketAddress address, SocketServerService socketServerService) {
        super(address);
        this.socketServerService= socketServerService;
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
        }else {
            JSONObject jsonObject= Utilities.stringToJson(message);
            Log.d(TAG, "git json: "+ jsonObject);
            if (jsonObject!= null){
                String toDo="";
                String value="";
                try {
                    toDo= Utilities.getItemFromJson(jsonObject, "toDo");
                    value= Utilities.getItemFromJson(jsonObject, "value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (toDo){
                    case "write":
                        MainActivity.setItemId(value);
                        Log.d(TAG, "set item id: "+ value);
                        break;
                    case "doCheckIn":
                        MainActivity.setDoCheckIn(value);
                        Log.d(TAG, "set docheck in "+ value);
                        break;
                }
                socketServerService.openApp();
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn!= null){
            Log.d(TAG, "an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        }    }

    @Override
    public void onStart() {
        Log.d(TAG, "server started successfully");
    }


    public static void main(String[] args) {
        String host = "localhost";
        int port = 8887;

//        WebSocketServer server = new SocketServer(new InetSocketAddress(host, port));
 //       server.run();
    }
}