package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringDaysOfWeek;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class InputDayOfWeekDialog extends DialogFragment {

    public interface OnDayOfWeekInputChosenListener {
        void onDaysOfWeekInputChosen(DaysOfWeek[] daysOfWeeks, String tag);
    }

    private static final String SAVED_SELECTED_DAYS = "savedSelectedDays";
    private static final String SAVED_CONTENT_FILTER = "savedContentFilter";
    private static final String SAVED_TAG = "savedTag";

    private static final String DIALOG_TITLE = "Select Days";
    private static final String DIALOG_CANCEL_TEXT = "Cancel";
    private static final String DIALOG_OK_TEXT = "Ok";

    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener filterListener;
    private OnDayOfWeekInputChosenListener dayOfWeekListener;
    private HashSet<DaysOfWeek> selectedDaysOfWeek = new HashSet<>();
    private ContentFilterAlgorithmRequiringDaysOfWeek contentFilter;
    private DaysOfWeek[] initialSelection;
    private String tag;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            filterListener = (FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            filterListener = null;
        }

        try {
            dayOfWeekListener = (OnDayOfWeekInputChosenListener) context;
        } catch (ClassCastException e) {
            dayOfWeekListener = null;
        }

        if (filterListener == null && dayOfWeekListener == null) {
            throw new ClassCastException("Parent activity does not implement either " +
                    "OnFilterAlgorithmSelectedListener nor OnDayOfWeekChosenListener");
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (initialSelection != null) {
            selectedDaysOfWeek.addAll(Arrays.asList(initialSelection));
        }

        if (savedInstanceState != null && selectedDaysOfWeek.isEmpty()) {
            selectedDaysOfWeek.addAll((HashSet<DaysOfWeek>) savedInstanceState.getSerializable(SAVED_SELECTED_DAYS));
        }
        if (savedInstanceState != null) {
            contentFilter = (ContentFilterAlgorithmRequiringDaysOfWeek) savedInstanceState.getSerializable(SAVED_CONTENT_FILTER);
            tag = savedInstanceState.getString(SAVED_TAG);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setMultiChoiceItems(getNamesOfDays(), getSelectedDaysIndex(savedInstanceState), (dialog, which, isChecked) -> {
            DaysOfWeek tappedDay = DaysOfWeek.values()[which];
            if (selectedDaysOfWeek.contains(tappedDay)) {
                selectedDaysOfWeek.remove(tappedDay);
            } else {
                selectedDaysOfWeek.add(tappedDay);
            }
        });
        builder.setPositiveButton(DIALOG_OK_TEXT, (dI, which) -> {
            if (!selectedDaysOfWeek.isEmpty()) {
                if (filterListener != null) {
                    contentFilter.setArgumentDaysOfWeek(selectedDaysOfWeek.toArray(new DaysOfWeek[0]));
                    filterListener.onFilterAlgorithmSelected(contentFilter);
                }
                if (dayOfWeekListener != null) {
                    dayOfWeekListener.onDaysOfWeekInputChosen(selectedDaysOfWeek.toArray(new DaysOfWeek[0]), tag);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Please choose at least one day", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(DIALOG_CANCEL_TEXT, (dI, which) -> {
            dismiss();
        });
        return builder.create();
    }

    private boolean[] getSelectedDaysIndex(Bundle savedInstanceState) {
        boolean[] selectedDays = new boolean[DaysOfWeek.values().length];
        if (savedInstanceState != null) {
            for (int i = 0; i < selectedDays.length; i++) {
                if (selectedDaysOfWeek.contains(DaysOfWeek.values()[i])) {
                    selectedDays[i] = true;
                }
            }
        } else {
            if (initialSelection != null) {
                List<DaysOfWeek> initialSelected = Arrays.asList(initialSelection);
                for (int i = 0; i < selectedDays.length; i++) {
                    if (initialSelected.contains(DaysOfWeek.values()[i])) {
                        selectedDays[i] = true;
                    }
                }
            }
        }
        return selectedDays;
    }

    private String[] getNamesOfDays() {
        String[] names = new String[DaysOfWeek.values().length];
        for (int i = 0; i < DaysOfWeek.values().length; i++) {
            names[i] = DaysOfWeek.values()[i].toString();
        }
        return names;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putSerializable(SAVED_SELECTED_DAYS, selectedDaysOfWeek);
        toSave.putSerializable(SAVED_CONTENT_FILTER, contentFilter);
        toSave.putString(SAVED_TAG, tag);
    }

    public void setContentFilterAlgorithmRequiringDaysOfWeek(@NonNull ContentFilterAlgorithmRequiringDaysOfWeek contentFilterAlgo) {
        this.contentFilter = contentFilterAlgo;
    }

    public void setInitialSelection(DaysOfWeek[] daysOfWeeks) {
        this.initialSelection = daysOfWeeks;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
