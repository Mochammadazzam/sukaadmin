package com.sukaadmin.app; // DISESUAIKAN dari com.yoursite.app

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Timer;
import java.util.TimerTask;

// Nama class diganti ke MainActivity agar standar dengan Manifest
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
        
        // Pastikan di folder res/layout ada file activity_main.xml
        setContentView(R.layout.activity_main);
        
        url = "file:///android_asset/index.html";

        // ID harus sama dengan yang ada di layout XML kamu
        webView = (WebView) findViewById(R.id.webview_compontent);

        webView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);

        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        
        // Setting wajib agar HTML/Firebase jalan
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.setAcceptFileSchemeCookies(true);

        restoreCookies();
        if (savedInstanceState == null) {
            webView.loadUrl(url);
        }

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
        prefsEditor.apply(); // pakai apply lebih efisien daripada commit
    }
    
    private void restoreCookies() {
        SharedPreferences sp = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String cookies = sp.getString("cookies", "");
        CookieManager.getInstance().setCookie(url, cookies);
    }

    @Override
    public void onBackPressed() {
        if(webView != null && webView.canGoBack()) {
            webView.goBack();
            saveCookies();
        } else {
            saveCookies();
            super.onBackPressed();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Logika untuk WhatsApp Link agar tidak error di WebView
            if (url.startsWith("https://wa.me/") || url.startsWith("whatsapp:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(url));
                startActivity(intent);
                return true;
            }
            
            if (url.startsWith("mailto:")) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("plain/text").putExtra(Intent.EXTRA_EMAIL, new String[] { url.replace("mailto:", "") });
                startActivity(i);
                return true;
            }
            view.loadUrl(url);
            return true;
        }
    }
}
