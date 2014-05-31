package com.mydeepsky.dsa.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.mydeepsky.dsa.R;

public class DialogManager {
    public static Dialog createRefreshDialog(Context activity) {
        Dialog refreshDialog = new Dialog(activity, R.style.RefreshDialog);
        refreshDialog.setContentView(R.layout.refresh_view);
        return refreshDialog;
    }

    public static Dialog createProgressDialog(Context activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(R.string.title_wait);
        progressDialog.setMessage(activity.getText(R.string.text_processing));
        return progressDialog;
    }
}
