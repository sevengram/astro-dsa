/*
 * Deep Sky Assistant for Android
 * Author 2012 Jianxiang FAN <sevengram1991@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.mydeepsky.dsa.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.android.util.WebUtil;

public class WikiWebviewActivity extends WebviewActivity {
    private WifiStatusReceiver wifiStatusReceiver;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Makes Progress bar Visible
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        webview.getSettings().setJavaScriptEnabled(true);
        // webview.getSettings().setSupportZoom(true);
        // webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webview.loadUrl(getIntent().getStringExtra(ShowInfoActivity.INFO_MESSAGE));
        webview.setWebChromeClient(new SimpleWebChromeClient());
        webview.setWebViewClient(new WikiWebViewClient());

        // setZoomControlGone(webview);
        wifiStatusReceiver = new WifiStatusReceiver();
    }

    private class WikiWebViewClient extends SimpleWebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (WebUtil.isWikiUrl(url))
                return false;
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiStatusReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (wifiStatusReceiver != null)
            unregisterReceiver(wifiStatusReceiver);
    }

    class WifiStatusReceiver extends BroadcastReceiver {
        private boolean withWifi = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkManager.isWifiAvailable() && !withWifi) {
                    ((WebviewActivity) context).getWebview().getSettings()
                            .setBlockNetworkImage(false);
                    withWifi = true;
                } else if (!NetworkManager.isWifiAvailable() && withWifi) {
                    ((WebviewActivity) context).getWebview().getSettings()
                            .setBlockNetworkImage(true);
                    withWifi = false;
                    if (NetworkManager.isNetworkAvailable()) {
                        Toast.makeText(context, "Without wifi, image blocked.", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }
}
