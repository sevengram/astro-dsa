package com.mydeepsky.dsa.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mydeepsky.dsa.ui.MainActivity;

public class TimeDialogFragment extends DialogFragment {

    public static final int SHOW_DATEPICK = 90;
    public static final int SHOW_STARTTIME_PICK = 91;
    public static final int SHOW_ENDTIME_PICK = 92;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        Bundle args = getArguments();
        switch (args.getInt("type")) {
        case SHOW_DATEPICK:
            return new DatePickerDialog(activity, activity.getOnDateSetListener(),
                    args.getInt("year"), args.getInt("month") - 1, args.getInt("date"));
        case SHOW_STARTTIME_PICK:
            return new TimePickerDialog(activity, activity.getOnStartTimeSetListener(),
                    args.getInt("hour"), args.getInt("min"), true);
        case SHOW_ENDTIME_PICK:
            return new TimePickerDialog(activity, activity.getOnEndTimeSetListener(),
                    args.getInt("hour"), args.getInt("min"), true);
        default:
            return null;
        }
    }
}
