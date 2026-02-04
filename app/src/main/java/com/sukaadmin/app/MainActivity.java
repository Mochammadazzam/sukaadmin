package com.sukaadmin.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private WebView webView;
    private String url;
    
    @Override
    protected void onStop() {
        super.onStop();
        saveCookies();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Memanggil layout activity_main.xml
        setContentView(R.layout.activity_main);
        
        // Memuat file HTML dari folder assets
        url = "file:///android_asset/index.html";

        // Referensi ke ID WebView di XML
        webView = (WebView) findViewById(R.id.webview_compontent);

        // Menonaktifkan klik kanan/long click
        webView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);

        // Konfigurasi WebView
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        
        // Pengaturan wajib agar JavaScript dan Firebase jalan
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Manajemen Cookies
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.setAcceptFileSchemeCookies(true);

        restoreCookies();
        if (savedInstanceState == null) {
            webView.loadUrl(url);
        }

        // Simpan cookies secara berkala setiap 10 detik
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveCookies();
            }
        }, 0, 10000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(webView != null) webView.saveState(outState);
        saveCookies();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(webView != null) webView.restoreState(savedInstanceState);
    }

    private void saveCookies() {
        String cookies = CookieManager.getInstance().getCookie(url);
        SharedPreferences sp = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sp.edit();
        prefsEditor.putString("cookies", cookies);
        prefsEditor.apply();
    }
    
    private void restoreCookies() {
        SharedPreferences sp = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String cookies = sp.getString("cookies", "");
        CookieManager.getInstance().setCookie(url, cookies);
    }

    @Override
    public void onBackPressed() {
        // Jika webview bisa back, maka kembali ke halaman sebelumnya di web
        if(webView != null && webView.canGoBack()) {
            webView.goBack();
            saveCookies();
        } else {
            saveCookies();
            super.onBackPressed();
        }
    }

    // Client untuk menangani URL luar seperti WhatsApp dan Email
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Logika agar link WhatsApp (wa.me) terbuka di aplikasi WA langsung
            if (url.startsWith("https://wa.me/") || url.startsWith("whatsapp:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            
            // Logika untuk link Email (mailto)
            if (url.startsWith("mailto:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }
            
            view.loadUrl(url);
            return true;
        }
    }
}
