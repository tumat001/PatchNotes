package com.example.patchnotes.dialogs;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringTimePeriodAndTimePhase;

public class InputTimePeriodAndTimePhaseDialogSequence {

    private FragmentManager fragmentManager;
    private ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter;

    public InputTimePeriodAndTimePhaseDialogSequence(@NonNull FragmentManager fragmentManager,
                                                     @NonNull ContentFilterAlgorithmRequiringTimePeriodAndTimePhase contentFilter) {
        this.fragmentManager = fragmentManager;
        this.contentFilter = contentFilter;
    }

    public void initiateDialogSequence() {
        InputDayPickerDialog dialog = new InputDayPickerDialog();
        dialog.setContentFilterAlgorithmRequiringTimePeriodAndTimePhase(contentFilter);
        dialog.isPartOfTimePeriodAndTimePhaseSequence(true);
        dialog.show(fragmentManager, "inputDayPickerDialog");
    }

}
