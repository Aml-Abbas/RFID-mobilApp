package com.example.rfid_mobilapp;
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
    SocketServerService socketServerService;
    private Collection<WebSocket> connections;

    public SocketServer(InetSocketAddress address, SocketServerService socketServerService) {
        super(address);
        this.connections = new ArrayList<>();
        this.socketServerService= socketServerService;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
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
                        break;
                    case "doCheckIn":
                        MainActivity.setDoCheckIn(value);
                        Log.d(TAG, "set docheck in "+ value);
                        break;
                }
                //socketServerService.openApp();
            }
        }
    }
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "received ByteBuffer from " + conn.getRemoteSocketAddress());
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
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8887;
//        WebSocketServer server = new SocketServer(new InetSocketAddress(host, port));
        //       server.run();
    }

    public void sendToAll(String text) {
        for (WebSocket c : connections) {
            c.send("Message from service: "+text);
        }
    }
}