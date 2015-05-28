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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.mydeepsky.android.util.WebUtil;
import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.dsa.R;
import com.mydeepsky.dsa.core.Generator;
import com.mydeepsky.dsa.data.CelestialObject;

public class ShowInfoActivity extends Activity {
    public static final String INFO_MESSAGE = "com.mydeepsky.ShowInfoActivity.INFO_MESSAGE";

    private Context context;

    private CelestialObject object;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        context = this;

        Intent intent = getIntent();
        int index = intent.getIntExtra(ShowResultActivity.EXTRA_MESSAGE, -1);
        this.object = Generator.getInstance().result.get(index);

        showBasicInfo();
        showVisualInfo();
    }

    private void showBasicInfo() {
        ListView infoView = (ListView) findViewById(R.id.infoView);

        List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();

        String epoch = "" + Generator.getInstance().getGeoInfo().getYear() + ".0";

        String name = object.getCommonName();
        if (!name.equals("--"))
            listItem.add(makeMap("Name", name));

        String number = object.showNumberInfo();
        if (!number.equals("--"))
            listItem.add(makeMap("Catalog Numbers", number));

        String description = object.showDescription();
        if (!description.equals(""))
            listItem.add(makeMap("Description", description));

        listItem.add(makeMap("Visual Magnitude", "" + object.getMagInString()));
        listItem.add(makeMap("Apparent Size", "" + object.getSize()));
        listItem.add(makeMap("R.A. (" + epoch + ")", object.getRaInString()));
        listItem.add(makeMap("Dec. (" + epoch + ")", object.getDecInString()));

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.context, listItem,
                R.layout.info_line, new String[] { "InfoTitle", "InfoText" }, new int[] {
                        R.id.InfoTitle, R.id.InfoText });

        infoView.setAdapter(listItemAdapter);
    }

    private void showVisualInfo() {
        ListView visualView = (ListView) findViewById(R.id.VisualView);

        List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();

        double altitude = Generator.getInstance().getGeoInfo().getAltitude();

        listItem.add(makeMap("Rises (Alt>" + altitude + "°)",
                TimeMath.hourMinFormat(object.getRiseTime())));
        listItem.add(makeMap("Transits", TimeMath.hourMinFormat(object.getCulminationTime())));
        listItem.add(makeMap("Sets (Alt>" + altitude + "°)",
                TimeMath.hourMinFormat(object.getSetTime())));

        SimpleAdapter listItemAdapter = new SimpleAdapter(this.context, listItem,
                R.layout.info_line, new String[] { "InfoTitle", "InfoText" }, new int[] {
                        R.id.InfoTitle, R.id.InfoText });

        visualView.setAdapter(listItemAdapter);
    }

    private HashMap<String, Object> makeMap(String title, String text) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("InfoTitle", title);
        map.put("InfoText", text);
        return map;
    }
}
