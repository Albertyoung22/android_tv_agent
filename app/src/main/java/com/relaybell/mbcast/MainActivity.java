package com.relaybell.mbcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView mWebView;
    private SharedPreferences mPrefs;
    private static final String PREF_NAME = "mbcast_tv_prefs";
    private static final String KEY_SERVER_IP = "server_ip";
    private static final String KEY_GROUP_ID = "group_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 全螢幕與防止休眠 (大電視常駐核心)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 沉浸式隱藏虛擬導航列與狀態列
        hideSystemUI();

        setContentView(R.layout.activity_main);

        mPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 2. 初始化 WebView
        mWebView = findViewById(R.id.webView);
        setupWebView();

        // 3. 載入推播接收頁面
        loadMediaPage();

        // 4. 啟動背景 UDP 心跳回報服務 (向老師主控台通報在線)
        startService(new Intent(this, UdpService.class));
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setupWebView() {
        mWebView.setBackgroundColor(Color.BLACK);
        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false); // 允許音訊與影片自動播放 (免遙控器確認)
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setSupportZoom(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // 斷線時提示並自動重試
                Toast.makeText(MainActivity.this, "連線主控伺服器中... (" + description + ")", Toast.LENGTH_SHORT).show();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient());
    }

    public void loadMediaPage() {
        String serverIp = mPrefs.getString(KEY_SERVER_IP, "");
        String groupId = mPrefs.getString(KEY_GROUP_ID, "1");

        if (serverIp.isEmpty()) {
            // 嘗試以預設子網路閘道或提示輸入
            serverIp = getLocalIpAddress();
            int lastDot = serverIp.lastIndexOf('.');
            if (lastDot > 0) {
                serverIp = serverIp.substring(0, lastDot) + ".1"; // 預設閘道猜測
            }
        }

        String targetUrl = "http://" + serverIp + ":5050/media";
        mWebView.loadUrl(targetUrl);
    }

    private String getLocalIpAddress() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        } catch (Exception e) {
            return "192.168.1.100";
        }
    }

    // 支援電視遙控器選單鍵設定伺服器 IP
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            showSettingsDialog();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 大電視按返回鍵重新整理
            mWebView.reload();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Mbcast 電視端設定");

        final EditText input = new EditText(this);
        input.setHint("請輸入主控端電腦 IP (例如 192.168.1.100)");
        input.setText(mPrefs.getString(KEY_SERVER_IP, ""));
        builder.setView(input);

        builder.setPositiveButton("儲存並連線", (dialog, which) -> {
            String newIp = input.getText().toString().trim();
            if (!newIp.isEmpty()) {
                mPrefs.edit().putString(KEY_SERVER_IP, newIp).apply();
                loadMediaPage();
                Toast.makeText(MainActivity.this, "已套用新 IP: " + newIp, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}
