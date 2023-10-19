package com.example.patchnotes.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringTimePeriodAndTimePhase;
import com.example.patchnotes.contentdatarelated.TimePhase;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class InputTimePickerDialog extends DialogFragment {

    public interface OnTimePickedListener extends Serializable {
        void onTimePicked(int hourOfDay, int minute, String tag);
    }

    private static final String SAVED_HOUR = "savedHour";
    private static final String SAVED_MINUTE = "savedMinute";
    private static final String SAVED_IS_PART_OF_SEQUENCE = "savedIsPartOfSequence";
    private static final String SAVED_FILTER = "savedFilter";
    private static final String SAVED_TIME_PICKED_LISTENER = "savedTimePickedListener";
    private static final String SAVED_TAG = "savedTag";

    private static final String DIALOG_TITLE = "Set time";
    private static final String DIALOG_OK = "Ok";
    private static final String DIALOG_CANCEL = "Cancel";

    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener noteFilterListener;
    private OnTimePickedListener timePickedListener;
    private ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter;
    private TimePickerDialog dialog;

    private Calendar initialTime;

    private int hourOfDay;
    private int minute;
    private String tag;

    private boolean isPartOfSequence = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            noteFilterListener = (FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            noteFilterListener = null;
        }

        try {
            timePickedListener = (OnTimePickedListener) context;
        } catch (ClassCastException ignored) {

        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour, minute;
        if (savedInstanceState != null) {
            hour = savedInstanceState.getInt(SAVED_HOUR);
            minute = savedInstanceState.getInt(SAVED_MINUTE);
            isPartOfSequence = savedInstanceState.getBoolean(SAVED_IS_PART_OF_SEQUENCE);
            filter = (ContentFilterAlgorithmRequiringTimePeriodAndTimePhase) savedInstanceState.getSerializable(SAVED_FILTER);
            if (timePickedListener == null) {
                timePickedListener = (OnTimePickedListener) savedInstanceState.getSerializable(SAVED_TIME_PICKED_LISTENER);
            }
            tag = savedInstanceState.getString(SAVED_TAG);
        } else {
            Calendar currentTime;
            if (initialTime == null) {
                currentTime = GregorianCalendar.getInstance();
            } else {
                currentTime = initialTime;
            }
            hour = currentTime.get(Calendar.HOUR_OF_DAY);
            minute = currentTime.get(Calendar.MINUTE);
        }

        this.hourOfDay = hour;
        this.minute = minute;
        //todo figure out why at landscape this does not display
        dialog = new TimePickerDialog(getContext(), null, hour, minute, true) {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                InputTimePickerDialog parent = InputTimePickerDialog.this;
                parent.hourOfDay = hourOfDay;
                parent.minute = minute;
            }
        };
        dialog.setTitle(DIALOG_TITLE);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, DIALOG_OK, (dI, which) -> {
            if (noteFilterListener != null) {
                filter = attachHourMinuteToFilter(filter);
                if (isPartOfSequence) {
                    startUpInputTimePhaseDialog(filter);
                } else {
                    noteFilterListener.onFilterAlgorithmSelected(filter);
                }
            }
            if (timePickedListener != null) {
                timePickedListener.onTimePicked(this.hourOfDay, this.minute, tag);
            }
            dismiss();
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, DIALOG_CANCEL, (dI, which) -> {
            dismiss();
        });

        return dialog;
    }

    private ContentFilterAlgorithmRequiringTimePeriodAndTimePhase attachHourMinuteToFilter(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter) {
        TimePhase filterTimePhase = filter.getTimePhase();
        Calendar filterTime = filter.getTimePeriod();

        filterTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        filterTime.set(Calendar.MINUTE, minute);
        filter.setArgumentTimePeriodAndTimePhase(filterTime, filterTimePhase);
        return filter;
    }

    private void startUpInputTimePhaseDialog(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter) {
        InputTimePhaseDialog dialog = new InputTimePhaseDialog();
        dialog.setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(filter);
        dialog.show(getFragmentManager(), "inputTimePhase");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putInt(SAVED_HOUR, hourOfDay);
        toSave.putInt(SAVED_MINUTE, minute);
        toSave.putBoolean(SAVED_IS_PART_OF_SEQUENCE, isPartOfSequence);
        toSave.putSerializable(SAVED_FILTER, filter);
        toSave.putSerializable(SAVED_TIME_PICKED_LISTENER, timePickedListener);
        toSave.putString(SAVED_TAG, tag);
    }

    //

    void isPartOfTimePeriodAndTimePhaseSequence(boolean isPartOfSequence) {
        this.isPartOfSequence = isPartOfSequence;
    }

    public void setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter) {
        this.filter = contentFilter;
    }

    public void setInitialTime(Calendar initialTime) {
        this.initialTime = initialTime;
    }

    public void setTimePickedListener(OnTimePickedListener listener) {
        this.timePickedListener = listener;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
