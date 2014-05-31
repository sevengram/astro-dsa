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

package com.mydeepsky.dsa.data;

import java.util.NoSuchElementException;

import com.mydeepsky.android.util.astro.Nutation;
import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.android.util.astro.TriMath;

public class GeoInfo {
    private int year;
    private int month;
    private int date;

    private double defaultJD;
    private double defaultEclipObliq;

    private double JD;
    private double eclipObliq;

    private double longitude;
    private double latitude;
    private double timezone;

    private int startTime;
    private int endTime;

    private double altitude;
    private double maglimit;

    private double siderealTime;

    public GeoInfo(int year, int month, int date, double longitude, double latitude,
            double timezone, int startTime, int endTime, double altitude, double maglimit) {
        super();
        this.year = year;
        this.month = month;
        this.date = date;

        this.longitude = longitude;
        this.latitude = latitude;
        this.timezone = timezone;

        this.startTime = startTime;
        this.endTime = endTime;

        this.altitude = altitude;
        this.maglimit = maglimit;

        this.JD = TimeMath.get_julian_day(year, month, date, startTime, timezone);

        Nutation nutation = new Nutation(this.JD);

        this.siderealTime = TriMath.range_degrees(TimeMath.get_greenwich_apparent_sidereal_time(
                nutation, this.JD) + longitude);

        this.eclipObliq = TriMath.deg2rad(nutation.getApparentEcliptic());
    }

    public double getEclipObliqDefault() {
        return defaultEclipObliq;
    }

    public double getEclipObliq() {
        return eclipObliq;
    }

    public String getDateString() {
        String date = String.format("%02d", this.getYear())
                + String.format("%02d", this.getMonth()) + String.format("%02d", this.getDate());
        return date;
    }

    public void setDefaultJD(String epoch) throws NoSuchElementException {
        if (epoch.charAt(0) == 'J') {
            double J = Double.parseDouble(epoch.substring(1, epoch.length()));
            this.defaultJD = (J - 2000.0) * 365.25 + 2451545.0;
        } else if (epoch.charAt(0) == 'B') {
            double B = Double.parseDouble(epoch.substring(1, epoch.length()));
            this.defaultJD = (B - 1900.0) * 365.242198781 + 2415020.31352;
        } else {
            throw new NoSuchElementException();
        }

        this.defaultEclipObliq = TriMath.deg2rad((new Nutation(this.defaultJD))
                .getApparentEcliptic());
    }

    public double getDefaultJD() {
        return defaultJD;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDate() {
        return date;
    }

    public double getsiderealTime() {
        return TriMath.deg2hr(siderealTime);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLatitudeRad() {
        return TriMath.deg2rad(latitude);
    }

    public double getTimezone() {
        return timezone;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public double getTimeLengthInSidereal() {
        double time = (endTime > startTime) ? (endTime - startTime) / 60.0
                : (endTime + 1440.0 - startTime) / 60.0;
        return TimeMath.solar2sidereal(time);
    }

    public double getAltitude() {
        return altitude;
    }

    public double getAltitudeRad() {
        return TriMath.deg2rad(altitude);
    }

    public double getMaglimit() {
        return maglimit;
    }

    public double getJD() {
        return JD;
    }

    @Override
    public String toString() {
        return String.format("%d %d %d %f %f %f %d %d %f %f", year, month, date, longitude,
                latitude, timezone, startTime, endTime, altitude, maglimit);
    }
}
