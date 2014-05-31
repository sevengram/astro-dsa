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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.dsa.R;
import com.mydeepsky.dsa.core.Generator;
import com.mydeepsky.dsa.data.CelestialObject;

public class ShowResultActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.mydeepsky.ShowResultActivity.MESSAGE";

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_show_result);

        ListView result = (ListView) findViewById(R.id.listview_result);
        if (getIntent().getBooleanExtra(MainActivity.ISLOAD, false)) {
            findViewById(R.id.button_save_list).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_remove_list).setVisibility(View.GONE);
        }

        List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();

        for (CelestialObject object : Generator.getInstance().result) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemTitle", object.showTitle());
            map.put("ItemText", object.showDescription());
            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.context, listItem,
                R.layout.result_line, new String[] { "ItemTitle", "ItemText" }, new int[] {
                        R.id.ItemTitle, R.id.ItemText });
        result.setAdapter(listItemAdapter);
        result.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(ShowResultActivity.this, ShowInfoActivity.class);
                intent.putExtra(EXTRA_MESSAGE, arg2);
                startActivity(intent);
            }
        });
    }

    public void saveList(View view) {
        String date = Generator.getInstance().getGeoInfo().getDateString();
        final EditText et = new EditText(this.context);
        et.setText(date);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
                .setTitle("Please input the filename").setIcon(android.R.drawable.ic_dialog_info)
                .setView(et).setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = "";
                        try {
                            String filename = Generator.getInstance().saveList(
                                    et.getText().toString());
                            message = "Saved as " + DirManager.getListPath() + File.separatorChar
                                    + filename;
                        } catch (IOException e) {
                            message = "Fail to save!";
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("Cancel", null);
        builder.show();

    }

    public void removeList(View view) {
        String filepath = getIntent().getStringExtra(MainActivity.FILENAME);
        String message = "";
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
            message = filepath + " removed.";
        } else {
            message = "File not found!";
        }
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }
}
