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

import java.io.IOException;
import java.lang.reflect.Field;

import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

import com.mydeepsky.android.util.FileUtil;
import com.mydeepsky.android.util.ImageUtil;
import com.mydeepsky.dsa.R;
import com.mydeepsky.dsa.ui.dialog.DialogManager;

public abstract class WebviewActivity extends Activity {
    private static final String MENU_MESSAGE = "HitTestResult";

    protected Context context;

    protected WebView webview;

    protected boolean pageError = false;

    protected int backgroundColor = 0;

    private Dialog refreshDialog;

    private Bitmap bitmap;

    protected static final int NOMAL_BACKGROUND = R.color.black;

    protected static final int ERROR_BACKGROUND = R.color.white;

    public WebView getWebview() {
        return webview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        // Adds Progrss bar Support
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_show_description);

        webview = (WebView) findViewById(R.id.description_webview);
        refreshDialog = DialogManager.createRefreshDialog(context);

        webview.setOnCreateContextMenuListener(imageLongClickListener);
    }

    protected class SimpleWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ((Activity) context).setProgress(newProgress * 100);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            ((Activity) context).setTitle(title);
            super.onReceivedTitle(view, title);
        }
    }

    protected class SimpleWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            pageError = true;
            if (backgroundColor != ERROR_BACKGROUND) {
                view.setBackgroundColor(getResources().getColor(ERROR_BACKGROUND));
                backgroundColor = ERROR_BACKGROUND;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!pageError && backgroundColor != NOMAL_BACKGROUND) {
                view.setBackgroundColor(getResources().getColor(NOMAL_BACKGROUND));
                backgroundColor = NOMAL_BACKGROUND;
            }
            pageError = false;
        }
    }

    protected void setZoomControlGone(View view) {
        try {
            Class<WebView> classType = WebView.class;
            Field field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
            field.set(view, mZoomButtonsController);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected OnCreateContextMenuListener imageLongClickListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
            HitTestResult result = ((WebView) v).getHitTestResult();
            int type = result.getType();
            String url = result.getExtra();

            if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                Intent intent = new Intent();
                intent.putExtra(MENU_MESSAGE, url);

                String[] menuTitle = WebviewActivity.this.getResources().getStringArray(
                        R.array.image_long_click_event);

                for (int i = 0; i < menuTitle.length; i++) {
                    menu.add(Menu.NONE, i, Menu.NONE, menuTitle[i])
                            .setOnMenuItemClickListener(imageLongClickHandler).setIntent(intent);
                }
                menu.setHeaderTitle(FileUtil.parseFilename(url));
            }
        }
    };

    protected OnMenuItemClickListener imageLongClickHandler = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = item.getIntent();
            String url = intent.getStringExtra(MENU_MESSAGE);
            switch (item.getItemId()) {
            case 0:
                webview.loadUrl(url);
                break;
            case 1:
                break;
            case 2:
                setWallpaper(url);
                break;
            }
            return true;
        }
    };

    private void setWallpaper(String url) {
        refreshDialog.show();
        new setWallpaperThread(url).start();
    }

    private class setWallpaperThread extends Thread {
        private String url;

        public setWallpaperThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                bitmap = ImageUtil.loadImageFromUrl(url);
                setWallpaperHandler.sendEmptyMessage(0);
            } catch (IOException e) {
                setWallpaperHandler.sendEmptyMessage(-1);
            }
        }
    }

    private Handler setWallpaperHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refreshDialog.dismiss();
            if (msg.what == 0)
                try {
                    WallpaperManager.getInstance(context).setBitmap(bitmap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            else
                Toast.makeText(context, "Fail to download!", Toast.LENGTH_SHORT).show();
        }
    };

    public void onClickBack(View v) {
        if (webview.canGoBack())
            webview.goBack();
        else
            finish();
    }

    public void onClickForward(View v) {
        if (webview.canGoForward())
            webview.goForward();
    }

    public void onClickRefresh(View v) {
        webview.reload();
    }
}
