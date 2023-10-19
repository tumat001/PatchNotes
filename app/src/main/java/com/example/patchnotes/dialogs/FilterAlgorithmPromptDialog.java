package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithm;
import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringCategory;
import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringDaysOfWeek;
import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringText;
import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringTimePeriodAndTimePhase;
import com.example.patchnotes.dialogs.categoryrelated.CategorySpinnerDialog;

import java.util.ArrayList;
import java.util.List;

public class FilterAlgorithmPromptDialog extends DialogFragment {

    public interface OnFilterAlgorithmSelectedListener {
        void onFilterAlgorithmSelected(@NonNull ContentFilterAlgorithm filterAlgorithm);
    }

    private static final String SAVED_FILTER_CHOICE_LIST = "savedFilterChoiceList";

    private static final String DIALOG_TITLE = "Choose Filter Type";

    private OnFilterAlgorithmSelectedListener listener;
    private ArrayList<ContentFilterAlgorithm> filterAlgorithms = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnFilterAlgorithmSelectedListener");
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null && filterAlgorithms.isEmpty()) {
            filterAlgorithms.addAll((ArrayList<ContentFilterAlgorithm>) savedInstanceState.getSerializable(SAVED_FILTER_CHOICE_LIST));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setSingleChoiceItems(getNamesOfFilterAlgorithmList(filterAlgorithms), -1, (dI, which) -> {
            onFilterChoiceSelected(which);
        });
        return builder.create();
    }

    private String[] getNamesOfFilterAlgorithmList(List<ContentFilterAlgorithm> filterAlgorithms) {
        String[] names = new String[filterAlgorithms.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = filterAlgorithms.get(i).getFilterName();
        }
        return names;
    }

    private void onFilterChoiceSelected(int which) {
        ContentFilterAlgorithm filterChosen = filterAlgorithms.get(which);
        if (filterChosen instanceof ContentFilterAlgorithmRequiringTimePeriodAndTimePhase) {
            callTimePeriodAndTimePhaseSequence((ContentFilterAlgorithmRequiringTimePeriodAndTimePhase) filterChosen);

        } else if (filterChosen instanceof ContentFilterAlgorithmRequiringText) {
            callInputTextDialog((ContentFilterAlgorithmRequiringText) filterChosen);

        } else if (filterChosen instanceof ContentFilterAlgorithmRequiringDaysOfWeek) {
            callInputDayOfWeekDialog((ContentFilterAlgorithmRequiringDaysOfWeek) filterChosen);

        } else if (filterChosen instanceof ContentFilterAlgorithmRequiringCategory) {
            callCategorySpinnerDialog();
        }

        dismiss();
    }

    //

    private void callInputDayOfWeekDialog(ContentFilterAlgorithmRequiringDaysOfWeek filter) {
        InputDayOfWeekDialog dialog = new InputDayOfWeekDialog();
        dialog.setContentFilterAlgorithmRequiringDaysOfWeek(filter);
        dialog.show(getFragmentManager(), "inputDayOfWeekDialog");
    }

    private void callInputTextDialog(ContentFilterAlgorithmRequiringText filter) {
        InputTextDialog dialog = new InputTextDialog();
        dialog.setContentFilterAlgorithmRequiringText(filter);
        dialog.show(getFragmentManager(), "inputTextDialog");
    }

    private void callTimePeriodAndTimePhaseSequence(ContentFilterAlgorithmRequiringTimePeriodAndTimePhase filter) {
        InputTimePeriodAndTimePhaseDialogSequence sequence = new InputTimePeriodAndTimePhaseDialogSequence(getFragmentManager(), filter);
        sequence.initiateDialogSequence();
    }

    private void callCategorySpinnerDialog() {
        CategorySpinnerDialog dialog = new CategorySpinnerDialog();
        dialog.show(getFragmentManager(), "inputCategoryDialog");
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putSerializable(SAVED_FILTER_CHOICE_LIST, filterAlgorithms);
    }

    //

    public void setFilterAlgorithms(List<ContentFilterAlgorithm> filterAlgorithms) {
        this.filterAlgorithms.clear();
        this.filterAlgorithms.addAll(filterAlgorithms);
    }

}
