package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithm;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class AssessmentDialog extends DialogFragment {

    private static final String SAVED_SORTER_ALGORTHIM = "savedSorterAlgorithm";
    private static final String SAVED_FILER_ALGORITHM_LIST = "savedFilterAlgorithmList";

    private static final String DIALOG_TITLE = "Content List Modifiers";
    private static final String DIALOG_OK = "Ok";

    private ContentSorterAlgorithm sorterAlgorithm;
    private ArrayList<ContentFilterAlgorithm> filterAlgorithmList = new ArrayList<>();

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sorterAlgorithm = (ContentSorterAlgorithm) savedInstanceState.getSerializable(SAVED_SORTER_ALGORTHIM);
            filterAlgorithmList = (ArrayList<ContentFilterAlgorithm>) savedInstanceState.getSerializable(SAVED_FILER_ALGORITHM_LIST);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(DIALOG_TITLE);
        builder.setMessage(getAssessment());
        builder.setPositiveButton(DIALOG_OK, (dI, i) -> {
            dismiss();
        });
        return builder.create();
    }

    private String getAssessment() {
        return "Sort method applied:\n" +
                getAssessmentOfSortAlgorithm() +
                "\n\n" +
                "Filters applied:\n" +
                getAssessmentOfFilterAlgorithmList();
    }

    private String getAssessmentOfSortAlgorithm() {
        if (sorterAlgorithm == null) {
            return "- None";
        } else {
            return "- " + sorterAlgorithm.toString();
        }
    }

    private String getAssessmentOfFilterAlgorithmList() {
        if (filterAlgorithmList.isEmpty()) {
            return "- None";
        } else {
            StringBuilder builder = new StringBuilder();
            for (ContentFilterAlgorithm filter: filterAlgorithmList) {
                builder.append("- ").append(filter.getFilterDescription()).append("\n");
            }
            return builder.toString();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putSerializable(SAVED_SORTER_ALGORTHIM, sorterAlgorithm);
        toSave.putSerializable(SAVED_FILER_ALGORITHM_LIST, filterAlgorithmList);
    }

    public void setSorterAlgorithm(@Nullable ContentSorterAlgorithm sorterAlgorithm) {
        this.sorterAlgorithm = sorterAlgorithm;
    }

    public void setFilterAlgorithmList(@Nullable List<ContentFilterAlgorithm> filterAlgorithmList) {
        this.filterAlgorithmList.clear();
        if (filterAlgorithmList != null) {
            this.filterAlgorithmList.addAll(filterAlgorithmList);
        }
    }
}