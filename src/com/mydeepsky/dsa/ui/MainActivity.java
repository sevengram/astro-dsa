package com.mydeepsky.dsa.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.mydeepsky.android.location.Locator.LocationInfo;
import com.mydeepsky.android.location.LocatorActivity;
import com.mydeepsky.android.task.Task.OnTaskListener;
import com.mydeepsky.android.task.*;
import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.android.util.EncryptUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.dsa.R;
import com.mydeepsky.dsa.core.AnswerTask;
import com.mydeepsky.dsa.core.Generator;
import com.mydeepsky.dsa.ui.dialog.DialogManager;
import com.mydeepsky.dsa.ui.dialog.TimeDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

public class MainActivity extends LocatorActivity {
    public final static String FILENAME = "com.mydeepsky.ui.MainActivity.FILENAME";
    public final static String ISLOAD = "com.mydeepsky.ui.MainActivity.IS_LOAD";

    private static final int SHOW_DATEPICK = 90;
    private static final int SHOW_STARTTIME_PICK = 91;
    private static final int SHOW_ENDTIME_PICK = 92;

    private static final int DEFAULT_END_HOUR = 4;
    private static final int DEFAULT_END_MIN = 0;
    private static final double DEFAULT_LONGITUDE = 121.436;
    private static final double DEFAULT_LATITUDE = 31.173;

    private static final double DEFAULT_MAGNITUDE = 11.0;
    private static final double MAGNITUDE_MIN = 5.0;
    private static final double MAGNITUDE_MAX = 15.0;

    private static final double DEFAULT_ALTITUDE = 15.0;

    private String resultMessage;

    private Dialog progressDialog;

    private EditText showDate;
    private Button pickDate;
    private EditText showStartTime;
    private Button pickStartTime;
    private EditText showEndTime;
    private Button pickEndTime;

    private EditText edit_longitude;
    private EditText edit_latitude;
    private EditText edit_timezone;

    private SeekBar seekBar_magnitude;
    private TextView textView_magnitude;

    private SeekBar seekBar_altitude;
    private TextView textView_altitude;

    private CheckBox checkbox_gcl;
    private CheckBox checkbox_ocl;
    private CheckBox checkbox_glx;
    private CheckBox checkbox_dbl;
    private CheckBox checkbox_brn;
    private CheckBox checkbox_pln;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mStartHour;
    private int mStartMinute;
    private int mEndHour;
    private int mEndMinute;

    private double mMagnitude;
    private double mAltitude;

    private int mLongtitudeSign;
    private int mLatitudeSign;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_main);
        initializeViews();
        setDefaultDate();
        setDefaultTime();
        setDefaultLocation();
        setDefaultMagnitude();
        setDefaultAltitude();
        setDefaultTimeZone();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "About").setIcon(
            android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = "Deep Sky Assistant";
        String message = "Deep Sky Assistant";
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
                    .setTitle(title).setMessage(message)
                    .setPositiveButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create();
                dialog.show();
                break;
            default:
                break;
        }
        return false;
    }

    /* Set Default */
    private void setDefaultDate() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH) + 1;
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDateDisplay();
    }

    private void setDefaultTimeZone() {
        edit_timezone.setText((double) TimeZone.getDefault().getRawOffset() / 1000 / 3600 + "");
    }

    private void setDefaultMagnitude() {
        mMagnitude = DEFAULT_MAGNITUDE;

        int process = (int) ((DEFAULT_MAGNITUDE - MAGNITUDE_MIN) * 100 / (MAGNITUDE_MAX - MAGNITUDE_MIN));
        seekBar_magnitude.setProgress(process);
        updateMagnitudeText();
    }

    private void setDefaultAltitude() {
        mAltitude = DEFAULT_ALTITUDE;

        seekBar_altitude.setProgress((int) mAltitude);
        updateAltitudeText();
    }

    private void setDefaultLocation() {
        edit_longitude.setText("" + DEFAULT_LONGITUDE);
        edit_latitude.setText("" + DEFAULT_LATITUDE);
        mLongtitudeSign = 1;
        mLatitudeSign = 1;
    }

    private void setDefaultTime() {
        final Calendar c = Calendar.getInstance();
        mStartHour = c.get(Calendar.HOUR_OF_DAY);
        mStartMinute = c.get(Calendar.MINUTE);
        mEndHour = DEFAULT_END_HOUR;
        mEndMinute = DEFAULT_END_MIN;
        updateTimeDisplay();
    }

    /* Update Display */
    private void updateDateDisplay() {
        showDate.setText(new StringBuilder().append(mYear).append("-")
            .append(mMonth < 10 ? "0" + mMonth : mMonth).append("-")
            .append((mDay < 10) ? "0" + mDay : mDay));
    }

    private void updateMagnitudeText() {
        textView_magnitude.setText("Magnitude Limit: " + mMagnitude);
    }

    private void updateAltitudeText() {
        textView_altitude.setText("Altitude: > " + mAltitude + "°");
    }

    private void updateTimeDisplay() {
        int startTime = mStartHour * 60 + mStartMinute;
        int endTime = mEndHour * 60 + mEndMinute;
        String nextDay = "";

        if (startTime >= endTime) {
            nextDay = " (next day)";
        }

        showStartTime.setText(new StringBuilder()
            .append(mStartHour < 10 ? "0" + mStartHour : mStartHour).append(":")
            .append((mStartMinute < 10) ? "0" + mStartMinute : mStartMinute));
        showEndTime.setText(new StringBuilder().append(mEndHour < 10 ? "0" + mEndHour : mEndHour)
            .append(":").append((mEndMinute < 10) ? "0" + mEndMinute : mEndMinute)
            .append(nextDay));
    }

    private void initializeViews() {
        showDate = (EditText) findViewById(R.id.show_date);
        pickDate = (Button) findViewById(R.id.pick_date);

        showStartTime = (EditText) findViewById(R.id.show_starttime);
        pickStartTime = (Button) findViewById(R.id.pick_starttime);
        showEndTime = (EditText) findViewById(R.id.show_endtime);
        pickEndTime = (Button) findViewById(R.id.pick_endtime);

        edit_longitude = (EditText) findViewById(R.id.edit_longitude);
        edit_latitude = (EditText) findViewById(R.id.edit_latitude);

        seekBar_magnitude = (SeekBar) findViewById(R.id.seekBar_magnitude);
        textView_magnitude = (TextView) findViewById(R.id.textview_magnitude);

        seekBar_altitude = (SeekBar) findViewById(R.id.seekBar_altitude);
        textView_altitude = (TextView) findViewById(R.id.textview_altitude);

        RadioGroup radioGroupEw = (RadioGroup) findViewById(R.id.radioGroup_longitude);
        RadioGroup radioGroupNs = (RadioGroup) findViewById(R.id.radioGroup_latitude);

        edit_timezone = (EditText) findViewById(R.id.edit_timezone);

        checkbox_gcl = (CheckBox) findViewById(R.id.checkbox_gcl);
        checkbox_ocl = (CheckBox) findViewById(R.id.checkbox_ocl);
        checkbox_glx = (CheckBox) findViewById(R.id.checkbox_glx);
        checkbox_dbl = (CheckBox) findViewById(R.id.checkbox_dbl);
        checkbox_brn = (CheckBox) findViewById(R.id.checkbox_brn);
        checkbox_pln = (CheckBox) findViewById(R.id.checkbox_pln);

        progressDialog = DialogManager.createProgressDialog(context);

        showDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickDate.performClick();
            }
        });

        showStartTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickStartTime.performClick();
            }
        });

        showEndTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickEndTime.performClick();
            }
        });

        radioGroupEw.setOnCheckedChangeListener(radioGroupEWListener);
        radioGroupNs.setOnCheckedChangeListener(radioGroupNSListener);
        seekBar_magnitude.setOnSeekBarChangeListener(seekBarMagnitudeListener);
        seekBar_altitude.setOnSeekBarChangeListener(seekBarAltitudeListener);
        edit_longitude.setOnFocusChangeListener(editLongitudeListener);
        edit_latitude.setOnFocusChangeListener(editLatitudeListener);
        edit_timezone.setOnFocusChangeListener(editTimezoneListener);
    }

    private RadioGroup.OnCheckedChangeListener radioGroupEWListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            mLongtitudeSign = (-1) * mLongtitudeSign;
        }
    };

    private RadioGroup.OnCheckedChangeListener radioGroupNSListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            mLatitudeSign = (-1) * mLatitudeSign;
        }
    };

    private OnSeekBarChangeListener seekBarMagnitudeListener = new OnSeekBarChangeListener() {
        public void onStopTrackingTouch(SeekBar arg0) {
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
            mMagnitude = MAGNITUDE_MIN + (double) process / 10.0;
            updateMagnitudeText();
        }
    };

    private OnSeekBarChangeListener seekBarAltitudeListener = new OnSeekBarChangeListener() {
        public void onStopTrackingTouch(SeekBar arg0) {
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
            mAltitude = (double) process;
            updateAltitudeText();
        }
    };

    private OnFocusChangeListener editTimezoneListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                try {
                    double timezone = Double.parseDouble(edit_timezone.getText().toString());
                    if (timezone > 14.0) {
                        edit_timezone.setText("14.0");
                    } else if (timezone < -12.0) {
                        edit_timezone.setText("-12.0");
                    }
                } catch (NumberFormatException e) {
                    edit_timezone.setText("0.0");
                }
            }
        }
    };

    private OnFocusChangeListener editLongitudeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                try {
                    if (Double.parseDouble(edit_longitude.getText().toString()) > 180) {
                        edit_longitude.setText("180.0");
                    }
                } catch (NumberFormatException e) {
                    edit_longitude.setText("0.0");
                }
            }
        }
    };

    private OnFocusChangeListener editLatitudeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                try {
                    if (Double.parseDouble(edit_latitude.getText().toString()) > 90) {
                        edit_latitude.setText("90.0");
                    }
                } catch (NumberFormatException e) {
                    edit_latitude.setText("0.0");
                }
            }
        }
    };

    public void onClickPickEndTime(View v) {
        DialogFragment newFragment = new TimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", SHOW_ENDTIME_PICK);
        args.putInt("hour", mEndHour);
        args.putInt("min", mEndMinute);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void onClickPickStartTime(View v) {
        DialogFragment newFragment = new TimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", SHOW_STARTTIME_PICK);
        args.putInt("hour", mStartHour);
        args.putInt("min", mStartMinute);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void onClickPickDate(View v) {
        DialogFragment newFragment = new TimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", SHOW_DATEPICK);
        args.putInt("year", mYear);
        args.putInt("month", mMonth);
        args.putInt("date", mDay);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void onClickGenerate(View view) {
        progressDialog.show();
        TaskContext taskContext = new TaskContext();
        String latitude_string = edit_latitude.getText().toString();
        if (latitude_string.equals("")) {
            latitude_string = "0.0";
        }
        double latitude = Math.min(Double.parseDouble(latitude_string) * mLatitudeSign, 90.0);

        Collection<String> types = new ArrayList<>();
        if (checkbox_brn.isChecked()) {
            types.add("BRTNB");
            types.add("CL+NB");
            types.add("LMCDN");
            types.add("SMCDN");
            types.add("LMCCN");
            types.add("SMCCN");
            types.add("SNREM");
        }
        if (checkbox_dbl.isChecked()) {
            types.add("ASTER");
            types.add("2STAR");
            types.add("3STAR");
            types.add("QUASR");
        }
        if (checkbox_gcl.isChecked()) {
            types.add("GLOCL");
            types.add("LMCGC");
            types.add("SMCGC");
            types.add("GX+GC");
        }
        if (checkbox_glx.isChecked()) {
            types.add("GALXY");
            types.add("GALCL");
            types.add("GX+DN");
            types.add("G+C+N");
        }
        if (checkbox_ocl.isChecked()) {
            types.add("OPNCL");
            types.add("SMCOC");
            types.add("LMCOC");
            types.add("CL+NB");
        }
        if (checkbox_pln.isChecked()) {
            types.add("PLNNB");
        }
        try {
            JSONObject json = new JSONObject();
            JSONObject param = new JSONObject();
            param.put("user", "fjx").put("lat", latitude).put("mag", mMagnitude)
                .put("type", new JSONArray(types));
            json.put("type", "query").put("param", param);
            taskContext.set(AnswerTask.URL, ConfigUtil.getString(Keys.HTTP_REAL_SERVER));
            taskContext.set(AnswerTask.REQUEST, json.toString().getBytes());
            AnswerTask task = new AnswerTask();
            task.addTaskListener(new WeakReference<>(generateTaskListener));
            task.execute(taskContext);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(context, "JSON Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private OnTaskListener generateTaskListener = new OnTaskListener() {

        @Override
        public void onTimeout(Object sender, TaskTimeoutEvent event) {
            Log.d("deepsky", event.getMessage());
            progressDialog.dismiss();
            Toast.makeText(context, "Time out", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFinished(Object sender, TaskFinishedEvent event) {
            Log.d("deepsky", "Task finished");
            String response = (String) event.getContext().get(AnswerTask.RESULT);
            try {
                JSONArray data = new JSONArray(response);
                int result = startCalculating(data);
                progressDialog.dismiss();
                if (result == 0) {
                    Intent intent = new Intent(context, ShowResultActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                if (progressDialog != null)
                    progressDialog.dismiss();
                Toast.makeText(context, "JSON Error!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onTaskFailed(Object sender, TaskFailedEvent event) {
            Log.d("deepsky", event.getMessage());
            progressDialog.dismiss();
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskCancel(Object sender, TaskCancelEvent event) {
            // TODO Auto-generated method stub

        }
    };

    private int startCalculating(JSONArray jsonArray) {
        try {
            String longitude_string = edit_longitude.getText().toString();
            if (longitude_string.equals("")) {
                longitude_string = "0.0";
            }
            double longitude = Math.min(Double.parseDouble(longitude_string) * mLongtitudeSign,
                180.0);

            String latitude_string = edit_latitude.getText().toString();
            if (latitude_string.equals("")) {
                latitude_string = "0.0";
            }
            double latitude = Math.min(Double.parseDouble(latitude_string) * mLatitudeSign, 90.0);

            String timezone_string = edit_timezone.getText().toString();
            if (timezone_string.equals("")) {
                timezone_string = "0.0";
            }
            double timezone = Math.min(Double.parseDouble(timezone_string), 14.0);
            timezone = Math.max(timezone, -12.0);

            int startTime = mStartHour * 60 + mStartMinute;
            int endTime = mEndHour * 60 + mEndMinute;

            Generator.getInstance().setGeoInfo(mYear, mMonth, mDay, longitude, latitude, timezone,
                startTime, endTime, mAltitude, mMagnitude);
            Generator.getInstance().readObject(jsonArray);
            Generator.getInstance().generateList();

            resultMessage = "";
            return 0;
        } catch (JSONException e) {
            resultMessage = "JSON error!";
            return -1;
        }
    }

    public void onClickLoad(View view) {
        final String listDir = DirManager.getListPath();
        File dataPath = new File(listDir);
        if (dataPath.exists()) {
            final String[] files = dataPath.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase(Locale.getDefault()).endsWith(".list");
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Load").setIcon(android.R.drawable.ic_dialog_info);
            builder.setItems(files, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filepath = listDir + File.separatorChar + files[which];
                    try {
                        Generator.getInstance().readObjectFromResult(filepath);
                        Intent intent = new Intent(context, ShowResultActivity.class);
                        intent.putExtra(FILENAME, filepath);
                        intent.putExtra(ISLOAD, true);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "File broken.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
        } else {
            Toast.makeText(this.context, "No saved files.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickUpload(View view) {
        final String skylistDir = DirManager.SD_PATH + "/SkySafari 4 Pro/Observing Lists";
        File dataPath = new File(skylistDir);
        if (dataPath.exists()) {
            final String[] files = dataPath.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase(Locale.getDefault()).endsWith(".skylist");
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Upload").setIcon(android.R.drawable.ic_dialog_info);
            builder.setItems(files, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog.show();
                    try {
                        TaskContext taskContext = new TaskContext();
                        JSONObject json = new JSONObject();
                        JSONObject param = new JSONObject();
                        JSONArray[] records = readSkylistRecord(skylistDir + "/" + files[which]);
                        param.put("user", "fjx").put("records", records[0])
                            .put("remains", records[1]);
                        json.put("type", "upload").put("param", param);
                        taskContext.set(AnswerTask.URL, ConfigUtil.getString(Keys.HTTP_REAL_SERVER));
                        taskContext.set(AnswerTask.REQUEST, json.toString().getBytes());

                        AnswerTask task = new AnswerTask();
                        task.addTaskListener(new WeakReference<>(uploadTaskListener));
                        task.execute(taskContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (progressDialog != null)
                            progressDialog.dismiss();
                        Toast.makeText(context, "Fail to upload!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
        } else {
            Toast.makeText(this.context, "No saved files.", Toast.LENGTH_SHORT).show();
        }
    }

    private OnTaskListener uploadTaskListener = new OnTaskListener() {

        @Override
        public void onTimeout(Object sender, TaskTimeoutEvent event) {
            Log.d("deepsky", event.getMessage());
            progressDialog.dismiss();
            Toast.makeText(context, "Time out", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFinished(Object sender, TaskFinishedEvent event) {
            Log.d("deepsky", "Task finished");
            String response = (String) event.getContext().get(AnswerTask.RESULT);
            try {
                JSONArray data = new JSONArray(response);
                int result = startCalculating(data);
                progressDialog.dismiss();
                if (result == 0) {
                    Intent intent = new Intent(context, ShowResultActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                if (progressDialog != null)
                    progressDialog.dismiss();
                Toast.makeText(context, "JSON Error!", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }

        @Override
        public void onTaskFailed(Object sender, TaskFailedEvent event) {
            Log.d("deepsky", event.getMessage());
            progressDialog.dismiss();
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskCancel(Object sender, TaskCancelEvent event) {
            // TODO Auto-generated method stub

        }
    };

    private OnDateSetListener onDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear + 1;
            mDay = dayOfMonth;
            updateDateDisplay();
        }
    };

    private OnTimeSetListener onStartTimeSetListener = new OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mStartHour = hourOfDay;
            mStartMinute = minute;
            updateTimeDisplay();
        }
    };

    private OnTimeSetListener onEndTimeSetListener = new OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mEndHour = hourOfDay;
            mEndMinute = minute;
            updateTimeDisplay();
        }
    };

    public OnDateSetListener getOnDateSetListener() {
        return onDateSetListener;
    }

    public OnTimeSetListener getOnStartTimeSetListener() {
        return onStartTimeSetListener;
    }

    public OnTimeSetListener getOnEndTimeSetListener() {
        return onEndTimeSetListener;
    }

    @Override
    public void onLocationUpdate(LocationInfo location, long costTime, int locator) {
        if (location == null) {
            return;
        }
        if (location.getLongitude() > 0) {
            ((RadioButton) findViewById(R.id.radio_east)).setChecked(true);
            mLongtitudeSign = 1;
        } else {
            ((RadioButton) findViewById(R.id.radio_west)).setChecked(true);
            mLongtitudeSign = -1;
        }
        if (location.getLatitude() > 0) {
            ((RadioButton) findViewById(R.id.radio_north)).setChecked(true);
            mLatitudeSign = 1;
        } else {
            ((RadioButton) findViewById(R.id.radio_south)).setChecked(true);
            mLatitudeSign = -1;
        }
        edit_longitude.setText("" + (location.getLongitude() * mLongtitudeSign));
        edit_latitude.setText("" + (location.getLatitude() * mLatitudeSign));
        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show();
    }

    private JSONArray[] readSkylistRecord(String filename) throws IOException, JSONException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        JSONArray[] result = new JSONArray[2];
        result[0] = new JSONArray();
        result[1] = new JSONArray();
        JSONObject item = new JSONObject();
        List<String> numbers = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            line = line.trim();
            switch (line) {
                case "SkyObject=BeginObject":
                    item = new JSONObject();
                    numbers.clear();
                    break;
                case "EndObject=SkyObject":
                    Collections.sort(numbers);
                    item.put("ssid",
                        EncryptUtil.md5(TextUtils.join("", numbers).replace(" ", "").toLowerCase()).toLowerCase());
                    if (item.has("DateObserved")) {
                        result[0].put(item);
                    } else {
                        result[1].put(item);
                    }
                    break;
                default:
                    String[] s = line.split("=");
                    switch (s[0]) {
                        case "ObjectID":
                            item.put("ObjectID", s[1]);
                            break;
                        case "DateObserved":
                            item.put("DateObserved", s[1]);
                            break;
                        case "CatalogNumber":
                            numbers.add(s[1]);
                            break;
                    }
                    break;
            }
        }
        br.close();
        return result;
    }

    @Override
    public void onLocationError(boolean showMessage) {
        if (showMessage) {
            Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickLocation(View v) {
        startLocator();
    }

    @Override
    public void onWithoutService(int error) {
        // TODO Auto-generated method stub

    }

}
