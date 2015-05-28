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

package com.mydeepsky.dsa.core;

import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.android.util.astro.Position;
import com.mydeepsky.android.util.astro.Precession;
import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.dsa.data.CelestialObject;
import com.mydeepsky.dsa.data.CelestialObject.Order;
import com.mydeepsky.dsa.data.GeoInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.*;

public class Generator {

    private static Generator instance;
    private List<CelestialObject> celestialObjects = new ArrayList<>();
    public List<CelestialObject> result = new ArrayList<>();

    private GeoInfo geoInfo;

    public GeoInfo getGeoInfo() {
        return geoInfo;
    }

    public static synchronized Generator getInstance() {
        if (instance == null)
            instance = new Generator();
        return instance;
    }

    private Generator() {
    }

    public void setGeoInfo(int year, int month, int date, double longitude, double latitude,
        double timezone, int startTime, int endTime, double altitude, double maglimit) {
        this.geoInfo = new GeoInfo(year, month, date, longitude, latitude, timezone, startTime,
            endTime, altitude, maglimit);
    }

    public void readObject(JSONArray jsonArray) throws JSONException {
        celestialObjects.clear();
        result.clear();
        this.geoInfo.setDefaultJD("J2000.0");
        for (int i = 0; i < jsonArray.length(); i++) {
            celestialObjects.add(new CelestialObject(jsonArray.getJSONObject(i)));
        }
    }

    public void generateList() {
        List<CelestialObject> early = new ArrayList<>();
        List<CelestialObject> mid = new ArrayList<>();
        List<CelestialObject> late = new ArrayList<>();

        Comparator<CelestialObject> bySetTime = new Comparator<CelestialObject>() {
            public int compare(CelestialObject o1, CelestialObject o2) {
                return o1.getSetTime() - o2.getSetTime();
            }
        };

        Comparator<CelestialObject> byCulminationTime = new Comparator<CelestialObject>() {
            public int compare(CelestialObject o1, CelestialObject o2) {
                return o1.getCulminationTime() - o2.getCulminationTime();
            }
        };

        Comparator<CelestialObject> byRiseTime = new Comparator<CelestialObject>() {
            public int compare(CelestialObject o1, CelestialObject o2) {
                return o1.getRiseTime() - o2.getRiseTime();
            }
        };

        for (CelestialObject object : celestialObjects) {
            if (object.isBrighter(geoInfo.getMaglimit())) {
                Position position = Precession.fix(object.getRaRad(), object.getDecRad(),
                    geoInfo.getEclipObliqDefault(), geoInfo.getEclipObliq(),
                    geoInfo.getDefaultJD(), geoInfo.getJD());
                object.setPosition(position.ra, position.dec);
                object.setTimesAndOrders(geoInfo);
                if (object.getOrder() == Order.FIRST)
                    early.add(object);
                else if (object.getOrder() == Order.THIRD)
                    late.add(object);
                else if (object.getOrder() == Order.SECOND)
                    mid.add(object);
            }
        }

        Collections.sort(early, bySetTime);
        Collections.sort(mid, byCulminationTime);
        Collections.sort(late, byRiseTime);

        result.addAll(early);
        result.addAll(mid);
        result.addAll(late);
    }

    public String saveList(String filename) throws IOException {
        String listFile = filename + ".list";
        String skySafariFile = filename + ".skylist";

        printList(result, listFile);
        printSkySafariList(result, skySafariFile);
        return listFile;
    }

    private void printList(Collection<CelestialObject> objects, String filename) throws IOException {
        File out_file = new File(DirManager.getListPath() + File.separatorChar + filename);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(out_file)));

        String ew = (geoInfo.getLongitude() >= 0) ? "E" : "W";
        String ns = (geoInfo.getLatitude() >= 0) ? "N" : "S";
        String pn = (geoInfo.getTimezone() >= 0) ? "UTC+" : "UTC";

        bw.write("Generated by Deep Sky Assistant\n"
            + "===============================================\n");

        bw.write("Location: " + String.format("%.3f", Math.abs(geoInfo.getLongitude())) + ew + " "
            + String.format("%.3f", Math.abs(geoInfo.getLatitude())) + ns + " " + pn
            + geoInfo.getTimezone() + "\n");

        bw.write("Date: " + geoInfo.getYear() + "/" + geoInfo.getMonth() + "/" + geoInfo.getDate()
            + "\n");

        bw.write("Time: " + TimeMath.hourMinFormat(geoInfo.getStartTime()) + " - "
            + TimeMath.hourMinFormat(geoInfo.getEndTime()) + "\n");

        bw.write("Altitude: >" + String.format("%.1f", geoInfo.getAltitude()) + "\n");
        bw.write("Magnitude: <" + String.format("%.1f", geoInfo.getMaglimit()) + "\n");

        bw.write("==========================================\n");
        bw.write("R - Rise time (Alt>" + geoInfo.getAltitude() + ")\n" + "M - Meridian time\n"
            + "S - Set time (Alt<" + geoInfo.getAltitude() + ")\n\n");

        bw.write("Geoinfo\n>>>>>======================================"
            + "==========================================="
            + "===========================================\n");
        bw.write(geoInfo.toString() + "\n\n");

        String str = String.format("%-8s ", "No.") + String.format("%-15s ", "NGC")
            + String.format("%-12s ", "R.A.") + String.format("%-13s ", "Dec.")
            + String.format("%-8s ", "Mag.") + String.format("%-12s ", "Size")
            + String.format("%-9s ", "Typ") + String.format("%-6s ", "Con")
            + String.format("%-8s ", "R") + String.format("%-8s ", "M")
            + String.format("%-8s ", "S") + "Common Name\n";
        bw.write(str);
        bw.write(">>>>>======================================"
            + "==========================================="
            + "===========================================\n");
        bw.flush();

        for (CelestialObject object : objects) {
            String outString = String.format("%-8s ", object.getNumber())
                + String.format("%-15s ", object.getNgc())
                + String.format("%-3d ", object.getRaHr())
                + String.format("%-7d ", object.getRaMin())
                + String.format("%-4d ", object.getDecDeg())
                + String.format("%-7d ", object.getDecMin())
                + String.format("%-8.1f ", object.getMag())
                + String.format("%-12s ", object.getSizeWithoutSpace())
                + String.format("%-9s ", object.getType())
                + String.format("%-6s ", object.getCon())
                + String.format("%-8s ", TimeMath.hourMinFormat(object.getRiseTime()))
                + String.format("%-8s ", TimeMath.hourMinFormat(object.getCulminationTime()))
                + String.format("%-8s ", TimeMath.hourMinFormat(object.getSetTime()))
                + object.getCommonNameWithLine() + "\n";
            bw.write(outString);
            bw.flush();
        }
        bw.close();
    }

    private void printSkySafariList(Collection<CelestialObject> objects, String filename)
        throws IOException {
        Collection<String> idset = new HashSet<>();

        File path = new File(DirManager.SD_PATH + "/SkySafari 4 Pro/Observing Lists");
        if (!path.exists()) {
            path.mkdirs();
        }

        File out_file = new File(DirManager.SD_PATH + "/SkySafari 4 Pro/Observing Lists/" + filename);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(out_file)));
        bw.write("SkySafariObservingListVersion=3.0\n");
        for (CelestialObject object : objects) {
            if (!object.getBuf().equals("")) {
                String ssid = object.getSsId();
                if (ssid.equals("") || !idset.contains(ssid)) {
                    bw.write("SkyObject=BeginObject\n");
                    bw.write(object.getBuf());
                    bw.write("EndObject=SkyObject\n");
                    idset.add(ssid);
                }
            }
        }
        bw.close();
    }

    public void readObjectFromResult(String filename) throws IOException, NoSuchElementException,
        NumberFormatException {
        celestialObjects.clear();
        result.clear();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String lineString;
        String[] temp;

        while ((lineString = br.readLine()) != null) {
            if (lineString.startsWith(">>>>>"))
                break;
        }

        lineString = br.readLine();
        temp = lineString.split("\\s+");

        if (temp.length < 10) {
            br.close();
            throw new NoSuchElementException();
        }
        setGeoInfo(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]),
            Double.parseDouble(temp[3]), Double.parseDouble(temp[4]),
            Double.parseDouble(temp[5]), Integer.parseInt(temp[6]), Integer.parseInt(temp[7]),
            Double.parseDouble(temp[8]), Double.parseDouble(temp[9]));

        while ((lineString = br.readLine()) != null) {
            if (lineString.startsWith(">>>>>"))
                break;
        }

        while ((lineString = br.readLine()) != null) {
            if (!lineString.equals("")) {
                temp = lineString.split("\\s+");

                if (temp.length < 14) { // Must with 14 columns
                    br.close();
                    throw new NoSuchElementException();
                }

                String number = temp[0];
                String ngc = temp[1];

                for (int i = 2; i <= 6; i++) {
                    if (temp[i].startsWith("+"))
                        temp[i] = temp[i].substring(1);
                }

                int raHr = Integer.parseInt(temp[2]);
                int raMin = Integer.parseInt(temp[3]);
                int decDegree = Integer.parseInt(temp[4]);
                int decMin = Integer.parseInt(temp[5]);

                double mag = Double.parseDouble(temp[6]);
                String size = temp[7];
                String type = temp[8];
                String con = temp[9];

                String riseTime = temp[10];
                String culminationTime = temp[11];
                String setTime = temp[12];

                String name = temp[13];

                double ra = raHr + (double) raMin / 60;
                double dec = (decDegree > 0) ? (decDegree + (double) decMin / 60)
                    : (decDegree - (double) decMin / 60);

                CelestialObject object = new CelestialObject(number, ngc, ra, dec, mag, size, type,
                    con, name);
                object.setTimes(riseTime, culminationTime, setTime);

                result.add(object);
            }
        }
        br.close();
    }
}
