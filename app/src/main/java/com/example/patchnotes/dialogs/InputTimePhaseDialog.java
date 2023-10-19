package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringTimePeriodAndTimePhase;
import com.example.patchnotes.contentdatarelated.TimePhase;

import java.util.Calendar;

public class InputTimePhaseDialog extends DialogFragment {

    private static final String SAVED_SELECTED_INDEX = "savedSelectedIndex";
    private static final String SAVED_FILTER = "savedFilter";

    private static final String DIALOG_TITLE = "Pick time phase";
    private static final String DIALOG_OK = "Ok";
    private static final String DIALOG_CANCEL = "Cancel";

    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener listener;
    private ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter;

    private int selectedIndex;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnFilterAlgorithmSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            filter = (ContentFilterAlgorithmRequiringTimePeriodAndTimePhase) savedInstanceState.getSerializable(SAVED_FILTER);
            selectedIndex = savedInstanceState.getInt(SAVED_SELECTED_INDEX);
        } else {
            selectedIndex = 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setSingleChoiceItems(getNamesOfTimePhases(), selectedIndex, ((dialog, which) -> {
            selectedIndex = which;
        }));
        builder.setPositiveButton(DIALOG_OK, (dI, which) -> {
            attachTimePhaseToNoteFilter(filter, TimePhase.values()[selectedIndex]);
            listener.onFilterAlgorithmSelected(filter);
        });
        builder.setNegativeButton(DIALOG_CANCEL, (dI, which) -> {
            dismiss();
        });
        return builder.create();
    }

    private String[] getNamesOfTimePhases() {
        String[] names = new String[TimePhase.values().length];
        for (int i = 0; i < names.length; i++) {
            names[i] = TimePhase.values()[i].toString();
        }
        return names;
    }

    private void attachTimePhaseToNoteFilter(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter, TimePhase chosenPhase) {
        Calendar filterTime = filter.getTimePeriod();
        filter.setArgumentTimePeriodAndTimePhase(filterTime, chosenPhase);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putSerializable(SAVED_FILTER, filter);
        toSave.putInt(SAVED_SELECTED_INDEX, selectedIndex);
    }

    //

    public void setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter) {
        this.filter = filter;
    }
}
