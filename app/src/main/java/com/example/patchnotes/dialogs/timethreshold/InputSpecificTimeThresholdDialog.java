package com.example.patchnotes.dialogs.timethreshold;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.R;
import com.example.patchnotes.database.TimeThreshold;

public class InputSpecificTimeThresholdDialog extends DialogFragment {

    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_NUMBER_INPUT = "savedNumberInput";
    private static final String SAVED_UNIT_PICKED_VALUE = "savedUnitPickedValue";

    private OnInputTimeThresholdListener listener;
    private String tag;
    private TimeThreshold initialThreshold;

    private NumberPicker unitThresholdPicker;
    private EditText numberThresholdInput;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnInputTimeThresholdListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity does not implement OnInputTimeThresholdListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            tag = savedInstanceState.getString(SAVED_TAG);
        }

        View pickerView = configureAndReturnDialogPickerView(savedInstanceState);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(pickerView);
        dialog.setTitle("Set Time Threshold");
        dialog.setNegativeButton("cancel", null);
        dialog.setPositiveButton("set", (dI, which) -> passThresholdToListener());
        return dialog.create();
    }

    private View configureAndReturnDialogPickerView(Bundle savedInstanceState) {
        View pickerView = View.inflate(getContext(), R.layout.dialog_time_threshold_layout, null);

        unitThresholdPicker = pickerView.findViewById(R.id.dialog_unit_name_threshold);
        unitThresholdPicker.setMinValue(0);
        unitThresholdPicker.setMaxValue(TimeThreshold.UnitThreshold.values().length - 1);
        unitThresholdPicker.setDisplayedValues(constructUnitValues());
        unitThresholdPicker.setValue(determineValueOfUnitThreshold(savedInstanceState));

        numberThresholdInput = pickerView.findViewById(R.id.dialog_number_unit_threshold);
        numberThresholdInput.setText(determineValueOfNumberThreshold(savedInstanceState));

        return pickerView;
    }

    private int determineValueOfUnitThreshold(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(SAVED_UNIT_PICKED_VALUE);
        } else if (initialThreshold != null) {
            return initialThreshold.getUnitThreshold().ordinal();
        } else {
            return 0;
        }
    }

    private String determineValueOfNumberThreshold(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getString(SAVED_NUMBER_INPUT);
        } else if (initialThreshold != null && initialThreshold.getNumberThreshold() > 0) {
            return String.valueOf(initialThreshold.getNumberThreshold());
        } else {
            return "3";
        }
    }

    private String[] constructUnitValues() {
        String[] unitNames = new String[TimeThreshold.UnitThreshold.values().length];
        for (int i = 0; i < unitNames.length; i++) {
            unitNames[i] = TimeThreshold.UnitThreshold.values()[i].toString().toLowerCase();
        }
        return unitNames;
    }

    private void passThresholdToListener() {
        try {
            Integer.parseInt(numberThresholdInput.getText().toString());
            listener.onInputTimeThreshold(getTimeThreshold());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private TimeThreshold getTimeThreshold() {
        TimeThreshold.UnitThreshold threshold = TimeThreshold.UnitThreshold.DAY;
        for (TimeThreshold.UnitThreshold u: TimeThreshold.UnitThreshold.values()) {
            if (u.toString().equalsIgnoreCase(TimeThreshold.UnitThreshold.values()[unitThresholdPicker.getValue()].toString())) {
                threshold = u;
                break;
            }
        }

        int number = Integer.parseInt(numberThresholdInput.getText().toString());

        return new TimeThreshold(threshold, number);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putString(SAVED_TAG, tag);
        toSave.putInt(SAVED_UNIT_PICKED_VALUE, unitThresholdPicker.getValue());
        toSave.putString(SAVED_NUMBER_INPUT, numberThresholdInput.getText().toString());
    }

    //

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setInitialThresholdValues(TimeThreshold thresholdValues) {
        this.initialThreshold = thresholdValues;
    }


}
