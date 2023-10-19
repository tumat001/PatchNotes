package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringTimePeriodAndTimePhase;
import com.example.patchnotes.contentdatarelated.TimePhase;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A fragment that prompts the user for a date. To receive the date selected, parent activity must implement
 * {@link com.example.patchnotes.dialogs.FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener}. The date received
 * is a calendar instance whose year, month, and day is that of the user's input. The calendar's other fields
 * however are not updated/changed, and should be disregarded.
 */
public class InputDayPickerDialog extends DialogFragment {

    public interface OnDayPickedListener extends Serializable {
        void onDayPicked(int year, int month, int dayOfMonth, String tag);
    }

    private static final String SAVED_YEAR = "savedYear";
    private static final String SAVED_MONTH_OF_YEAR = "savedMonthOfYear";
    private static final String SAVED_DAY_OF_MONTH = "savedDayOfMonth";
    private static final String SAVED_FILTER = "savedFilter";
    private static final String SAVED_IS_PART_OF_SEQUENCE = "savedIsPartOfSequence";
    private static final String SAVED_DAY_PICKED_LISTENER = "savedDayPickedListener";
    private static final String SAVED_TAG = "savedTag";

    private static final String DIALOG_TITLE = "Pick date to set";
    private static final String DIALOG_OK = "Ok";
    private static final String DIALOG_CANCEL = "Cancel";

    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener noteFilterListener;
    private OnDayPickedListener dayPickedListener;
    private ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter;
    private DatePicker datePicker;
    private String tag;

    private Calendar initialDayValues;

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
            dayPickedListener = (OnDayPickedListener) context;
        } catch (ClassCastException ignore) {
            //
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        datePicker = new DatePicker(getContext());
        int year, month, day;
        if (savedInstanceState != null) {
            year = savedInstanceState.getInt(SAVED_YEAR);
            month = savedInstanceState.getInt(SAVED_MONTH_OF_YEAR);
            day = savedInstanceState.getInt(SAVED_DAY_OF_MONTH);
            contentFilter = (ContentFilterAlgorithmRequiringTimePeriodAndTimePhase) savedInstanceState.getSerializable(SAVED_FILTER);
            isPartOfSequence = savedInstanceState.getBoolean(SAVED_IS_PART_OF_SEQUENCE);
            if (dayPickedListener == null) {
                dayPickedListener = (OnDayPickedListener) savedInstanceState.getSerializable(SAVED_DAY_PICKED_LISTENER);
            }
            tag = savedInstanceState.getString(SAVED_TAG);
        } else {
            Calendar initialDayToDisplay;
            if (initialDayValues == null) {
                initialDayToDisplay = GregorianCalendar.getInstance();
            } else {
                initialDayToDisplay = initialDayValues;
            }
            year = initialDayToDisplay.get(Calendar.YEAR);
            month = initialDayToDisplay.get(Calendar.MONTH);
            day = initialDayToDisplay.get(Calendar.DAY_OF_MONTH);
        }
        datePicker.init(year, month, day, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setView(datePicker);
        builder.setPositiveButton(DIALOG_OK, (dI, which) -> {
            dateSelected();
            dismiss();
        });
        builder.setNegativeButton(DIALOG_CANCEL, (dI, which) -> {
            dismiss();
        });
        return builder.create();
    }

    private void dateSelected() {
        if (noteFilterListener != null) {
            TimePhase timePhase = contentFilter.getTimePhase();
            Calendar calendar = contentFilter.getTimePeriod();
            calendar.set(Calendar.YEAR, datePicker.getYear());
            calendar.set(Calendar.MONTH, datePicker.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

            contentFilter.setArgumentTimePeriodAndTimePhase(calendar, timePhase);

            if (isPartOfSequence) {
                startUpInputTimeDialog(contentFilter);
            } else {
                noteFilterListener.onFilterAlgorithmSelected(contentFilter);
            }
        }

        if (dayPickedListener != null) {
            dayPickedListener.onDayPicked(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), tag);
        }
    }

    private void startUpInputTimeDialog(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter) {
        InputTimePickerDialog dialog = new InputTimePickerDialog();
        dialog.isPartOfTimePeriodAndTimePhaseSequence(true);
        dialog.setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(contentFilter);
        dialog.show(getFragmentManager(), "inputTimePickerDialog");
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putInt(SAVED_YEAR, datePicker.getYear());
        toSave.putInt(SAVED_MONTH_OF_YEAR, datePicker.getMonth());
        toSave.putInt(SAVED_DAY_OF_MONTH, datePicker.getDayOfMonth());
        toSave.putSerializable(SAVED_FILTER, contentFilter);
        toSave.putBoolean(SAVED_IS_PART_OF_SEQUENCE, isPartOfSequence);
        toSave.putSerializable(SAVED_DAY_PICKED_LISTENER, dayPickedListener);
        toSave.putString(SAVED_TAG, tag);
    }

    //

    public void setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(@NonNull ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter) {
        this.contentFilter = contentFilter;
    }

    void isPartOfTimePeriodAndTimePhaseSequence(boolean isPartOfSequence) {
        this.isPartOfSequence = isPartOfSequence;
    }

    public void setInitialDayValues(Calendar initialDayValues) {
        this.initialDayValues = initialDayValues;
    }

    public void setDayPickedListener(OnDayPickedListener listener) {
        this.dayPickedListener = listener;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}