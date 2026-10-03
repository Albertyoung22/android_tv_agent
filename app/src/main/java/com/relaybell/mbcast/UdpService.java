package com.relaybell.mbcast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.text.format.Formatter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Android TV 背景 UDP 服務：
 * 1. 定時向老師主控台廣播 HELLO 心跳包 (UDP 8080)，讓大電視自動出現在主控台名冊中。
 * 2. 監聽 UDP 8081 指令，支援遠端重整、開啟網頁等操作。
 */
public class UdpService extends Service {

    private boolean isRunning = false;
    private Thread heartbeatThread;
    private Thread cmdListenThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startHeartbeat();
            startCommandListener();
        }
        return START_STICKY;
    }

    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);

                while (isRunning) {
                    try {
                        String myIp = getLocalIpAddress();
                        String myName = Build.MODEL != null ? Build.MODEL : "大電視";
                        String clientId = "TV-" + (myIp.replace(".", "_"));

                        // 格式符合 controller_server HELLO 協議:
                        // HELLO|<client_id>|<hostname>|<ip>|<group>|<version>
                        String payload = "HELLO|" + clientId + "|" + myName + "|" + myIp + "|大電視|v2.3.0";
                        byte[] data = payload.getBytes(StandardCharsets.UTF_8);

                        // 發送全網廣播
                        DatagramPacket packet = new DatagramPacket(
                                data, data.length,
                                InetAddress.getByName("255.255.255.255"), 8080
                        );
                        socket.send(packet);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 每 3 秒發送一次心跳
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private void startCommandListener() {
        cmdListenThread = new Thread(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(8081);
                byte[] buffer = new byte[2048];

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    // 收到指令後可透過廣播通知 MainActivity 執行重整或導向
                    // 例如: CMD|TARGET=ALL|open_url|http://...
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
        cmdListenThread.setDaemon(true);
        cmdListenThread.start();
    }

    private String getLocalIpAddress() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        } catch (Exception e) {
            return "192.168.1.50";
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
