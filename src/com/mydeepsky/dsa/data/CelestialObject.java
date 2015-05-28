package com.mydeepsky.dsa.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.android.util.astro.TriMath;

public class CelestialObject {
    public static final int NEVER_VISIBLE = Integer.MIN_VALUE;
    public static final int ALWAYS_VISIBLE = Integer.MAX_VALUE;
    public static final double ALWAYS_VISIBLE_DOUBLE = 1024.0;
    public static final double NEVER_VISIBLE_DOUBLE = -1024.0;

    private String type;

    public enum Order {
        FIRST, SECOND, THIRD, NONE
    };

    private String ssid;
    private String number;
    private String ngc;

    private double ra; // Right ascension
    private double dec; // Declination

    private double mag;
    private String size;

    private String con;
    private String name;

    private String buf;

    private int riseTime;
    private int culminationTime;
    private int setTime;

    private Order order;

    private double riseSiderealTime;
    private double setSiderealTime;

    public String getSsId() {
        return ssid;
    }

    public CelestialObject(JSONObject jsonObject) {
        super();
        try {
            this.number = "--";
            this.ngc = jsonObject.getString("object");
            this.con = jsonObject.getString("con");
            this.mag = jsonObject.getDouble("mag");
            this.ra = jsonObject.getDouble("ra_value");
            this.dec = jsonObject.getDouble("dec_value");
            this.size = jsonObject.getString("size");
            this.type = jsonObject.getString("type");
            this.name = "--";
            this.buf = getInfo(jsonObject);
            this.ssid = getSsId(jsonObject);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getSsId(JSONObject jsonObject) {
        try {
            return jsonObject.getJSONObject("ssinfo").getString("ObjectID");
        } catch (JSONException e) {
            return "";
        }
    }

    private String getInfo(JSONObject jsonObject) {
        try {
            String info = "";
            JSONObject ssinfo = jsonObject.getJSONObject("ssinfo");
            info += "ObjectID=" + ssinfo.getString("ObjectID") + "\n";
            JSONArray commonName = ssinfo.getJSONArray("CommonName");
            for (int i = 0; i < commonName.length(); i++) {
                info += "CommonName=" + commonName.getString(i) + "\n";
            }
            JSONArray catalogNumber = ssinfo.getJSONArray("CatalogNumber");
            for (int i = 0; i < catalogNumber.length(); i++) {
                info += "CatalogNumber=" + catalogNumber.getString(i) + "\n";
            }
            return info;
        } catch (JSONException e) {
            return "";
        }
    }

    public CelestialObject(String number, String ngc, double ra, double dec, double mag,
            String size, String type, String con, String name) {
        super();
        this.number = number;
        this.ngc = ngc;
        this.ra = ra;
        this.dec = dec;
        this.mag = mag;
        this.size = size;
        this.con = con;
        this.name = name;
        this.buf = "";
        this.ssid = "";
        this.type = type;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public void setDec(double dec) {
        this.dec = dec;
    }

    public void setPosition(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }

    public void setTimes(String riseTime, String culminationTime, String setTime) {
        this.riseTime = TimeMath.getTimeFromSring(riseTime);
        this.culminationTime = TimeMath.getTimeFromSring(culminationTime);
        this.setTime = TimeMath.getTimeFromSring(setTime);
    }

    public String getBuf() {
        return buf;
    }

    public void setBuf(String buf) {
        this.buf = buf;
    }

    public int getRiseTime() {
        return riseTime;
    }

    public Order getOrder() {
        return order;
    }

    public int getCulminationTime() {
        return culminationTime;
    }

    public int getSetTime() {
        return setTime;
    }

    public String getNumber() {
        return number;
    }

    public String getNgc() {
        return ngc.replace("_", " ");
    }

    public String getNgcWithSpace() {
        Matcher m = Pattern.compile("[0-9]").matcher(ngc);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(ngc);
            sb.insert(m.start(), ' ');
            return sb.toString();
        } else {
            return ngc;
        }
    }

    public String getNgcWithLine() {
        Matcher m = Pattern.compile("[0-9]").matcher(ngc);
        if (m.find()) {
            StringBuilder sb = new StringBuilder(ngc);
            sb.insert(m.start(), '_');
            return sb.toString();
        } else {
            return ngc;
        }
    }

    public double getRa() {
        return ra;
    }

    public int getRaHr() {
        return (int) this.ra;
    }

    public int getRaMin() {
        return (int) ((this.ra - getRaHr()) * 60);
    }

    public String getRaInString() {
        int raHr = (int) this.ra;
        int raMin = (int) ((this.ra - raHr) * 60);
        return String.format("%02d", raHr) + "h " + String.format("%02d", raMin) + "m";
    }

    public String getDecInString() {
        int decDeg = (int) this.dec;
        int decMin = Math.abs((int) ((this.dec - decDeg) * 60));
        return String.format("%02d", decDeg) + "° " + String.format("%02d", decMin) + "'";
    }

    public int getDecDeg() {
        return (int) this.dec;
    }

    public int getDecMin() {
        return Math.abs((int) ((this.dec - getDecDeg()) * 60));
    }

    public double getRaRad() {
        return TriMath.hr2rad(ra);
    }

    public double getDec() {
        return dec;
    }

    public double getDecRad() {
        return TriMath.deg2rad(dec);
    }

    public double getMag() {
        return mag;
    }

    public String getMagInString() {
        return (mag > 0) ? "+" + mag : "" + mag;
    }

    public String getSize() {
        return size.replace("x", " x ");
    }

    public String getSizeWithoutSpace() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getCon() {
        return con;
    }

    public String getCommonName() {
        return name.replace('_', ' ');
    }

    public String getCommonNameWithLine() {
        return name;
    }

    public String showDescription() {
        String description = "";
        String typeString = getType();
        if (!typeString.equals("N/A")) {
            description += typeString + " ";
        }
        if (!con.equals("--")) {
            description += "in " + this.con;
        }
        return description;
    }

    public String showTitle() {
        String title = "";
        if (!getCommonName().equals("--")) {
            title += getCommonName() + " - ";
        }
        switch (getNumber().charAt(0)) {
        case 'M':
            title += "Messier " + this.number.substring(1);
            break;
        case 'C':
            title += "Caldwell " + this.number.substring(1);
            break;
        default:
            title += getNgcWithSpace();
        }
        return title;
    }

    public String showNumberInfo() {
        String result = getNgcWithSpace();

        switch (this.number.charAt(0)) {
        case 'M':
            if (result.equals("--"))
                result = "Messier " + this.number.substring(1);
            else
                result = "Messier " + this.number.substring(1) + '\n' + result;
            break;
        case 'C':
            if (result.equals("--"))
                result = "Caldwell " + this.number.substring(1);
            else
                result = "Caldwell " + this.number.substring(1) + '\n' + result;
            break;
        }
        return result;
    }

    public String showNumberInfoInOneline() {
        return showNumberInfo().replace("\n", " - ");
    }

    public String showFullDescription() {
        String result = "";

        String commonName = getCommonName();
        if (!commonName.equals("--"))
            result += commonName + " - ";

        String numberInfo = showNumberInfoInOneline();
        if (!numberInfo.equals("--"))
            result += numberInfo + " - ";

        String description = showDescription();
        result += description;

        return result;
    }

    public boolean isBrighter(double magLimit) {
        return mag <= magLimit;
    }

    public void setTimesAndOrders(GeoInfo geo) {
        setRiseAndSetSiderealTime(geo);

        double baseSiderealTime = geo.getsiderealTime();
        double siderealTimeLength = geo.getTimeLengthInSidereal();

        double deltaRiseSiderealTime = riseSiderealTime - baseSiderealTime;
        double deltaCulSiderealTime = ra - baseSiderealTime;
        double deltaSetSiderealTime = setSiderealTime - baseSiderealTime;

        if (riseSiderealTime >= ALWAYS_VISIBLE_DOUBLE) {
            order = Order.SECOND;

            riseTime = ALWAYS_VISIBLE;
            culminationTime = TimeMath.computeSolarTime(geo.getStartTime(), deltaCulSiderealTime);
            setTime = ALWAYS_VISIBLE;
            return;
        }

        if (riseSiderealTime <= NEVER_VISIBLE_DOUBLE) {
            order = Order.NONE;

            riseTime = NEVER_VISIBLE;
            culminationTime = TimeMath.computeSolarTime(geo.getStartTime(), deltaCulSiderealTime);
            setTime = NEVER_VISIBLE;
            return;
        }

        if (TimeMath.isInOrder(deltaRiseSiderealTime, deltaCulSiderealTime, deltaSetSiderealTime)) {
            if (deltaSetSiderealTime < 0.0) {
                deltaRiseSiderealTime += 24.0;
                deltaCulSiderealTime += 24.0;
                deltaSetSiderealTime += 24.0;
            }
        } else if (TimeMath.isInOrder(deltaSetSiderealTime, deltaRiseSiderealTime,
                deltaCulSiderealTime)) {
            if (deltaSetSiderealTime < 0.0) {
                deltaSetSiderealTime += 24.0;
            } else {
                deltaRiseSiderealTime -= 24.0;
                deltaCulSiderealTime -= 24.0;
            }
        } else if (TimeMath.isInOrder(deltaCulSiderealTime, deltaSetSiderealTime,
                deltaRiseSiderealTime)) {
            if (deltaSetSiderealTime > 0.0) {
                deltaRiseSiderealTime -= 24.0;
            } else {
                deltaCulSiderealTime += 24.0;
                deltaSetSiderealTime += 24.0;
            }
        }

        riseTime = TimeMath.computeSolarTime(geo.getStartTime(), deltaRiseSiderealTime);
        culminationTime = TimeMath.computeSolarTime(geo.getStartTime(), deltaCulSiderealTime);
        setTime = TimeMath.computeSolarTime(geo.getStartTime(), deltaSetSiderealTime);

        if (TimeMath.isInPeriod(0, siderealTimeLength, deltaRiseSiderealTime)
                || TimeMath.isInPeriod(0, siderealTimeLength, deltaSetSiderealTime)
                || TimeMath.isInPeriod(deltaRiseSiderealTime, deltaSetSiderealTime, 0)
                || TimeMath.isInPeriod(deltaRiseSiderealTime, deltaSetSiderealTime,
                        siderealTimeLength)) {

            if (TimeMath.isInPeriod(0, siderealTimeLength / 2.0, deltaSetSiderealTime))
                order = Order.FIRST;
            else if (TimeMath.isInPeriod(siderealTimeLength / 2.0, siderealTimeLength,
                    deltaRiseSiderealTime))
                order = Order.THIRD;
            else
                order = Order.SECOND;
        } else
            order = Order.NONE;
    }

    private void setRiseAndSetSiderealTime(GeoInfo geo) {
        double cosr = Math.tan(geo.getLatitudeRad()) * Math.tan(getDecRad());
        double angleFix = Math.sin(geo.getAltitudeRad())
                / (Math.cos(geo.getLatitudeRad()) * Math.cos(getDecRad()));
        cosr -= angleFix;

        if (cosr >= 1.0) {
            riseSiderealTime = setSiderealTime = ALWAYS_VISIBLE_DOUBLE * 2;
        } else if (cosr <= -1.0) {
            riseSiderealTime = setSiderealTime = NEVER_VISIBLE_DOUBLE * 2;
        } else {
            double rRad = Math.acos(cosr);
            double siderealTime = (1 - rRad / Math.PI) * 24.0;

            riseSiderealTime = ra - siderealTime / 2.0;
            setSiderealTime = ra + siderealTime / 2.0;

            if (riseSiderealTime < 0.0) {
                riseSiderealTime += 24.0;
            }
            if (setSiderealTime > 24.0) {
                setSiderealTime -= 24.0;
            }
        }
    }
}
