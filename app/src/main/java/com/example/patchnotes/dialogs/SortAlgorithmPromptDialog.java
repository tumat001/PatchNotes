package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentsorter.ContentSorterAlgorithm;

public class SortAlgorithmPromptDialog extends DialogFragment {

    public interface OnSortAlgorithmSelectedListener {
        void onSortAlgorithmSelected(@NonNull ContentSorterAlgorithm sorterAlgorithm);
    }

    private static final String SAVED_SORT_ALGORITHM_PROVIDED = "savedSortAlgorithmsProvided";
    private static final String SAVED_SORT_ALGORITHM_SELECTED = "savedSortAlgorithmSelected";

    private static final String DIALOG_TITLE = "Choose Sort Method";

    private OnSortAlgorithmSelectedListener listener;
    private ContentSorterAlgorithm[] sorterAlgorithms;
    private ContentSorterAlgorithm selectedSorterAlgorithm;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnSortAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnSortAlgorithmSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null && sorterAlgorithms == null) {
            sorterAlgorithms = (ContentSorterAlgorithm[]) savedInstanceState.getSerializable(SAVED_SORT_ALGORITHM_PROVIDED);
        } else if (sorterAlgorithms == null) {
            sorterAlgorithms = new ContentSorterAlgorithm[0];
        }

        if (savedInstanceState != null) {
            selectedSorterAlgorithm = (ContentSorterAlgorithm) savedInstanceState.getSerializable(SAVED_SORT_ALGORITHM_SELECTED);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setSingleChoiceItems(
                convertSorterAlgorithmToCorrespondingDisplayName(sorterAlgorithms), getIndexOfSelectedAlgo(), (dI, which) -> {
                    listener.onSortAlgorithmSelected(sorterAlgorithms[which]);
                    dismiss();
                });
        return builder.create();
    }

    private String[] convertSorterAlgorithmToCorrespondingDisplayName(ContentSorterAlgorithm[] sorterAlgorithms) {
        String[] sortAlgoName = new String[sorterAlgorithms.length];
        for (int i = 0; i < sortAlgoName.length; i++) {
            sortAlgoName[i] = sorterAlgorithms[i].toString();
        }
        return sortAlgoName;
    }

    private int getIndexOfSelectedAlgo() {
        for (int i = 0; i < sorterAlgorithms.length; i++) {
            if (selectedSorterAlgorithm == sorterAlgorithms[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putSerializable(SAVED_SORT_ALGORITHM_PROVIDED, sorterAlgorithms);
        toSave.putSerializable(SAVED_SORT_ALGORITHM_SELECTED, selectedSorterAlgorithm);
    }

    public void setSorterAlgorithms(@Nullable ContentSorterAlgorithm[] sorterAlgorithms) {
        this.sorterAlgorithms = sorterAlgorithms;
    }

    public void setSelectedSorterAlgorithm(@Nullable ContentSorterAlgorithm sorterAlgorithm) {
        this.selectedSorterAlgorithm = sorterAlgorithm;
    }

}