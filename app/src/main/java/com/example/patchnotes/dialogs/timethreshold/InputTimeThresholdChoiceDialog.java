package com.example.patchnotes.dialogs.timethreshold;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.database.TimeThreshold;

public class InputTimeThresholdChoiceDialog extends DialogFragment {

    private static final String SAVED_INITIAL_TIME_THRESHOLD = "savedInitialTimeThreshold";
    private static final String SAVED_TAG = "savedTag";

    private OnInputTimeThresholdListener listener;
    private TimeThreshold initialTimeThreshold;
    private String tag;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnInputTimeThresholdListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnInputTimeThresholdListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initializeTimeThreshold(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Deletion Threshold");
        builder.setSingleChoiceItems(getChoices(), -1, this::onSelectedItem);
        return builder.create();
    }

    private void initializeTimeThreshold(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            initialTimeThreshold = (TimeThreshold) savedInstanceState.getSerializable(SAVED_INITIAL_TIME_THRESHOLD);
        }
    }

    private String[] getChoices() {
        return new String[] {
                "Never delete notes",
                "Delete notes immediately",
                "Use custom deletion threshold"
        };
    }

    private void onSelectedItem(DialogInterface dI, int index) {
        if (index == 0) {
            listener.onInputTimeThreshold(new TimeThreshold(TimeThreshold.UnitThreshold.DAY, -1));
        } else if (index == 1) {
            listener.onInputTimeThreshold(new TimeThreshold(TimeThreshold.UnitThreshold.DAY, 0));
        } else if (index == 2) {
            showInputSpecificTimeDialog();
        }
        dismiss();
    }

    private void showInputSpecificTimeDialog() {
        InputSpecificTimeThresholdDialog dialog = new InputSpecificTimeThresholdDialog();
        dialog.setInitialThresholdValues(initialTimeThreshold);
        dialog.setTag(tag);
        dialog.show(getFragmentManager(), "SpecificTimeThresholdInput");
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putSerializable(SAVED_INITIAL_TIME_THRESHOLD, initialTimeThreshold);
        toSave.putString(SAVED_TAG, tag);
    }

    //

    public void setInitialTimeThreshold(TimeThreshold initialTimeThreshold) {
        this.initialTimeThreshold = initialTimeThreshold;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
